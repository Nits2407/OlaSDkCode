package com.goqii.goqiisdk.network.ApiModels;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.goqii.goqiisdk.util.Utils;

public class TrackerSettingResponse {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("data")
    @Expose
    private TrackerSettingData data;
    @SerializedName("endpoints")
    @Expose
    private Endpoints endpoints;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public TrackerSettingData getData() {
        return data;
    }

    public void setData(TrackerSettingData data) {
        this.data = data;
    }

    public Endpoints getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Endpoints endpoints) {
        this.endpoints = endpoints;
    }

    public static void saveEndPoints(Context context, Endpoints endPoints) {
        if (endPoints != null)
            Utils.saveStringPreferences(context, Utils.KEY_END_POINTS, new Gson().toJson(endPoints));
    }
}