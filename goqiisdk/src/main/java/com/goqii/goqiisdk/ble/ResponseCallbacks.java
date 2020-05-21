package com.goqii.goqiisdk.ble;

import com.goqii.goqiisdk.network.NetworkManager;

public interface ResponseCallbacks {
    void getStatus(int code);
    void getApiResult(boolean isSuccess, NetworkManager.REQUEST request);
}
