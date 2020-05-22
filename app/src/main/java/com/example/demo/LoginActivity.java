package com.example.demo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.goqii.goqiisdk.ble.BleManager;
import com.goqii.goqiisdk.ble.ResponseCallbacks;
import com.goqii.goqiisdk.network.NetworkManager;
import com.goqii.goqiisdk.util.Utils;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements ResponseCallbacks {

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
    }

    private void initViews() {
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callISettingsApi();
            }
        });
    }

    private void callISettingsApi() {
        JSONObject olaJsonObject = new JSONObject();
        try {
            olaJsonObject.put("driverId", "d3-ban");
            olaJsonObject.put("sessionId", "903243fhrsa");
        } catch (Exception e) {
            e.printStackTrace();
        }
        BleManager.init(getApplicationContext(), olaJsonObject);
        if (Utils.isNetworkAvailable(this)) {
            showConnectDialog(getString(R.string.fetching_tracker_setting));
        }
        moveToNext();
    }

    @Override
    public void getStatus(int code) {

    }

    @Override
    public void getApiResult(boolean isSuccess, NetworkManager.REQUEST request) {
        if (isSuccess)
            moveToNext();
    }

    private void moveToNext() {
        dissMissDialog();
        Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, RealtimeHrAndTempDataActivity.class);
        startActivity(intent);
        finish();
    }

    private void showConnectDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        if (!progressDialog.isShowing()) progressDialog.show();

    }

    private void dissMissDialog() {
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();

    }

}
