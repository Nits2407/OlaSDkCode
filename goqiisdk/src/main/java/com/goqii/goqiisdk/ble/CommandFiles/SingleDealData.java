package com.goqii.goqiisdk.ble.CommandFiles;

import android.content.Context;

import com.goqii.goqiisdk.ble.BleManager;
import com.goqii.goqiisdk.database.DatabaseHandler;
import com.goqii.goqiisdk.network.CommonApiCalls;
import com.goqii.goqiisdk.util.BleUtilStatus;
import com.goqii.goqiisdk.util.DeviceKey;
import com.goqii.goqiisdk.util.Utils;

import java.util.HashMap;
import java.util.Map;


public class SingleDealData {

    public static void receiveUpdateValue(byte[] value, final Context mContext) {
        Map<String, String> map = new HashMap<>();
        if (mContext != null) {
            switch (value[0]) {
                case DeviceConst.CMD_START_REAL_TIME:
                    String[] activityData = ResolveUtil.getActivityData(value);
                    if (activityData != null) {
                        map.put(DeviceKey.KTotalSteps, activityData[0]);
                        map.put(DeviceKey.KCalories, activityData[1]);
                        map.put(DeviceKey.KDistance, activityData[2]);
                        map.put(DeviceKey.KSportTime, activityData[3]);
                        map.put(DeviceKey.KHeartValue, activityData[4]);
                        map.put(DeviceKey.KTemperature, activityData[5]);
                        Utils.printLog("e", "HeartRate", activityData[4]);
                        Utils.printLog("e", "Temperature", activityData[5]);
                        Utils.saveStringPreferences(mContext, Utils.HEARTRATE, activityData[4]);
                        Utils.saveStringPreferences(mContext, Utils.TEMPERATURE, activityData[5]);
                        final String status = (String) Utils.getPreferences(mContext, Utils.DRIVER_STATUS, Utils.PREFTYPE_STRING);
                        boolean isSyncOn = (boolean) Utils.getPreferences(mContext, Utils.IS_SYNC_ON, Utils.PREFTYPE_BOOLEAN);
                        boolean isLinked = (boolean) Utils.getPreferences(mContext, Utils.IS_LINKED, Utils.PREFTYPE_BOOLEAN);
                        if (!(status.equalsIgnoreCase("logout")
                                || status.equalsIgnoreCase("disconnect")
                                || status.equalsIgnoreCase("offduty")) && isSyncOn && isLinked) {
                            BleUtilStatus.sendBandStatus(mContext, 1101);
                            long localId = DatabaseHandler.getInstance(mContext).insertTemperature(mContext);
                            Utils.printLog("e", "LocalId:", "" + localId);
                            CommonApiCalls.CallHrTemperatureApi(mContext);
                        }

                    }
                    break;
                case DeviceConst.CMD_Get_BatteryLevel:
                    String battery = ResolveUtil.getDeviceBattery(value);
                    Utils.saveIntPreferences(mContext, Utils.BATTERY_POWER, Integer.parseInt(battery));
                    map.put(DeviceKey.KBattery, battery);
                    boolean isLinked = (boolean) Utils.getPreferences(mContext, Utils.IS_LINKED, Utils.PREFTYPE_BOOLEAN);
                    if (isLinked) {
                        CommonApiCalls.CallBatteryApi(mContext, battery);
                        BleUtilStatus.sendBandStatus(mContext, 1101);
                    }
                    break;
                case DeviceConst.CMD_RECIEVE_BINDING_REQUEST:
                    Utils.saveBooleanPreferences(mContext, Utils.IS_LINKED, true);
                    BleUtilStatus.sendBandStatusWithData(mContext, "Successful tracker connection", "", 1200);
                    BleManager.getInstance().cancelHandler();
                    //BleManager.getInstance().createBond(BleManager.getInstance().getGoqiiTrackerMacID());
                    //CommonApiCalls.CallUpdateStatus(mContext, null, "Connect");
                    break;

                default:
                    break;
            }
        }
    }

}
