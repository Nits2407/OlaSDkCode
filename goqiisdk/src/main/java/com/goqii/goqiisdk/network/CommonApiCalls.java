package com.goqii.goqiisdk.network;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.goqii.goqiisdk.ble.BleManager;
import com.goqii.goqiisdk.ble.CommandFiles.CommandSendRequest;
import com.goqii.goqiisdk.ble.ResponseCallbacks;
import com.goqii.goqiisdk.database.DatabaseHandler;
import com.goqii.goqiisdk.model.TemperatureModel;
import com.goqii.goqiisdk.network.ApiModels.BaseResponse;
import com.goqii.goqiisdk.network.ApiModels.BaseResponseData;
import com.goqii.goqiisdk.network.ApiModels.PoolingData;
import com.goqii.goqiisdk.network.ApiModels.PoolingResponse;
import com.goqii.goqiisdk.network.ApiModels.TrackerSettingData;
import com.goqii.goqiisdk.network.ApiModels.TrackerSettingResponse;
import com.goqii.goqiisdk.util.BleUtilStatus;
import com.goqii.goqiisdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Response;

public class CommonApiCalls {
    private static long lastHrApiCallingTime;
    private static long lastBatteryApiCallingTime;
    private static long lastPoolingApiCallingTime;


    public static void CallSettingsApi(final Context context, final ResponseCallbacks responseCallbacks) {
        if (Utils.isNetworkAvailable(context)) {
            Map<String, Object> query = NetworkManager.getInstance(context).getDefaultQueryMap(context);
            query.put("trackerMacId", BleManager.getInstance().getGoqiiTrackerMacID());
            query.put("profile", Utils.getIdentityObject(context));
            query.put("phoneIdentity", Utils.getPhoneIdentityObject(context));
            NetworkManager.getInstance(context).request(query, NetworkManager.REQUEST.FETCH_TRACKER_SETTINGS,
                    new NetworkManager.RequestCallback() {
                        @Override
                        public void onSuccess(NetworkManager.REQUEST type, Response response) {

                            TrackerSettingResponse trackerSettingResponse = (TrackerSettingResponse) response.body();
                            if (trackerSettingResponse.getCode() == 200) {
                                Utils.saveIntPreferences(context, Utils.IS_SETTINGS_API, 1);
                                TrackerSettingData trackerSettingData = trackerSettingResponse.getData();
                                Utils.saveStringPreferences(context, Utils.APICALLTIME, trackerSettingData.getApiCallTimeInSeconds());
                                Utils.saveStringPreferences(context, Utils.APIMINCALLTIME, trackerSettingData.getApiCallMinTimeInSeconds());

                                Utils.saveStringPreferences(context, Utils.APIMAXCALLTIME, trackerSettingData.getApiCallMaxTimeInSeconds());
                                Utils.saveStringPreferences(context, Utils.BATTERYAPICALLTIME, trackerSettingData.getBatteryApiCallTime());
                                Utils.saveStringPreferences(context, Utils.BATTERSTATUSALERTPERCENTAGE, trackerSettingData.getBatterStatusAlertPercentage());
                                Utils.saveStringPreferences(context, Utils.PREVIOUSTRACKERID, trackerSettingData.getPreviousTrackerId());
                                Utils.saveStringPreferences(context, Utils.TRACKERNOTWEARALERTTIME, trackerSettingData.getTrackerNotWearAlertTime());
                                Utils.saveStringPreferences(context, Utils.BASE_URL, trackerSettingData.getApiBaseUrl());
                                Utils.saveStringPreferences(context, Utils.FETCHALERTTIME, trackerSettingData.getFetchAlertTime());
                                Utils.saveStringPreferences(context, Utils.DEBUG_LEVEL, trackerSettingData.getDebugLevel());
                                Gson gson = new Gson();
                                String json = gson.toJson(trackerSettingData.getSearchDeviceName());
                                Utils.saveStringPreferences(context, Utils.DEVICENAME, json);
                                TrackerSettingResponse.saveEndPoints(context, trackerSettingResponse.getEndpoints());
                                if (responseCallbacks != null)
                                    responseCallbacks.getApiResult(true, type);
                            }
                        }

                        @Override
                        public void onFailure(NetworkManager.REQUEST type) {
                            Log.e("Data Send result", "Failed");
                        }

                    });
        }
    }

