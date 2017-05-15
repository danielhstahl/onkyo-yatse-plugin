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

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.danielhstahl.plugin.avreceiver.onkyo.helpers.EiscpConnector;
import com.danielhstahl.plugin.avreceiver.onkyo.helpers.EiscpListener;
import com.danielhstahl.plugin.avreceiver.onkyo.helpers.PreferencesHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.yatse.plugin.avreceiver.api.AVReceiverPluginService;
import tv.yatse.plugin.avreceiver.api.PluginCustomCommand;
import tv.yatse.plugin.avreceiver.api.YatseLogger;


/**
 * Sample AVReceiverPluginService that implement all functions with dummy code that displays Toast and logs to main Yatse log system.
 * <p/>
 * See {@link AVReceiverPluginService} for documentation on all functions
 */
public class OnkyoPluginService extends AVReceiverPluginService {
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String TAG = "OnkyoPluginService";
    private Map<String, String> lastReceivedValues = new HashMap<>();
    private String mHostUniqueId;
    private String mHostName;
    private String mHostIp;
    private String mReceiverPort;
    private String mReceiverIP;
    private EiscpConnector conn = null;
    private boolean mIsMuted = false;
    private double mVolumePercent = 50;
    private static final double max_volume = 100;
    private static final double numberOfPercentsInOne = 100.0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (conn != null)
            conn.close();
    }

    @Override
    protected int getVolumeUnitType() {
        return UNIT_TYPE_PERCENT;
    }

    @Override
    protected double getVolumeMinimalValue() {
        return 0.0;
    }

    @Override
    protected double getVolumeMaximalValue() {
        return max_volume;
    }

    @Override
    protected boolean setMuteStatus(boolean isMuted) {
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Setting mute status : %s", isMuted);
        if (isMuted)
            new sendIscpCommand().execute(EiscpConnector.MUTE_OFF); //unmute
        else
            new sendIscpCommand().execute(EiscpConnector.MUTE_ON); //mute
        mIsMuted = !isMuted;
        return true;
    }

    @Override
    protected boolean getMuteStatus() {
        return mIsMuted;
    }

    @Override
    protected boolean toggleMuteStatus() {
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Toggling mute status");
        setMuteStatus(mIsMuted);
        return true;
    }

    @Override
    protected boolean setVolumeLevel(double volume) {
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Setting volume level : %s", volume);
        new sendIscpCommand().execute(EiscpConnector.MASTER_VOL + String.format("0x%08X", (int) (volume))); //hexadecimal
        mVolumePercent = volume * numberOfPercentsInOne / max_volume;
        return true;
    }

    @Override
    protected double getVolumeLevel() {
        return mVolumePercent;
    }

    @Override
    protected boolean volumePlus() {
        new sendIscpCommand().execute(EiscpConnector.MASTER_VOL_UP);
        mVolumePercent = Math.min(max_volume, mVolumePercent + numberOfPercentsInOne / max_volume);
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Calling volume plus");
        return true;
    }

    @Override
    protected boolean volumeMinus() {
        new sendIscpCommand().execute(EiscpConnector.MASTER_VOL_DOWN);
        mVolumePercent = Math.max(0.0, mVolumePercent - numberOfPercentsInOne / max_volume);
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Calling volume minus");
        return true;
    }

    @Override
    protected boolean refresh() {
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Refreshing values from receiver");
        if (lastReceivedValues.get(EiscpConnector.MASTER_VOL) != null) {
            mVolumePercent = (double) (Integer.parseInt(lastReceivedValues.get(EiscpConnector.MASTER_VOL), 16)) * numberOfPercentsInOne / max_volume;
        }
        if (lastReceivedValues.get(EiscpConnector.MUTE) != null) {
            mIsMuted = lastReceivedValues.get(EiscpConnector.MUTE).equals("01");
        }
        return true;
    }

    @Override
    protected List<PluginCustomCommand> getDefaultCustomCommands() {
        String source = getString(R.string.plugin_unique_id);
        List<PluginCustomCommand> commands = new ArrayList<>();
        // Plugin custom commands must set the source parameter to their plugin unique Id !
        commands.add(new PluginCustomCommand().title("Sample command: Volume Up").source(source).param1(EiscpConnector.MASTER_VOL_UP).type(0));
        return commands;
    }

    @Override
    protected boolean executeCustomCommand(PluginCustomCommand customCommand) {
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Executing CustomCommand : %s", customCommand.title());
        new sendIscpCommand().execute(customCommand.param1());
        return false;
    }

    @Override
    protected void connectToHost(String uniqueId, String name, String ip) {
        mHostUniqueId = uniqueId;
        mHostName = name;
        mHostIp = ip;

        mReceiverIP = PreferencesHelper.getInstance(getApplicationContext()).hostIp(mHostUniqueId);
        mReceiverPort = PreferencesHelper.getInstance(getApplicationContext()).hostPort(mHostUniqueId);
        new connectToReceiver().execute();

        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Connected to : %s / %s ", name, mHostUniqueId);
    }

    @Override
    protected long getSettingsVersion() {
        return PreferencesHelper.getInstance(getApplicationContext()).settingsVersion();
    }

    @Override
    protected String getSettings() {
        return PreferencesHelper.getInstance(getApplicationContext()).getSettingsAsJSON();
    }

    @Override
    protected boolean restoreSettings(String settings, long version) {
        boolean result = PreferencesHelper.getInstance(getApplicationContext()).importSettingsFromJSON(settings, version);
        if (result) {
            connectToHost(mHostUniqueId, mHostName, mHostIp);
        }
        return result;
    }

    /*public void sendIscpCommand(String cmd) {
        try {
            conn.sendIscpCommand(cmd);

        } catch (Exception ex) {
            connectToHost(mHostUniqueId, mHostName, mHostIp);//attempt to connect if errrored
            YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when sending command: %s", ex.getMessage());
        }
    }*/
    public class sendIscpCommand extends AsyncTask<String, String, EiscpConnector> {
        @Override
        protected EiscpConnector doInBackground(String... message) {
            try {
                EiscpConnector conn = new EiscpConnector(mReceiverIP, Integer.parseInt(mReceiverPort));
                conn.sendIscpCommand(message[0]);
                conn.close();
            } catch (Exception e) {
                YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when connecting: %s", e.getMessage());
            }
            return null;
        }

    }

    public class connectToReceiver extends AsyncTask<String, String, EiscpConnector> {
        @Override
        protected EiscpConnector doInBackground(String... message) {
            EiscpConnector conn = null;
            try {
                conn = new EiscpConnector(mReceiverIP, Integer.parseInt(mReceiverPort));
            } catch (Exception e) {
                YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when connecting: %s", e.getMessage());
            }
            return conn;
        }

        @Override
        protected void onPostExecute(EiscpConnector conn_) {
            conn = conn_;
            ImplementListener listener = new ImplementListener(conn);
            Thread listenerThread = new Thread(listener);
            listenerThread.start();
        }

    }

    public class ImplementListener implements Runnable, EiscpListener {
        private EiscpConnector conn;

        public ImplementListener(EiscpConnector conn) {
            this.conn = conn;
        }

        @Override
        public void run() {
            try {
                conn.addListener(this);
                conn.sendIscpCommand(EiscpConnector.SYSTEM_POWER_QUERY);
                conn.sendIscpCommand(EiscpConnector.MUTE_QUERY);
                conn.sendIscpCommand(EiscpConnector.MASTER_VOL_QUERY);
            } catch (Exception ex) {
                YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when adding listener: %s", ex.getMessage());
            }
        }

        @Override
        public void receivedIscpMessage(String message) {
            String command = message.substring(0, 3);
            String parameter = message.substring(3);
            YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Receiving message");
            lastReceivedValues.put(command, parameter);
        }

    }
}
