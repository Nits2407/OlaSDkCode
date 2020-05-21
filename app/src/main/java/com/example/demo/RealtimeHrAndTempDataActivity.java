package com.example.demo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.goqii.goqiisdk.ble.BleManager;
import com.goqii.goqiisdk.ble.BleService;
import com.goqii.goqiisdk.ble.ResponseCallbacks;
import com.goqii.goqiisdk.network.NetworkManager;
import com.goqii.goqiisdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class RealtimeHrAndTempDataActivity extends AppCompatActivity implements ResponseCallbacks, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String ACTION_UPDATE = "ACTION_UPDATE";
    private TextView tvHrValue, tvTempValue, tvBatteryValue;
    private BandInfoBroadcastReceiver bandReciever;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private ProgressDialog progressDialog;
    private TextView tvMac, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_hr_and_temp_data);

        initViews();
        initData();
    }

    private void initData() {
        if (BleManager.getInstance().isMacIdAvailable()) {
            String mac = (String) Utils.getPreferences(this, Utils.MACADDRESS, Utils.PREFTYPE_STRING);
            tvMac.setText("MacAdress:" + mac);
            BleManager.getInstance().setResponseCallbacks(this);
        }
        registerBandReceiver();
        startScanCode();
        //connectDevice();
    }

    private void initViews() {
        tvHrValue = findViewById(R.id.tvHrValue);
        tvTempValue = findViewById(R.id.tvTempValue);
        tvBatteryValue = findViewById(R.id.tvBatteryValue);
        tvMac = findViewById(R.id.tvMac);
        Button btnDashboard = findViewById(R.id.btnDashboard);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnChangeApiCallTime = findViewById(R.id.btnChangeApiCallTime);
        Button btnOffDuty = findViewById(R.id.btnOffDuty);
        Button btnOnDuty = findViewById(R.id.btnOnDuty);
        Button btnDisconnect = findViewById(R.id.btnDisconnect);
        Button btnSkip = findViewById(R.id.btnSkip);
        Button btnSyncOff = findViewById(R.id.btnSyncOff);
        Button btnSyncOn = findViewById(R.id.btnSyncOn);

        btnDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BleManager.getInstance().isMacIdAvailable()) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://insight.goqii.com/real-time-hr?uid=" + BleManager.getInstance().getMacIdBase64()));
                    startActivity(browserIntent);
                } else {
                    Toast.makeText(RealtimeHrAndTempDataActivity.this, "Tracker not linked", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConnectDialog(getString(R.string.please_wait));
                BleManager.getInstance().updateStatus("logout");
            }
        });

        btnOffDuty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConnectDialog(getString(R.string.please_wait));
                BleManager.getInstance().updateStatus("offduty");
            }
        });

        btnOnDuty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BleManager.getInstance().isBleEnable())
                    BleManager.getInstance().updateStatus("onduty");
                else {
                    Intent enableBtIntent = new Intent(
                            BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }

            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleManager.getInstance().updateStatus("skipPairing");
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConnectDialog(getString(R.string.please_wait));
                BleManager.getInstance().updateStatus("disconnect");
            }
        });

        btnSyncOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleManager.getInstance().updateSyncingStatus("syncOn");
            }
        });

        btnSyncOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleManager.getInstance().updateSyncingStatus("syncOff");
            }
        });

        btnChangeApiCallTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject settingsData = new JSONObject();
                try {
                    settingsData.put("apiCallTime", "240");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                BleManager.getInstance().updateTrackerSettings(settingsData.toString());
            }
        });
    }

    private void startScanCode() {
        if (BleManager.getInstance().isBleEnable())
            initialisedScan();
        else {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void initialisedScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        } else {
            String status = (String) Utils.getPreferences(this, Utils.DRIVER_STATUS, Utils.PREFTYPE_STRING);
            if (!status.equalsIgnoreCase("skipPairing")) {
                //showConnectDialog(getString(R.string.connectting));
                setUpGClient();
                //BleManager.getInstance().startScan(this);
            }
        }
    }

    private void showConnectDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        if (!progressDialog.isShowing()) progressDialog.show();

    }

    private void dissMissDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();

    }


    private void registerBandReceiver() {
        bandReciever = new BandInfoBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BleService.CALLBACK_STATUS);

        registerReceiver(bandReciever, filter);

    }

    private void updateData() {
        final int batteryPower = BleManager.getInstance().getBatteryStatus();
        final String heartRate = BleManager.getInstance().getHeartRate();
        final String temp = BleManager.getInstance().getBodyTemperature();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(heartRate) && Integer.parseInt(heartRate) > 0)
                    tvHrValue.setText(heartRate + " bpm");
                if (!TextUtils.isEmpty(temp) && Float.parseFloat(temp) > 0)
                    tvTempValue.setText(temp + " \u2109");
                if (batteryPower > 0)
                    tvBatteryValue.setText(batteryPower + "%");
            }
        });
    }

    @Override
    public void getStatus(int code) {
        Utils.printLog("e", "callbackCode:", "" + code);
    }

    @Override
    public void getApiResult(boolean isSuccess, NetworkManager.REQUEST request) {
        dissMissDialog();
        if (request == NetworkManager.REQUEST.UPDATE_STATUS) {
            if (isSuccess)
                finish();
        } else {
            if (isSuccess)
                BleManager.getInstance().connectDevice();
        }
    }

    public class BandInfoBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null)
                switch (intent.getAction()) {
                    case BleService.CALLBACK_STATUS:
                        int callbackCode = intent.getIntExtra("callbackCode", 0);
                        showToast(intent, callbackCode);
                        break;
                    default:
                        break;
                }
        }
    }

    private void showToast(Intent intent, int callbackCode) {
        String alertType = intent.getStringExtra("alertType");
        String severity = intent.getStringExtra("severity");
        Utils.printLog("e","Callback","" + callbackCode);
        String msg = "";
        dissMissDialog();
        boolean isToastShow = true;
        switch (callbackCode) {
            case 1101:
                updateData();
                isToastShow = false;
                break;
            case 1200:
                if (alertType != null)
                    msg = alertType;
                else
                    msg = "Successful tracker connection";
                if (BleManager.getInstance().isMacIdAvailable()) {
                    String mac = (String) Utils.getPreferences(this, Utils.MACADDRESS, Utils.PREFTYPE_STRING);
                    tvMac.setText("MacAdress:" + mac);
                }
                BleManager.getInstance().startDataReading();
                isToastShow = false;
                dissMissDialog();
                break;
            case 1301:
                if (alertType != null)
                    msg = alertType;
                else
                    msg = "Bluetooth turned off";
                break;
            case 1302:
                if (alertType != null)
                    msg = alertType;
                else
                    msg = "Pairing request sent to your tracker.Please tap on tracker";
                break;
            case 1401:
                msg = "Tracker not found";
                break;
            case 1402:
            case 1407:
            case 1404:
            case 1403:
                if (alertType != null)
                    msg = alertType;
                break;
            case 1405:
                if (alertType != null)
                    msg = alertType;
                else
                    msg = "Tracker Disconnected";
                break;
            case 1406:
                if (alertType != null)
                    msg = alertType;
                else
                    msg = "Pairing Timeout";
                dissMissDialog();
                break;

        }
        if (isToastShow)
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BleManager.getInstance().isConnected())
            BleManager.getInstance().disconnectDevice();
        if (bandReciever != null)
            unregisterReceiver(bandReciever);
    }

    private synchronized void setUpGClient() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result1) {
                final Status status = result1.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(RealtimeHrAndTempDataActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        String status = (String) Utils.getPreferences(this, Utils.DRIVER_STATUS, Utils.PREFTYPE_STRING);
                        if (!status.equalsIgnoreCase("skipPairing")) {
                            //showConnectDialog(getString(R.string.connectting));
                            setUpGClient();
                            //BleManager.getInstance().startScan(this);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                return;
            } else {
                initialisedScan();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
