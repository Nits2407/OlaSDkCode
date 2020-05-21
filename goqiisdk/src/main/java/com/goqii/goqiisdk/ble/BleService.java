package com.goqii.goqiisdk.ble;import android.app.Service;import android.bluetooth.BluetoothAdapter;import android.bluetooth.BluetoothDevice;import android.bluetooth.BluetoothGatt;import android.bluetooth.BluetoothGattCallback;import android.bluetooth.BluetoothGattCharacteristic;import android.bluetooth.BluetoothGattDescriptor;import android.bluetooth.BluetoothGattService;import android.bluetooth.BluetoothManager;import android.bluetooth.BluetoothProfile;import android.content.BroadcastReceiver;import android.content.Context;import android.content.Intent;import android.content.IntentFilter;import android.os.Binder;import android.os.IBinder;import android.util.Log;import com.goqii.goqiisdk.ble.CommandFiles.CommandSendRequest;import com.goqii.goqiisdk.ble.CommandFiles.ReceiveBandCommandResponseTask;import com.goqii.goqiisdk.ble.CommandFiles.SendCmdState;import com.goqii.goqiisdk.util.BleUtilStatus;import com.goqii.goqiisdk.util.Utils;import java.lang.reflect.Method;import java.util.LinkedList;import java.util.Queue;import java.util.UUID;public final class BleService extends Service {    private static final String TAG = "BleService";    private static final UUID NOTIY = UUID            .fromString("00002902-0000-1000-8000-00805f9b34fb");    private static final UUID SERVICE_DATA = UUID            .fromString("0000fff0-0000-1000-8000-00805f9b34fb");    private static final UUID DATA_Characteristic = UUID            .fromString("0000fff6-0000-1000-8000-00805f9b34fb");    private static final UUID NOTIY_Characteristic = UUID            .fromString("0000fff7-0000-1000-8000-00805f9b34fb");    private boolean NeedReconnect = false;    public final static String ACTION_GATT_onDescriptorWrite = "com.goqii.goqiisdk.ble.service.onDescriptorWrite";    public final static String ACTION_GATT_DISCONNECTED = "com.goqii.goqiisdk.ble.service.ACTION_GATT_DISCONNECTED";    public final static String ACTION_DATA_AVAILABLE = "com.goqii.goqiisdk.ble.service.ACTION_DATA_AVAILABLE";    public final static String CALLBACK_STATUS = "CALLBACK_STATUS";    private final IBinder kBinder = new LocalBinder();    private BluetoothManager bluetoothManager;    private BluetoothAdapter mBluetoothAdapter;    private BluetoothGatt mGatt;    private boolean isConnected;    private BluetoothReciever blueToothReciever;    private BluetoothGattService service;    private Context mContext;    @Override    public IBinder onBind(Intent intent) {        //initAdapter();        return kBinder;    }    @Override    public void onCreate() {        super.onCreate();        //initAdapter();        //registerBluetoothReceiver();    }    private void registerBluetoothReceiver() {        blueToothReciever = new BluetoothReciever();        IntentFilter filter = new IntentFilter();        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);        mContext.registerReceiver(blueToothReciever, filter);    }    public static BleService getInstance() {        return getService();    }    static BleService getService() {        return new BleService();    }    @Override    public boolean onUnbind(Intent intent) {        return super.onUnbind(intent);    }    private String address;    public void initBluetoothDevice(final String address, final Context context) {        mContext = context.getApplicationContext();        initAdapter();        registerBluetoothReceiver();        this.address = address;        final BluetoothDevice device = mBluetoothAdapter                .getRemoteDevice(address);        // TODO Auto-generated method stub        if (isConnected()) return;        closeGatt();        mGatt = device.connectGatt(context, false, bleGattCallback);        if (mGatt == null) {            System.out.println(device.getAddress() + "gatt is null");        }    }    private void reconnect(boolean enable) {        Log.i(TAG, "reconnect: " + enable);        if (!mBluetoothAdapter.isEnabled()) {            //BleUtilStatus.sendBandStatusWithData(BleService.this, "Tracker Disconnected", "", 1405);            //broadcastUpdate(ACTION_GATT_DISCONNECTED);            return;        }        if (BleManager.getInstance().isMacIdAvailable())            mGatt = mBluetoothAdapter.getRemoteDevice(address).connectGatt(this, false,                    bleGattCallback);    }    private void initAdapter() {        if (bluetoothManager == null) {            bluetoothManager = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);            if (bluetoothManager == null) {                return;            }        }        mBluetoothAdapter = bluetoothManager.getAdapter();    }    public void disconnect() {        NeedReconnect = false;        if (mGatt == null)            return;        mGatt.disconnect();    }    public void makeDisconnectFlag() {        isConnected = false;    }    class LocalBinder extends Binder {        BleService getService() {            return BleService.this;        }    }    private BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {        @Override        public void onConnectionStateChange(BluetoothGatt gatt, int status,                                            int newState) {            Log.i(TAG, "onConnectionStateChange:  status" + status + " newstate " + newState);            if (newState == BluetoothProfile.STATE_CONNECTED) {                if (status == 133) {                    closeGatt();                    reconnect(true);                    return;                }                try {                    gatt.discoverServices();                } catch (Exception e) {                    // TODO: handle exception                    e.printStackTrace();                }            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {                isConnected = false;                Log.i(TAG, "onConnectionStateChange: " + ACTION_GATT_DISCONNECTED);                refreshDeviceCache(mGatt);                queues.clear();                if (!NeedReconnect) {                    BleUtilStatus.sendBandStatusWithData(mContext, "Tracker Disconnected", "", 1405);                }                closeGatt();                String driverStatus = (String) Utils.getPreferences(mContext, Utils.DRIVER_STATUS, Utils.PREFTYPE_STRING);                if (BleManager.getInstance().isBleEnable() && BleManager.getInstance().isMacIdAvailable() && driverStatus.equalsIgnoreCase("onDuty"))                    reconnect(true);            }        }        private void refreshDeviceCache(BluetoothGatt gatt) {            try {                Method localMethod = gatt.getClass().getMethod("refresh");                localMethod.invoke(gatt);            } catch (Exception localException) {                Log.e("s", "An exception occured while refreshing device");            }        }        @Override        public void onServicesDiscovered(BluetoothGatt gatt, int status) {            // if (mGatt == null)            // return;            if (status == BluetoothGatt.GATT_SUCCESS) {                setCharacteristicNotification(true);                System.out.println("discover services " + status);            } else {                Log.w("servieDiscovered", "onServicesDiscovered received: "                        + status);            }        }        public void onCharacteristicRead(BluetoothGatt gatt,                                         BluetoothGattCharacteristic characteristic,                                         int status) {            if (status == BluetoothGatt.GATT_SUCCESS) {                byte[] data = characteristic.getValue();                if (data != null) {                    new ReceiveBandCommandResponseTask().execute(data, mContext);                }                //broadcastUpdate(CALLBACK_STATUS, characteristic);            } else {                Log.i("onCharacteristicRead", "onCharacteristicRead false "                        + status + characteristic.toString());            }        }        public void onDescriptorWrite(BluetoothGatt gatt,                                      BluetoothGattDescriptor descriptor, int status) {            if (status == BluetoothGatt.GATT_SUCCESS) {                isConnected = true;                boolean isLinked = (boolean) Utils.getPreferences(mContext, Utils.IS_LINKED, Utils.PREFTYPE_BOOLEAN);                if (!BleManager.getInstance().isMacIdAvailable()) {                    BleUtilStatus.sendBandStatusWithData(mContext, "Pairing request sent to your tracker.Please tap on tracker", "", 1302);                    CommandSendRequest.sendBindingRequestToV3Band();                    Utils.saveStringPreferences(mContext, Utils.TEMP_MACADDRESS, gatt.getDevice().getAddress());                    Utils.saveStringPreferences(mContext, Utils.MACADDRESS, gatt.getDevice().getAddress());                    Utils.saveStringPreferences(mContext, Utils.DRIVER_STATUS, "Connect");                } else if (isLinked)                    BleUtilStatus.sendBandStatusWithData(mContext, "Successful tracker connection", "", 1200);                broadcastUpdate(ACTION_GATT_onDescriptorWrite);            } else {                Log.i(TAG, "onDescriptorWrite: failed");            }        }        public void onCharacteristicChanged(BluetoothGatt gatt,                                            BluetoothGattCharacteristic characteristic) {            if (mGatt == null)                return;            Log.e(TAG, "onCharacteristicChangedResponse: " + Utils.byte2Hex(characteristic.getValue()));            byte[] data = characteristic.getValue();            if (data != null) {                new ReceiveBandCommandResponseTask().execute(data, mContext);            }            //broadcastUpdate(CALLBACK_STATUS, characteristic);        }        public void onCharacteristicWrite(BluetoothGatt gatt,                                          BluetoothGattCharacteristic characteristic, int status) {            if (status == BluetoothGatt.GATT_SUCCESS) {                nextQueue();            }        }    };    private void broadcastUpdate(String action) {        Intent intent = new Intent(action);        mContext.sendBroadcast(intent);    }    private void broadcastUpdate(String action,                                 BluetoothGattCharacteristic characteristic) {        byte[] data = characteristic.getValue();        Intent intent = new Intent();        intent.setAction(action);        intent.putExtra("response", data);        intent.putExtra("callbackCode", 1101);        sendBroadcast(intent);    }    public void writeValue(byte[] value) {        if (mGatt == null || value == null) return;        service = mGatt.getService(SERVICE_DATA);        if (service == null) return;        BluetoothGattCharacteristic characteristic = service.getCharacteristic(DATA_Characteristic);        if (characteristic == null) return;        if (value[0] == (byte) 0x47) {            NeedReconnect = false;        }        characteristic.setValue(value);        Log.i(TAG, "writeValue: " + Utils.byte2Hex(value));        mGatt.writeCharacteristic(characteristic);    }    public void writeValue(byte[] value, SendCmdState command) {        if (mGatt != null && value != null) {            BluetoothGattService service = mGatt.getService(SERVICE_DATA);            if (service == null) return;            BluetoothGattCharacteristic characteristic = service.getCharacteristic(DATA_Characteristic);            if (characteristic == null) return;            characteristic.setValue(value);            mGatt.writeCharacteristic(characteristic);        }    }    public void setCharacteristicNotification(boolean enable) {        // TODO Auto-generated method stub        if (mGatt == null) return;        BluetoothGattService service = mGatt.getService(SERVICE_DATA);        if (service == null) return;        BluetoothGattCharacteristic characteristic = service.getCharacteristic(NOTIY_Characteristic);        if (characteristic == null) return;        mGatt.setCharacteristicNotification(characteristic, enable);        try {            Thread.sleep(20);        } catch (InterruptedException e) {            e.printStackTrace();        }        BluetoothGattDescriptor descriptor = characteristic                .getDescriptor(NOTIY);        if (descriptor == null) {            return;        }        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);        if (mGatt == null)            return;        mGatt.writeDescriptor(descriptor);    }    @Override    public void onDestroy() {        // TODO Auto-generated method stub        super.onDestroy();        disconnect();        if (blueToothReciever != null)            unregisterReceiver(blueToothReciever);    }    Queue<byte[]> queues = new LinkedList<>();    public void nextQueue() {        final Queue<byte[]> requests = queues;        byte[] data = requests != null ? requests.poll() : null;        writeValue(data);    }    public boolean isConnected() {        return this.isConnected;    }    public class BluetoothReciever extends BroadcastReceiver {        @Override        public void onReceive(final Context context, Intent intent) {            try {                final String action = intent.getAction();                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);                    if (BleManager.getInstance() != null) {                        switch (state) {                            case BluetoothAdapter.STATE_OFF:                                BleUtilStatus.sendBandStatusWithData(context, "Bluetooth turned off", "", 1301);                                isConnected = false;                                BleManager.getInstance().disconnectDevice();                                break;                            case BluetoothAdapter.STATE_ON:                                if (BleManager.getInstance().isMacIdAvailable())                                    BleManager.getInstance().connectDevice();                                break;                        }                    }                } else if (action.equalsIgnoreCase(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {                    final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);                    if (bondState == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {                        Utils.printLog("e", "Unpaired", "");                    }                }            } catch (Exception e) {                Utils.printStackTrace(e);            }        }    }    public void cleanUp() {        Utils.printLog("e", TAG, "cleanUp called");        closeGatt();    }    public void closeGatt() {        if (mGatt != null) {            mGatt.disconnect();            if (mGatt != null)                mGatt.close();//            bleGattCallback = null;            mGatt = null;            service = null;        }    }}