    public static void CallUpdateStatus(final Context context, final ResponseCallbacks responseCallbacks, final String status) {
        String macAdress = BleManager.getInstance().getGoqiiTrackerMacID();
        updateStatusProcedure(context, status);
        if (status.equalsIgnoreCase("logout") || status.equalsIgnoreCase("disconnect")) {
            BleManager.getInstance().getAllPairedDevices(macAdress);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BleManager.getInstance().disconnectDevice();
                    BleManager.getInstance().makeDisconnect();
                }
            }, 2000);

        }
        if (Utils.isNetworkAvailable(context)) {
            Map<String, Object> query = NetworkManager.getInstance(context).getDefaultQueryMap(context);
            query.put("trackerMacId", macAdress);
            query.put("profile", Utils.getIdentityObject(context));
            query.put("phoneIdentity", Utils.getPhoneIdentityObject(context));
            query.put("status", status);
            NetworkManager.getInstance(context).request(query, NetworkManager.REQUEST.UPDATE_STATUS,
                    new NetworkManager.RequestCallback() {
                        @Override
                        public void onSuccess(NetworkManager.REQUEST type, Response response) {
                            BaseResponse baseResponse = (BaseResponse) response.body();
                            if (baseResponse.getCode() == 200) {
                                if (responseCallbacks != null && !status.equalsIgnoreCase("onduty"))
                                    responseCallbacks.getApiResult(true, type);
                            } else if (responseCallbacks != null)
                                responseCallbacks.getApiResult(false, type);
                        }

                        @Override
                        public void onFailure(NetworkManager.REQUEST type) {
                            Log.e("Data Send result", "Failed");
                        }
                    });
        }
    }

    private static void updateStatusProcedure(Context context, String status) {
        Utils.saveStringPreferences(context, Utils.DRIVER_STATUS, status);
        if (status.equalsIgnoreCase("logout") || status.equalsIgnoreCase("disconnect")) {
            CommandSendRequest.switchHeartRateClicked(false);
            CommandSendRequest.setRealTimeMode(false);
            Utils.saveStringPreferences(context, Utils.MACADDRESS, "");
            Utils.saveStringPreferences(context, Utils.TEMP_MACADDRESS, "");
            Utils.saveIntPreferences(context, Utils.IS_SETTINGS_API, 0);
            Utils.saveBooleanPreferences(context, Utils.IS_SYNC_ON, false);
            Utils.saveBooleanPreferences(context, Utils.IS_LINKED, false);
            CallHrTemperatureApi(context);
            //BleManager.getInstance().turnOffBluetooth();
        } else if (status.equalsIgnoreCase("offduty")) {
            //CommandSendRequest.switchHeartRateClicked(false);
            BleManager.getInstance().disconnectDevice();
            CallHrTemperatureApi(context);
        } else if (status.equalsIgnoreCase("onduty")) {
            if (BleManager.getInstance().isMacIdAvailable()) {
                BleManager.getInstance().serviceInitialisation();
                if (!BleManager.getInstance().isConnected())
                    BleManager.getInstance().connectDevice();
                else
                    Toast.makeText(context, "Tracker Already connected", Toast.LENGTH_SHORT).show();
            } else
                BleManager.getInstance().startScan(null);
        } else if (status.equalsIgnoreCase("connect")) {
            Utils.saveStringPreferences(context, Utils.DRIVER_STATUS, "onduty");
            BleManager.getInstance().startScan(null);
        }

    }

    private static void fetchPoolingMechanism(final Context context) {
        if (Utils.isNetworkAvailable(context)) {
            long currentTime = System.currentTimeMillis();
            long diff = currentTime - lastPoolingApiCallingTime;
            final String status = (String) Utils.getPreferences(context, Utils.DRIVER_STATUS, Utils.PREFTYPE_STRING);
            String apiCallingTime = (String) Utils.getPreferences(context, Utils.FETCHALERTTIME, Utils.PREFTYPE_STRING);
            if ((diff >= Long.parseLong(apiCallingTime) * 1000) && BleManager.getInstance().isConnected()) {
                lastPoolingApiCallingTime = currentTime;
                Map<String, Object> query = NetworkManager.getInstance(context).getDefaultQueryMap(context);
                query.put("trackerMacId", BleManager.getInstance().getGoqiiTrackerMacID());
                query.put("profile", Utils.getIdentityObject(context));
                query.put("phoneIdentity", Utils.getPhoneIdentityObject(context));
                NetworkManager.getInstance(context).request(query, NetworkManager.REQUEST.FETCH_ALERT,
                        new NetworkManager.RequestCallback() {
                            @Override
                            public void onSuccess(NetworkManager.REQUEST type, Response response) {
                                PoolingResponse poolingResponse = (PoolingResponse) response.body();
                                if (poolingResponse.getCode() == 200) {
                                    if (!status.equalsIgnoreCase("logout") && !status.equalsIgnoreCase("disconnect")) {
                                        Utils.saveStringPreferences(context, Utils.HEARTRATE, poolingResponse.getCurrentHR());
                                        Utils.saveStringPreferences(context, Utils.TEMPERATURE, poolingResponse.getCurrentTemperature());
                                        ArrayList<PoolingData> poolingDataList = poolingResponse.getData();
                                        handleAllAlerts(context, poolingDataList);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(NetworkManager.REQUEST type) {
                                Log.e("Data Send result", "Failed");
                            }

                        });
            }
        }
    }

    private static void handleAllAlerts(Context context, ArrayList<PoolingData> poolingDataList) {
        for (int i = 0; i < poolingDataList.size(); i++) {
            PoolingData poolingData = poolingDataList.get(i);
            String alertCode = poolingData.getAlertCode();
            String alertType = poolingData.getAlertType();
            String severity = poolingData.getSeverity();

            if (!alertCode.equalsIgnoreCase("0"))
                BleUtilStatus.sendBandStatusWithData(context, alertType, severity, Integer.parseInt(alertCode));
        }
    }

    public static void CallUpdateTrackerStatus(final Context context, String trackerSetting) {
        JSONObject settingObject;
        String modifiedTime = "0";
        try {
            settingObject = new JSONObject(trackerSetting);
            modifiedTime = settingObject.getString("apiCallTime");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String apiMaxCallingTime = (String) Utils.getPreferences(context, Utils.APIMAXCALLTIME, Utils.PREFTYPE_STRING);
        String apiMinCallingTime = (String) Utils.getPreferences(context, Utils.APIMINCALLTIME, Utils.PREFTYPE_STRING);

        if (Integer.parseInt(apiMinCallingTime) < Integer.parseInt(modifiedTime) || Integer.parseInt(apiMaxCallingTime) > Integer.parseInt(modifiedTime))
            Utils.saveStringPreferences(context, Utils.APICALLTIME, modifiedTime);
        else if (Integer.parseInt(apiMinCallingTime) > Integer.parseInt(modifiedTime))
            Utils.saveStringPreferences(context, Utils.APICALLTIME, apiMinCallingTime);
        else if (Integer.parseInt(apiMaxCallingTime) < Integer.parseInt(modifiedTime))
            Utils.saveStringPreferences(context, Utils.APICALLTIME, apiMaxCallingTime);

        if (Utils.isNetworkAvailable(context)) {
            Map<String, Object> query = NetworkManager.getInstance(context).getDefaultQueryMap(context);
            query.put("trackerMacId", BleManager.getInstance().getGoqiiTrackerMacID());
            query.put("profile", Utils.getIdentityObject(context));
            query.put("phoneIdentity", Utils.getPhoneIdentityObject(context));
            query.put("settingsChange", trackerSetting);
            NetworkManager.getInstance(context).request(query, NetworkManager.REQUEST.UPDATE_TRACKER_STATUS,
                    new NetworkManager.RequestCallback() {
                        @Override
                        public void onSuccess(NetworkManager.REQUEST type, Response response) {
                            BaseResponse baseResponse = (BaseResponse) response.body();
                        }

                        @Override
                        public void onFailure(NetworkManager.REQUEST type) {
                            Log.e("Data Send result", "Failed");
                        }

                    });
        }
    }

    public static void CallIdentityApi(final Context context, final ResponseCallbacks responseCallbacks) {
        if (Utils.isNetworkAvailable(context)) {
            //Utils.showProgressDialog(context);
            Map<String, Object> query = NetworkManager.getInstance(context).getDefaultQueryMap(context);
            query.put("trackerMacId", BleManager.getInstance().getGoqiiTrackerMacID());
            query.put("profile", Utils.getIdentityObject(context));
            query.put("phoneIdentity", Utils.getPhoneIdentityObject(context));
            NetworkManager.getInstance(context).request(query, NetworkManager.REQUEST.PARTNER_IDENTITY,
                    new NetworkManager.RequestCallback() {
                        @Override
                        public void onSuccess(NetworkManager.REQUEST type, Response response) {
                            BaseResponse baseResponse = (BaseResponse) response.body();
                            if (baseResponse.getCode() == 200) {
                                if (responseCallbacks != null)
                                    responseCallbacks.getApiResult(true, type);
                                if (BleManager.getInstance().isConnected())
                                    BleManager.getInstance().makeDisconnect();

                                BleManager.getInstance().connectDevice();
                            }

                        }

                        @Override
                        public void onFailure(NetworkManager.REQUEST type) {
                            Log.e("Data Send result", "Failed");
                        }

                    });
        }
    }

    public static void CallBatteryApi(final Context context, String battery) {
        if (Utils.isNetworkAvailable(context)) {
            Map<String, Object> query = NetworkManager.getInstance(context).getDefaultQueryMap(context);
            query.put("trackerMacId", BleManager.getInstance().getGoqiiTrackerMacID());
            query.put("profile", Utils.getIdentityObject(context));
            query.put("phoneIdentity", Utils.getPhoneIdentityObject(context));
            query.put("battery", battery);
            NetworkManager.getInstance(context).request(query, NetworkManager.REQUEST.BATTERY_STATUS,
                    new NetworkManager.RequestCallback() {
                        @Override
                        public void onSuccess(NetworkManager.REQUEST type, Response response) {
                            BaseResponse baseResponse = (BaseResponse) response.body();
                            if (baseResponse.getCode() == 200) {
                                BaseResponseData baseResponseData = baseResponse.getData();
                                Utils.printLog("e", "Message", baseResponseData.getMessage());
                            }
                        }

                        @Override
                        public void onFailure(NetworkManager.REQUEST type) {
                            Log.e("Data Send result", "Failed");
                        }

                    });
        }
    }

    public static void CallHrTemperatureApi(final Context context) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastHrApiCallingTime;
        String apiCallingTime = (String) Utils.getPreferences(context, Utils.APICALLTIME, Utils.PREFTYPE_STRING);
        final String status = (String) Utils.getPreferences(context, Utils.DRIVER_STATUS, Utils.PREFTYPE_STRING);
        fetchPoolingMechanism(context);
        sendBatteryCommand(context);
        if (diff >= Long.parseLong(apiCallingTime) * 1000 || status.equalsIgnoreCase("logout")
                || status.equalsIgnoreCase("disconnect")) {
            lastHrApiCallingTime = currentTime;
            Utils.printLog("e", "Temperature API", "Called");
            ArrayList<TemperatureModel> tempList = DatabaseHandler.getInstance(context).getAllNewRecords();
            if (status.equalsIgnoreCase("logout")) {
                DatabaseHandler.getInstance(context).clearAllData();
                Utils.saveStringPreferences(context, Utils.HEARTRATE, "");
                Utils.saveStringPreferences(context, Utils.TEMPERATURE, "");
                Utils.saveIntPreferences(context, Utils.BATTERY_POWER, 0);
            }
            if (tempList.size() > 0) {
                if (Utils.isNetworkAvailable(context)) {
                    Gson gson = new Gson();
                    String json = gson.toJson(tempList);
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("data",json);
                        obj.put("trackerMacId",BleManager.getInstance().getGoqiiTrackerMacID());
                        obj.put("profile",Utils.getIdentityObject(context));
                        obj.put("phoneIdentity",Utils.getPhoneIdentityObject(context));
                        obj.put("organizationId",Utils.getPreferences(context,Utils.GOQii_ACCOUNT_ID,Utils.PREFTYPE_STRING));
                        obj.put("nonce", Utils.getPreferences(context,Utils.GOQii_ACCOUNT_ID,Utils.PREFTYPE_STRING));
                        //obj.put("organizationApiKey", "f988n87erhv3y94154qj4uy4l");//Live
                        obj.put("organizationApiKey", "zavj59zhx4fhm2yjd344bachx");// Demo
                        obj.put("signature", Utils.createSignature(context));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Utils.writeToFile(context, obj.toString(), DatabaseHandler.TABLE_TEMPERATURE);
                }
            }
        }
    }

    private static void sendBatteryCommand(Context context) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - lastBatteryApiCallingTime;
        String apiCallingTime = (String) Utils.getPreferences(context, Utils.BATTERYAPICALLTIME, Utils.PREFTYPE_STRING);
        if ((diff >= Long.parseLong(apiCallingTime) * 1000) && BleManager.getInstance().isConnected()) {
            lastBatteryApiCallingTime = currentTime;
            CommandSendRequest.getBandBatteryStatus();
        }
    }

}