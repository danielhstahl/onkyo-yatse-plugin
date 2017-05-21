/*
 * Copyright 2015 Tolriq / Genimee.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.danielhstahl.plugin.avreceiver.onkyo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tv.yatse.plugin.avreceiver.api.AVReceiverPluginService;
import tv.yatse.plugin.avreceiver.api.YatseLogger;
import com.danielhstahl.plugin.avreceiver.onkyo.helpers.EiscpConnector;

import com.danielhstahl.plugin.avreceiver.onkyo.helpers.PreferencesHelper;





/**
 * Sample SettingsActivity that handle correctly the parameters passed by Yatse.
 * <p/>
 * You need to save the passed extra {@link AVReceiverPluginService#EXTRA_STRING_MEDIA_CENTER_UNIQUE_ID}
 * and return it in the result intent.
 * <p/>
 * <b>Production plugin should make input validation and tests before accepting the user input and returning RESULT_OK.</b>
 */
public class SettingsActivity extends AppCompatActivity  {

    private static final String TAG = "SettingsActivity";
   // private EiscpConnector conn;
    private String mMediaCenterUniqueId;
    private String mMediaCenterName;
    private boolean mMuted;
    private String threadedIP;
    private String threadedPort;
    @BindView(R.id.receiver_settings_title)
    TextView mViewSettingsTitle;
    @BindView(R.id.receiver_port_description)
    TextView mPortDescription;
    @BindView(R.id.receiver_ip)
    EditText mViewReceiverIP;
    @BindView(R.id.receiver_port)
    EditText mViewReceiverPort;
    @BindView(R.id.btn_toggle_mute)
    ImageButton mViewMute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        if (getIntent() != null) {
            mMediaCenterUniqueId = getIntent().getStringExtra(AVReceiverPluginService.EXTRA_STRING_MEDIA_CENTER_UNIQUE_ID);
            mMediaCenterName = getIntent().getStringExtra(AVReceiverPluginService.EXTRA_STRING_MEDIA_CENTER_NAME);
        }
        if (TextUtils.isEmpty(mMediaCenterUniqueId)) {
            YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error : No media center unique id sent");
            Snackbar.make(findViewById(R.id.receiver_settings_content), "Wrong data sent by Yatse !", Snackbar.LENGTH_LONG).show();
        }
        mViewSettingsTitle.setText(getString(R.string.sample_plugin_settings) + " " + mMediaCenterName);
        mPortDescription.setText(getString(R.string.sample_plugin_receiver_port_description)+" ("+EiscpConnector.DEFAULT_EISCP_PORT+")");
        mViewReceiverIP.setText(PreferencesHelper.getInstance(getApplicationContext()).hostIp(mMediaCenterUniqueId));
        String myPort=PreferencesHelper.getInstance(getApplicationContext()).hostPort(mMediaCenterUniqueId);
        if(myPort.length()>5|myPort.length()==0){
            myPort=Integer.toString(EiscpConnector.DEFAULT_EISCP_PORT);
        }
        mViewReceiverPort.setText(myPort);
    }

    @OnClick({R.id.receiver_scan, R.id.btn_ok, R.id.btn_cancel, R.id.btn_vol_down, R.id.btn_toggle_mute, R.id.btn_vol_up})
    public void onClick(View v) {
        Intent resultIntent;
        threadedIP=mViewReceiverIP.getText().toString();
        threadedPort=mViewReceiverPort.getText().toString();
        switch (v.getId()) {
            case R.id.receiver_scan:
                new seekAddress().execute();
                break;
            case R.id.btn_toggle_mute:
                if(mMuted){
                    new testTask().execute(EiscpConnector.MUTE_OFF);
                }
                else {
                    new testTask().execute(EiscpConnector.MUTE_ON);
                }
                mViewMute.setImageResource(!mMuted ? R.drawable.ic_volume_low : R.drawable.ic_volume_off);
                mMuted = !mMuted;
                Snackbar.make(findViewById(R.id.receiver_settings_content), "Toggling mute", Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btn_vol_down:
                new testTask().execute(EiscpConnector.MASTER_VOL_DOWN);
                Snackbar.make(findViewById(R.id.receiver_settings_content), "Volume down", Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btn_vol_up:
                new testTask().execute(EiscpConnector.MASTER_VOL_UP);
                Snackbar.make(findViewById(R.id.receiver_settings_content), "Volume up", Snackbar.LENGTH_LONG).show();
                break;
            case R.id.btn_ok:
                PreferencesHelper.getInstance(getApplicationContext()).hostIp(mMediaCenterUniqueId, threadedIP);
                PreferencesHelper.getInstance(getApplicationContext()).hostPort(mMediaCenterUniqueId, threadedPort);
                resultIntent = new Intent();
                resultIntent.putExtra(AVReceiverPluginService.EXTRA_STRING_MEDIA_CENTER_UNIQUE_ID, mMediaCenterUniqueId);
                setResult(RESULT_OK, resultIntent);
                finish();
                break;
            case R.id.btn_cancel:
                resultIntent = new Intent();
                resultIntent.putExtra(AVReceiverPluginService.EXTRA_STRING_MEDIA_CENTER_UNIQUE_ID, mMediaCenterUniqueId);
                setResult(RESULT_CANCELED, resultIntent);
                finish();
                break;
            default:
                break;
        }
    }
    public class testTask extends AsyncTask<String, String, EiscpConnector> {
        @Override
        protected EiscpConnector doInBackground(String... message){
            try {
                EiscpConnector conn = new EiscpConnector(threadedIP, Integer.parseInt(threadedPort));
                conn.sendIscpCommand(message[0]);
                conn.close();
            }catch(Exception e){
                YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when connecting: %s", e);
            }
            return null;
        }

    }
    public class seekAddress extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... message){
            String address=null;
            try {
                EiscpConnector conn = EiscpConnector.autodiscover();
                address=conn.getAddress();
                conn.close();
            }catch(Exception e){
                YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when scanning: %s", e);
            }
            return address;
        }
        @Override
        protected void onPostExecute(String address){
            mViewReceiverIP.setText(address);
            mViewReceiverPort.setText(Integer.toString(EiscpConnector.DEFAULT_EISCP_PORT));
        }

    }

}
