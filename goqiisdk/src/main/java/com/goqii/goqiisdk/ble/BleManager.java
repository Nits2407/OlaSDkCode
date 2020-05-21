package com.goqii.goqiisdk.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.goqii.goqiisdk.ble.CommandFiles.CommandSendRequest;
import com.goqii.goqiisdk.database.DatabaseHandler;
import com.goqii.goqiisdk.network.CommonApiCalls;
import com.goqii.goqiisdk.util.BleUtilStatus;
import com.goqii.goqiisdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class BleManager {
    private static BleManager ourInstance;
    private String address;
    private BleService bleService;
    private static final long SCAN_PERIOD = 10000;
    private Intent serviceIntent;
    private BluetoothAdapter bluetoothAdapter;
    private static Context mContext;
    private Handler mHandler = new Handler();
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private ArrayList<ScanFilter> filters;
    private ArrayList<String> deviceList;
    private ResponseCallbacks responseCallbacks;
    private boolean isDeviceFound;

    private BleManager(final Context context, JSONObject olaJsonObject) {
        mContext = context.getApplicationContext();
        Utils.saveStringPreferences(mContext, Utils.PROFILE_DATA, olaJsonObject.toString());
        if (serviceIntent == null) {
            serviceIntent = new Intent(mContext, BleService.class);
            mContext.startService(serviceIntent);
            // TODO Auto-generated method stub
            ServiceConnection serviceConnection = new ServiceConnection() {

                @Override
                public void onServiceDisconnected(ComponentName name) {// TODO Auto-generated method stub
                    bleService = null;
                }

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    BleService.LocalBinder binder = (BleService.LocalBinder) service;
                    bleService = binder.getService();
                    if (!TextUtils.isEmpty(address)) {
                        bleService.initBluetoothDevice(address, mContext);
                    }
                }
            };
            mContext.bindService(serviceIntent, serviceConnection,
                    Service.BIND_AUTO_CREATE);
        }
        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(mContext);
        databaseHandler.getWritableDatabase();
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        iniitiateLeScanner();
        Utils.getCurrentVersion(mContext);
        Utils.getPhoneIdentity(mContext);
    }

    private void iniitiateLeScanner() {
        isDeviceFound = false;
        if (mLEScanner == null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mLEScanner = bluetoothAdapter.getBluetoothLeScanner();
            }
    }

    public static void init(Context context, JSONObject olaJsonObject) {
        if (ourInstance == null) {
            synchronized (BleManager.class) {
                if (ourInstance == null) {
                    ourInstance = new BleManager(context, olaJsonObject);
                }
            }
            int isSettingsApi = (int) Utils.getPreferences(mContext, Utils.IS_SETTINGS_API, Utils.PREFTYPE_INT);
            if (isSettingsApi == 0)
                BleManager.getInstance().callSettingsApi(null);
//            else
//                responseCallbacks.getApiResult(true, NetworkManager.REQUEST.FETCH_TRACKER_SETTINGS);
        }
    }

    public boolean isBleEnable() {
        return bluetoothAdapter.isEnabled();
    }

    public boolean isBluetoothAvailable() {

        return (bluetoothAdapter != null &&
                isBleEnable() &&
                bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON);
    }

    public static BleManager getInstance() {
        try {
            if (ourInstance == null) {
                synchronized (BleManager.class) {
                    if (ourInstance == null) {
                        try {
                            JSONObject jsonObject = new JSONObject(Utils.getIdentityObject(mContext));
                            ourInstance = new BleManager(mContext, jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        } catch (Exception e) {
            try {
                JSONObject jsonObject = new JSONObject(Utils.getIdentityObject(mContext));
                ourInstance = new BleManager(mContext, jsonObject);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            Utils.printStackTrace(e);
        }
        return ourInstance;
    }

    public void connectDevice() {
        String address = (String) Utils.getPreferences(mContext, Utils.TEMP_MACADDRESS, Utils.PREFTYPE_STRING);
        if (!bluetoothAdapter.isEnabled() || TextUtils.isEmpty(address) || isConnected()) return;

        if (bleService == null) {
            this.address = address;
        } else {
            bleService.initBluetoothDevice(address, mContext);
        }
    }

    public void writeValue(byte[] value) {
        if (bleService == null || ourInstance == null || !isConnected()) return;
        bleService.writeValue(value);
    }

    public void disconnectDevice() {
        if (bleService == null) return;
        bleService.disconnect();
        stopBLEService();
    }

    private void stopBLEService() {
        try {
            //stopConnectTimer();
            Intent service = new Intent(mContext, BleService.class);
            mContext.stopService(service);

            if (ourInstance != null)
                ourInstance.CleanUpNow();
        } catch (Exception e) {
            Utils.printStackTrace(e);
        }
    }

    private void CleanUpNow() {
        if (bleService != null) {
            bleService.cleanUp();
            //isServiceConnected = false;
        }
    }

    public void unpairDevice(BluetoothDevice device) {
        try {
            Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method removeBondMethod = btClass.getMethod("removeBond");
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(device.getAddress());
            boolean invoke = (boolean) removeBondMethod.invoke(bluetoothDevice);
            Utils.printLog("e", "Pair Status:", "" + invoke);
        } catch (Exception e) {
            Utils.printStackTrace(e);
        }
    }

    public boolean createBond(BluetoothDevice btDevice) {
        try {
            Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
            Method createBondMethod = class1.getMethod("createBond");
            return (Boolean) createBondMethod.invoke(btDevice);
        } catch (Exception e) {
            Utils.printStackTrace(e);
        }
        return false;
    }

    private Handler bondReqHandler;
    private Runnable bondReqRunnable;

    public void bondRequestTimer() {

        try {
            bondReqHandler = new Handler(Looper.getMainLooper());
            bondReqRunnable = new Runnable() {
                @Override
                public void run() {
                    cancelHandler();
                    String macAddress = (String) Utils.getPreferences(mContext, Utils.MACADDRESS, Utils.PREFTYPE_STRING);
                    Utils.saveStringPreferences(mContext, Utils.MACADDRESS, "");
                    Utils.saveStringPreferences(mContext, Utils.TEMP_MACADDRESS, "");
                    Utils.saveIntPreferences(mContext, Utils.IS_SETTINGS_API, 0);
                    //Utils.saveBooleanPreferences(mContext, Utils.IS_SYNC_ON, false);
                    BleManager.getInstance().getAllPairedDevices(macAddress);
                    BleManager.getInstance().disconnectDevice();
                    BleManager.getInstance().startScan(null);
                    BleUtilStatus.sendBandStatusWithData(mContext, "You have not tapped the band.\n Please try sending link request again.", "", 1406);
                    BleUtilStatus.sendBandStatusWithData(mContext, "", "", 1409);
                }
            };
            int BOND_REQ_PERIOD = 10000;
            bondReqHandler.postDelayed(bondReqRunnable, BOND_REQ_PERIOD);
        } catch (Exception e) {
            Utils.printStackTrace(e);
        }
    }

    public void cancelHandler() {

        if (bondReqHandler != null)
            bondReqHandler.removeCallbacks(bondReqRunnable);
    }

    public boolean isConnected() {
        if (bleService == null) return false;
        return bleService.isConnected();
    }

    public void callIdentityApi(ResponseCallbacks responseCallbacks) {
        CommonApiCalls.CallIdentityApi(mContext, responseCallbacks);
    }

    public void callSettingsApi(ResponseCallbacks responseCallbacks) {
        CommonApiCalls.CallSettingsApi(mContext, responseCallbacks);
    }

    public void updateStatus(String status) {
        try {
            CommonApiCalls.CallUpdateStatus(mContext, responseCallbacks, status);
        } catch (Exception e) {
            Utils.printStackTrace(e);
        }
    }

    public void updateTrackerSettings(String trackerSettings) {
        try {
            CommonApiCalls.CallUpdateTrackerStatus(mContext, trackerSettings);
        } catch (Exception e) {
            Utils.printStackTrace(e);
        }
    }

    public int getBatteryStatus() {
        return (int) Utils.getPreferences(mContext, Utils.BATTERY_POWER, Utils.PREFTYPE_INT);
    }

    public String getCurrentDriverStatus() {
        String status = (String) Utils.getPreferences(mContext, Utils.DRIVER_STATUS, Utils.PREFTYPE_STRING);
        if (TextUtils.isEmpty(status) || status.equalsIgnoreCase("logOut"))
            status = "logout";
        else if (isConnected())
            status = "onDuty";
        else if (!isConnected())
            status = "offduty";
        return status;
    }

    public String getBodyTemperature() {
        if (TextUtils.isEmpty((String) Utils.getPreferences(mContext, Utils.POOLING_TEMPERATURE, Utils.PREFTYPE_STRING)))
            return (String) Utils.getPreferences(mContext, Utils.TEMPERATURE, Utils.PREFTYPE_STRING);
        else
            return (String) Utils.getPreferences(mContext, Utils.TEMPERATURE, Utils.PREFTYPE_STRING);
    }

    public String getHeartRate() {
        if (TextUtils.isEmpty((String) Utils.getPreferences(mContext, Utils.POOLING_HEARTRATE, Utils.PREFTYPE_STRING)))
            return (String) Utils.getPreferences(mContext, Utils.HEARTRATE, Utils.PREFTYPE_STRING);
        else
            return (String) Utils.getPreferences(mContext, Utils.HEARTRATE, Utils.PREFTYPE_STRING);
    }

    public String getGoqiiTrackerMacID() {
        if (TextUtils.isEmpty((String) Utils.getPreferences(mContext, Utils.MACADDRESS, Utils.PREFTYPE_STRING)))
            return (String) Utils.getPreferences(mContext, Utils.TEMP_MACADDRESS, Utils.PREFTYPE_STRING);
        else
            return (String) Utils.getPreferences(mContext, Utils.MACADDRESS, Utils.PREFTYPE_STRING);
    }

    public void startScan(ResponseCallbacks responseCallbacks) {
        String status = (String) Utils.getPreferences(mContext, Utils.DRIVER_STATUS, Utils.PREFTYPE_STRING);
        if (responseCallbacks != null)
            this.responseCallbacks = responseCallbacks;
        if (!status.equalsIgnoreCase("skipPairing")) {
            if (!isMacIdAvailable()) {
                Gson gson = new Gson();
                String devices = (String) Utils.getPreferences(mContext, Utils.DEVICENAME, Utils.PREFTYPE_STRING);
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                deviceList = gson.fromJson(devices, type);
                if (deviceList == null) {
                    deviceList = new ArrayList<>();
                    deviceList.add("goqii vital 3");
                    deviceList.add("goqii vital 3t");
                }
                if (Build.VERSION.SDK_INT >= 21) {
                    settings = new ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build();
                    filters = new ArrayList<>();
                }
                scanLeDevice(true);
            } else
                connectDevice();
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {

                }
            };

    private void scanLeDevice(final boolean enable) {
        if (isBleEnable()) {
            if (enable) {
                iniitiateLeScanner();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isBluetoothAvailable()) {
                            if (Build.VERSION.SDK_INT < 21) {
                                bluetoothAdapter.stopLeScan(mLeScanCallback);
                            } else {
                                String address = (String) Utils.getPreferences(mContext, Utils.TEMP_MACADDRESS, Utils.PREFTYPE_STRING);
                                Utils.printLog("e", "Callback", address);
                                if (!isDeviceFound)
                                    BleUtilStatus.sendBandStatus(mContext, 1408);
                                mLEScanner.stopScan(mScanCallback);
                            }
                        }
                    }
                }, SCAN_PERIOD);
                if (Build.VERSION.SDK_INT < 21) {
                    bluetoothAdapter.startLeScan(mLeScanCallback);
                } else {
                    mLEScanner.startScan(filters, settings, mScanCallback);
                }
            } else {
                if (Build.VERSION.SDK_INT < 21) {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                } else {
                    mLEScanner.stopScan(mScanCallback);
                }
            }
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            String deviceName = btDevice.getName();

            if (TextUtils.isEmpty(deviceName))
                deviceName = "Unknown Device";

            if (isDeviceAvailable(deviceName)) {
                Utils.saveStringPreferences(mContext, Utils.TEMP_MACADDRESS, btDevice.getAddress());
                isDeviceFound = true;
                //Utils.updateIdentityObject(mContext, btDevice.getAddress());
                scanLeDevice(false);
                mHandler.removeCallbacksAndMessages(null);
                callIdentityApi(responseCallbacks);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private boolean isDeviceAvailable(String deviceName) {
        for (String name : deviceList) {
            if (name.equalsIgnoreCase(deviceName))
                return true;
        }
        return false;
    }

    public boolean isMacIdAvailable() {
        String address = (String) Utils.getPreferences(mContext, Utils.MACADDRESS, Utils.PREFTYPE_STRING);
        return !TextUtils.isEmpty(address);
    }

    public String getMacIdBase64() {
        String address = (String) Utils.getPreferences(mContext, Utils.MACADDRESS, Utils.PREFTYPE_STRING);
        byte[] data = address.getBytes(StandardCharsets.UTF_8);
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        return base64;
    }

    public void startDataReading() {
        CommandSendRequest.setDateTimeToBand();
        CommandSendRequest.getBandBatteryStatus();
        CommandSendRequest.switchHeartRateClicked(true);
        CommandSendRequest.setRealTimeMode(true);
    }

    public void updateSyncingStatus(String status) {
        if (status.equalsIgnoreCase("syncOn")) {
            Utils.saveBooleanPreferences(mContext, Utils.IS_SYNC_ON, true);
        } else if (status.equalsIgnoreCase("syncOff")) {
            Utils.saveBooleanPreferences(mContext, Utils.IS_SYNC_ON, false);
        }
    }

    public void getAllPairedDevices(String macAddress) {
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        List<BluetoothDevice> pairedDevices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
        for (BluetoothDevice bt : pairedDevices) {
            if (bt.getAddress().equalsIgnoreCase(macAddress)) {
                unpairDevice(bt);
            }
        }
    }

    public void stopScanning() {
        scanLeDevice(false);
    }

    public void setResponseCallbacks(ResponseCallbacks responseCallbacks) {
        this.responseCallbacks = responseCallbacks;
    }

    public void turnOffBluetooth() {
        bluetoothAdapter.disable();
    }
}
