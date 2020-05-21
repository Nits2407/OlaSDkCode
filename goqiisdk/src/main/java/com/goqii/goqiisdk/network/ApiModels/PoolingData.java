package com.goqii.goqiisdk.network.ApiModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class PoolingData {

    @SerializedName("alertCode")
    @Expose
    private String alertCode;
    @SerializedName("severity")
    @Expose
    private String severity;
    @SerializedName("alertType")
    @Expose
    private String alertType;

    public String getAlertCode() {
        return alertCode;
    }

    public void setAlertCode(String alertCode) {
        this.alertCode = alertCode;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }
}
