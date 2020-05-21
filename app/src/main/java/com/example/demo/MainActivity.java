package com.example.demo;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goqii.goqiisdk.ble.BleManager;
import com.goqii.goqiisdk.ble.BleService;
import com.goqii.goqiisdk.util.Utils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSend, btnClear;
    private EditText ed1, ed2, ed3, ed4, ed5, ed6, ed7, ed8, ed9, ed10, ed11, ed12, ed13, ed14, ed15, ed16;
    private BandInfoBroadcastReceiver bandReciever;
    private TextView tvResponse;
    private ProgressDialog progressDialog;
    private String responseText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initListeners();
        initData();
    }

    private void initViews() {
        ed1 = findViewById(R.id.ed1);
        ed2 = findViewById(R.id.ed2);
        ed3 = findViewById(R.id.ed3);
        ed4 = findViewById(R.id.ed4);
        ed5 = findViewById(R.id.ed5);
        ed6 = findViewById(R.id.ed6);
        ed7 = findViewById(R.id.ed7);
        ed8 = findViewById(R.id.ed8);
        ed9 = findViewById(R.id.ed9);
        ed10 = findViewById(R.id.ed10);
        ed11 = findViewById(R.id.ed11);
        ed12 = findViewById(R.id.ed12);
        ed13 = findViewById(R.id.ed13);
        ed14 = findViewById(R.id.ed14);
        ed15 = findViewById(R.id.ed15);
        ed16 = findViewById(R.id.ed16);
        btnSend = findViewById(R.id.btnSend);
        btnClear = findViewById(R.id.btnClear);
        tvResponse = findViewById(R.id.tvResponse);
        tvResponse.setMovementMethod(new ScrollingMovementMethod());
    }

    private void initListeners() {
        btnSend.setOnClickListener(this);
        btnClear.setOnClickListener(this);

        btnClear.setEnabled(false);
        btnSend.setEnabled(false);
    }

    private void initData() {
        registerBandReceiver();
        connectDevice();
    }

    private void connectDevice() {
        String address = getIntent().getStringExtra("address");
        if (TextUtils.isEmpty(address)) {
            Log.i("Main", "onCreate: address null ");
            return;
        }
        Log.i("Main", "onCreate: ");
        BleManager.getInstance().connectDevice();
        showConnectDialog();
    }

    private void showConnectDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.connectting));
        // progressDialog.setCancelable(false);
        if (!progressDialog.isShowing()) progressDialog.show();

    }

    private void dissMissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

    }


    private void registerBandReceiver() {
        bandReciever = new BandInfoBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BleService.ACTION_GATT_onDescriptorWrite);
        filter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BleService.ACTION_DATA_AVAILABLE);

        registerReceiver(bandReciever, filter);

    }

    public class BandInfoBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null)
                switch (intent.getAction()) {
                    case BleService.ACTION_GATT_onDescriptorWrite:
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        btnClear.setEnabled(true);
                        btnSend.setEnabled(true);
                        dissMissDialog();
                        break;
                    case BleService.ACTION_GATT_DISCONNECTED:
                        Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                        break;

                    case BleService.ACTION_DATA_AVAILABLE:
                        byte[] response = intent.getByteArrayExtra("response");
                        if (response != null) {
                            responseText += "\n " + Utils.byte2Hex(response);
                            tvResponse.setText(responseText);
                        }
                        break;
                    default:
                        break;

                }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClear:
                resetAll();
                break;
            case R.id.btnSend:
                byte[] value = requestCommandResponse();
                BleManager.getInstance().writeValue(value);
                break;
        }
    }

    private byte[] requestCommandResponse() {
        byte[] value = new byte[16];
        value[0] = (byte) Integer.parseInt(ed1.getText().toString().trim(), 16);
        value[1] = (byte) Integer.parseInt(ed2.getText().toString().trim(), 16);
        value[2] = (byte) Integer.parseInt(ed3.getText().toString().trim(), 16);
        value[3] = (byte) Integer.parseInt(ed4.getText().toString().trim(), 16);
        value[4] = (byte) Integer.parseInt(ed5.getText().toString().trim(), 16);
        value[5] = (byte) Integer.parseInt(ed6.getText().toString().trim(), 16);
        value[6] = (byte) Integer.parseInt(ed7.getText().toString().trim(), 16);
        value[7] = (byte) Integer.parseInt(ed8.getText().toString().trim(), 16);
        value[8] = (byte) Integer.parseInt(ed9.getText().toString().trim(), 16);
        value[9] = (byte) Integer.parseInt(ed10.getText().toString().trim(), 16);
        value[10] = (byte) Integer.parseInt(ed11.getText().toString().trim(), 16);
        value[11] = (byte) Integer.parseInt(ed12.getText().toString().trim(), 16);
        value[12] = (byte) Integer.parseInt(ed13.getText().toString().trim(), 16);
        value[13] = (byte) Integer.parseInt(ed14.getText().toString().trim(), 16);
        value[14] = (byte) Integer.parseInt(ed15.getText().toString().trim(), 16);

        byte crc = 0;
        for (int i = 0; i < value.length - 1; i++) {
            crc += value[i];
        }
        value[value.length - 1] = (byte) (crc & 0xff);
        return value;
    }

    private void resetAll() {
        ed1.setText("00");
        ed2.setText("00");
        ed3.setText("00");
        ed4.setText("00");
        ed5.setText("00");
        ed6.setText("00");
        ed7.setText("00");
        ed8.setText("00");
        ed9.setText("00");
        ed10.setText("00");
        ed11.setText("00");
        ed12.setText("00");
        ed13.setText("00");
        ed14.setText("00");
        ed15.setText("00");
        ed16.setText("00");
        tvResponse.setText("");
        responseText = "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bandReciever);
    }
}
