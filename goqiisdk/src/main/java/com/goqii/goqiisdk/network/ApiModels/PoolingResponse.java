package com.goqii.goqiisdk.network.ApiModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PoolingResponse {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("currentHR")
    @Expose
    private String currentHR;
    @SerializedName("currentTemperature")
    @Expose
    private String currentTemperature;
    @SerializedName("data")
    @Expose
    private ArrayList<PoolingData> data = null;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getCurrentHR() {
        return currentHR;
    }

    public void setCurrentHR(String currentHR) {
        this.currentHR = currentHR;
    }

    public String getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(String currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public ArrayList<PoolingData> getData() {
        return data;
    }

    public void setData(ArrayList<PoolingData> data) {
        this.data = data;
    }

}