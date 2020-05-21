package com.goqii.goqiisdk.ble.CommandFiles;

import android.content.Context;
import android.os.AsyncTask;

import com.goqii.goqiisdk.model.DeviceBean;

/**
 * Created by Nitish-GOQii on 11-07-2018.
 */

public class ReceiveBandCommandResponseTask extends AsyncTask<Object, Void, DeviceBean> {
    @Override
    protected DeviceBean doInBackground(Object... params) {
        byte[] arr = (byte[]) params[0];
        Context context = (Context) params[1];
        SingleDealData.receiveUpdateValue(arr,context.getApplicationContext());
        return null;
    }

    @Override
    protected void onPostExecute (DeviceBean aVoid) {
        super.onPostExecute(aVoid);
    }
}