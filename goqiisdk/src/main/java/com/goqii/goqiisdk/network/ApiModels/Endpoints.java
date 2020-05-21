package com.goqii.goqiisdk.network.ApiModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Endpoints {

    @SerializedName("partner_identity")
    @Expose
    private String partnerIdentity;
    @SerializedName("update_status")
    @Expose
    private String updateStatus;
    @SerializedName("update_tracker_status")
    @Expose
    private String updateTrackerStatus;
    @SerializedName("battery_status")
    @Expose
    private String batteryStatus;
    @SerializedName("hr_temperature")
    @Expose
    private String hrTemperature;
    @SerializedName("fetch_alert")
    @Expose
    private String fetchAlert;

    public String getPartnerIdentity() {
        return partnerIdentity;
    }

    public void setPartnerIdentity(String partnerIdentity) {
        this.partnerIdentity = partnerIdentity;
    }

    public String getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    public String getUpdateTrackerStatus() {
        return updateTrackerStatus;
    }

    public void setUpdateTrackerStatus(String updateTrackerStatus) {
        this.updateTrackerStatus = updateTrackerStatus;
    }

    public String getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(String batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public String getHrTemperature() {
        return hrTemperature;
    }

    public void setHrTemperature(String hrTemperature) {
        this.hrTemperature = hrTemperature;
    }

    public String getFetchAlert() {
        return fetchAlert;
    }

    public void setFetchAlert(String fetchAlert) {
        this.fetchAlert = fetchAlert;
    }

}