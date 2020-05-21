package com.goqii.goqiisdk.network.ApiModels;

/**
 * Created by GOQii-Rohan on 19-05-2017.
 */

public class BaseResponse {

    private int code;
    private BaseResponseData data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public BaseResponseData getData() {
        return data;
    }

    public void setData(BaseResponseData data) {
        this.data = data;
    }
}
