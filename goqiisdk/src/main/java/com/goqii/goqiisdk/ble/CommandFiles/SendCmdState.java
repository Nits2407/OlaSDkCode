package com.goqii.goqiisdk.ble.CommandFiles;



public enum SendCmdState {

    SEND_DEVICE_Time,           // Set Device Time              0x01
    START_REAL_TIME_PEDOMETER_MODE, // Real Time Data          0x09
    READ_DEVICE_BATTERY,        // Get Device Battery                  0x13
    SEND_BINDING_REQUEST,       // Send Bond Request                0x20
    RECIEVE_BINDING_REQUEST,    // Recieve Bond Request             0x21
}
