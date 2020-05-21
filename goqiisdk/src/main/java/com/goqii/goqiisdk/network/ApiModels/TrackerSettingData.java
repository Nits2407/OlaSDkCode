package com.goqii.goqiisdk.network.ApiModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class TrackerSettingData {
    @SerializedName("searchDeviceName")
    @Expose
    private ArrayList<String> searchDeviceName = null;
    @SerializedName("apiCallTimeInSeconds")
    @Expose
    private String apiCallTimeInSeconds;
    @SerializedName("apiCallMinTimeInSeconds")
    @Expose
    private String apiCallMinTimeInSeconds ;
    @SerializedName("apiCallMaxTimeInSeconds")
    @Expose
    private String apiCallMaxTimeInSeconds;
    @SerializedName("batteryApiCallTime")
    @Expose
    private String batteryApiCallTime ;
    @SerializedName("previousTrackerId")
    @Expose
    private String previousTrackerId  ;
    @SerializedName("batterStatusAlertPercentage")
    @Expose
    private String batterStatusAlertPercentage;
    @SerializedName("trackerNotWearAlertTime")
    @Expose
    private String trackerNotWearAlertTime  ;
    @SerializedName("apiBaseUrl")
    @Expose
    private String apiBaseUrl  ;
    @SerializedName("fetchAlertTime")
    @Expose
    private String fetchAlertTime  ;
    @SerializedName("debugLevel")
    @Expose
    private String debugLevel;

    public ArrayList<String> getSearchDeviceName() {
        return searchDeviceName;
    }

    public void setSearchDeviceName(ArrayList<String> searchDeviceName) {
        this.searchDeviceName = searchDeviceName;
    }

    public String getApiCallTimeInSeconds() {
        return apiCallTimeInSeconds;
    }

    public void setApiCallTimeInSeconds(String apiCallTimeInSeconds) {
        this.apiCallTimeInSeconds = apiCallTimeInSeconds;
    }

    public String getApiCallMinTimeInSeconds() {
        return apiCallMinTimeInSeconds;
    }

    public void setApiCallMinTimeInSeconds(String apiCallMinTimeInSeconds) {
        this.apiCallMinTimeInSeconds = apiCallMinTimeInSeconds;
    }

    public String getApiCallMaxTimeInSeconds() {
        return apiCallMaxTimeInSeconds;
    }

    public void setApiCallMaxTimeInSeconds(String apiCallMaxTimeInSeconds) {
        this.apiCallMaxTimeInSeconds = apiCallMaxTimeInSeconds;
    }

    public String getBatteryApiCallTime() {
        return batteryApiCallTime;
    }

    public void setBatteryApiCallTime(String batteryApiCallTime) {
        this.batteryApiCallTime = batteryApiCallTime;
    }

    public String getPreviousTrackerId() {
        return previousTrackerId;
    }

    public void setPreviousTrackerId(String previousTrackerId) {
        this.previousTrackerId = previousTrackerId;
    }

    public String getBatterStatusAlertPercentage() {
        return batterStatusAlertPercentage;
    }

    public void setBatterStatusAlertPercentage(String batterStatusAlertPercentage) {
        this.batterStatusAlertPercentage = batterStatusAlertPercentage;
    }

    public String getTrackerNotWearAlertTime() {
        return trackerNotWearAlertTime;
    }

    public void setTrackerNotWearAlertTime(String trackerNotWearAlertTime) {
        this.trackerNotWearAlertTime = trackerNotWearAlertTime;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getFetchAlertTime() {
        return fetchAlertTime;
    }

    public void setFetchAlertTime(String fetchAlertTime) {
        this.fetchAlertTime = fetchAlertTime;
    }

    public String getDebugLevel() {
        return debugLevel;
    }

    public void setDebugLevel(String debugLevel) {
        this.debugLevel = debugLevel;
    }

}
