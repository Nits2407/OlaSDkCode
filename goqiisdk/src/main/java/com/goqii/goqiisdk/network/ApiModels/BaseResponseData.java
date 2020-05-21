package com.goqii.goqiisdk.network.ApiModels;

/**
 * Created by GOQii-Rohan on 19-05-2017.
 */

public class BaseResponseData {
    private String message;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
