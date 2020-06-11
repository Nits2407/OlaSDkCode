package com.goqii.goqiisdk.util;

import android.content.Context;
import android.content.Intent;

import com.goqii.goqiisdk.ble.BleService;

public class BleUtilStatus {

    public static void sendBandStatus(Context context, int callbackCode) {
        Utils.printLog("e", "GoqiiOlaIntegration", "" + callbackCode);
        Intent intent = new Intent();
        intent.setAction(BleService.CALLBACK_STATUS);
        intent.putExtra("callbackCode", callbackCode);
        context.sendBroadcast(intent);
    }

    public static void sendBandStatusWithData(Context context, String alertType, String severity, int callbackCode) {
        Utils.printLog("e", "GoqiiOlaIntegration", "" + callbackCode);
        Intent intent = new Intent();
        intent.setAction(BleService.CALLBACK_STATUS);
        intent.putExtra("callbackCode", callbackCode);
        intent.putExtra("alertType", alertType);
        intent.putExtra("severity", severity);
        context.sendBroadcast(intent);
    }
}
