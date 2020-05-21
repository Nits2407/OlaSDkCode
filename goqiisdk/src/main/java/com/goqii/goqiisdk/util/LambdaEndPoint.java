package com.goqii.goqiisdk.util;

import android.content.Context;

import com.google.gson.Gson;
import com.goqii.goqiisdk.network.ApiModels.Endpoints;

public class LambdaEndPoint {

    public static String getLamdaUrls(Context context, String type) {

        String string = (String) Utils.getPreferences(context, Utils.KEY_END_POINTS, Utils.PREFTYPE_STRING);

        Endpoints endPoints = new Gson()
                .fromJson(string, Endpoints.class);


        String url = Utils.getBaseUrl(context) + "" + type;
        if (endPoints != null)
            switch (type) {
                case LambdaConstant.PARTNER_IDENTITY:
                    if (endPoints.getPartnerIdentity() != null)
                    url = endPoints.getPartnerIdentity();
                    break;

                case LambdaConstant.BATTERY_STATUS:
                    if (endPoints.getBatteryStatus() != null)
                        url = endPoints.getBatteryStatus();
                    break;

                case LambdaConstant.FETCH_ALERT:
                    if (endPoints.getFetchAlert() != null)
                        url = endPoints.getFetchAlert();
                    break;

                case LambdaConstant.HR_TEMPERATURE:
                    if (endPoints.getHrTemperature() != null)
                        url = endPoints.getHrTemperature();
                    break;

                case LambdaConstant.UPDATE_STATUS:
                    if (endPoints.getUpdateStatus() != null)
                        url = endPoints.getUpdateStatus();
                    break;

                case LambdaConstant.UPDATE_TRACKER_STATUS:
                    if (endPoints.getUpdateTrackerStatus() != null)
                        url = endPoints.getUpdateTrackerStatus();
                    break;



                default:
                    return url;
            }
        return url;
    }


}
