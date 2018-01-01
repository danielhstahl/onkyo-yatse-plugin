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
import android.text.TextUtils;

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

    private static final String TAG = "OnkyoPluginService";
    private Map<String, String> lastReceivedValues = new HashMap<>();
    private String mHostUniqueId;
    EiscpConnector conn;
    private String mHostName;
    private String mHostIp;
    private String mReceiverPort;
    private String mReceiverIP;
    private boolean mIsMuted = false;
    private boolean isTwoWay = true;
    private double mVolumePercent = 50;
    private static final double max_volume = 100;
    private static final double numberOfPercentsInOne = 100.0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (conn != null) {
            conn.close();
            conn = null;
        }
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
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Setting mute status: %s", isMuted);
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
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Setting volume level: %s", volume);
        String volumeStr=String.format("%02X", (int) (volume));
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Setting volume level string: %s", volumeStr);
        new sendIscpCommand().execute(EiscpConnector.MASTER_VOL + volumeStr); //hexadecimal
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
        if (conn == null && isTwoWay) {
            // Was disconnected from receiver, try to reconnect
            new connectToReceiver().execute();
        }
        return true;
    }

    @Override
    protected List<PluginCustomCommand> getDefaultCustomCommands() {
        String source = getString(R.string.plugin_unique_id);
        List<PluginCustomCommand> commands = new ArrayList<>();
        // Plugin custom commands must set the source parameter to their plugin unique Id !
        commands.add(new PluginCustomCommand().title("Power: ON").source(source).param1(EiscpConnector.SYSTEM_POWER_ON).type(0));
        commands.add(new PluginCustomCommand().title("Power: Standby").source(source).param1(EiscpConnector.SYSTEM_POWER_STANDBY).type(0));
        return commands;
    }

    @Override
    protected boolean executeCustomCommand(PluginCustomCommand customCommand) {
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Executing CustomCommand: %s", customCommand.title());
        if (!TextUtils.isEmpty(customCommand.param1())) {
            new sendIscpCommand().execute(customCommand.param1());
        }
        return true;
    }

    @Override
    protected void connectToHost(String uniqueId, String name, String ip) {
        mHostUniqueId = uniqueId;
        mHostName = name;
        mHostIp = ip;
        mReceiverIP = PreferencesHelper.getInstance(getApplicationContext()).hostIp(mHostUniqueId);
        mReceiverPort = PreferencesHelper.getInstance(getApplicationContext()).hostPort(mHostUniqueId);
        isTwoWay = PreferencesHelper.getInstance(getApplicationContext()).receiverCommunication(mHostUniqueId);
        if (isTwoWay) {
            new connectToReceiver().execute();
        }
        YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Connected to: %s / %s ", name, mHostUniqueId);
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

    /* Moved back to "stateless" to fix issue some receivers are having */
    private class sendIscpCommand extends AsyncTask<String, String, Void> {
        @Override
        protected Void doInBackground(String... message) {
            EiscpConnector eiscpConnector;
            try {
                eiscpConnector = new EiscpConnector(mReceiverIP, Integer.parseInt(mReceiverPort));
                eiscpConnector.sendIscpCommand(message[0]);
                eiscpConnector.close();
            } catch (Exception e) {
                YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when sending command", e);
            }
            return null;
        }
    }

    private EiscpListener eiscpListener = new EiscpListener() {
        @Override
        public void receivedIscpMessage(String message) {
            String command = message.substring(0, 3);
            String parameter = message.substring(3);
            YatseLogger.getInstance(getApplicationContext()).logVerbose(TAG, "Receiving message");
            lastReceivedValues.put(command, parameter);
        }

        @Override
        public void disconnected() {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignore) {
                }
                conn = null;
            }
        }
    };

    /* I have to do async since I cannot initialize "EiscpConnector" on the main thread */
    private class connectToReceiver extends AsyncTask<String, String, Void> {
        @Override
        protected Void doInBackground(String... message) {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignore) {
                }
                conn = null;
            }
            try {
                conn = new EiscpConnector(mReceiverIP, Integer.parseInt(mReceiverPort));
            } catch (Exception e) {
                YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when connecting: %s", e.getMessage());
            }
            if (conn != null) {
                try {
                    /* this runs once, spawns the "loop" which tracks receiver */
                    conn.addListener(eiscpListener);
                    conn.sendIscpCommand(EiscpConnector.SYSTEM_POWER_QUERY);
                    conn.sendIscpCommand(EiscpConnector.MUTE_QUERY);
                    conn.sendIscpCommand(EiscpConnector.MASTER_VOL_QUERY);
                } catch (Exception ex) {
                    YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when adding listener: %s", ex.getMessage());
                }
            }
            if (conn != null && message != null && message.length > 0 && !TextUtils.isEmpty(message[0])) {
                try {
                    conn.sendIscpCommand(message[0]);
                } catch (Exception e) {
                    YatseLogger.getInstance(getApplicationContext()).logError(TAG, "Error when sending command: %s", e.getMessage());
                }
            }
            return null;
        }
    }




}
