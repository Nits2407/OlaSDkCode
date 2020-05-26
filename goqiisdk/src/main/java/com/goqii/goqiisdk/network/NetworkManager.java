package com.goqii.goqiisdk.network;

import android.content.Context;

import com.google.gson.Gson;
import com.goqii.goqiisdk.util.LambdaConstant;
import com.goqii.goqiisdk.util.LambdaEndPoint;
import com.goqii.goqiisdk.util.Utils;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkManager {

    private static NetworkManager mInstance = null;
    private static Context mContext;
    private HashMap<String, Object> mHeaderMap;

    public static synchronized NetworkManager getInstance(Context context) {
        if (mInstance == null) {
            mContext = context;
            mInstance = new NetworkManager();
        }

        return mInstance;
    }

    private APIFactory getResponseFactory(Context mContext) {
        APIFactory factory = NetworkClient.getClient(mContext,0).create(APIFactory.class);
        return factory;
    }

    public Call request(final Map<String, Object> queryMap, final REQUEST type, final RequestCallback callback) {
        Call request = getRequest(null, queryMap,null, type);
        if (request != null)
            enqueueRequest(request, type, callback);

        return request;
    }

    public void request(final Map<String, Object> queryMap, final Map<String,Object>queryBody, final REQUEST type, final RequestCallback callback) {
        Call request = getRequest(null,queryMap,queryBody, type);
        if (request != null)
            enqueueRequest(request, type, callback);
    }

    private Call getRequest(String url, Map<String, Object> queryMap, Map<String, Object> queryBody, REQUEST type) {
        Call call = null;

        switch (type) {

            case FETCH_TRACKER_SETTINGS:
                call = getResponseFactory(mContext).getSettings(queryMap);
                break;
            case PARTNER_IDENTITY:
                url = LambdaEndPoint.getLamdaUrls(mContext, LambdaConstant.PARTNER_IDENTITY);
                call = getResponseFactory(mContext).sendIdentity(url,queryMap);
                //call = getResponseFactory(mContext).sendIdentity(queryMap);
                break;
            case BATTERY_STATUS:
                url = LambdaEndPoint.getLamdaUrls(mContext, LambdaConstant.BATTERY_STATUS);
                call = getResponseFactory(mContext).sendBattery(url,queryMap);
               // call = getResponseFactory(mContext).sendBattery(queryMap);
                break;
            case UPDATE_STATUS:
                url = LambdaEndPoint.getLamdaUrls(mContext, LambdaConstant.UPDATE_STATUS);
                call = getResponseFactory(mContext).updateStatus(url,queryMap);
                //call = getResponseFactory(mContext).updateStatus(queryMap);
                break;
            case FETCH_ALERT:
                url = LambdaEndPoint.getLamdaUrls(mContext, LambdaConstant.FETCH_ALERT);
                call = getResponseFactory(mContext).fetchAlert(url, queryMap);
                //call = getResponseFactory(mContext).fetchAlert( queryMap);
                break;
            case UPDATE_TRACKER_STATUS:
                url = LambdaEndPoint.getLamdaUrls(mContext, LambdaConstant.UPDATE_TRACKER_STATUS);
                call = getResponseFactory(mContext).updateTrackerStatus(url, queryMap);
                //call = getResponseFactory(mContext).updateTrackerStatus( queryMap);
                break;
            case TEMPERATURE_DATA:
                url = LambdaEndPoint.getLamdaUrls(mContext, LambdaConstant.HR_TEMPERATURE);
                call = getResponseFactory(mContext).sendTemperatureData(url, queryMap,getBody(type,queryBody));
                break;
            case GENERATE_PRESIGNED_URL:
                call = getResponseFactory(mContext).generate_presigned_url(queryMap);
                break;
        }

        return call;
    }

    private RequestBody getBody(Map<String, Object> bodyMap) {
        String mapJson = new Gson().toJson(bodyMap);
        return RequestBody.create(MediaType.parse("text/plain"), mapJson);
    }

    Map<String, Object> getDefaultQueryMap(Context context) {
        if (mHeaderMap == null) {
            mHeaderMap = new HashMap<>();
            mHeaderMap.put("organizationId", Utils.getPreferences(context,Utils.GOQii_ACCOUNT_ID,Utils.PREFTYPE_STRING));
            mHeaderMap.put("nonce", Utils.getPreferences(context,Utils.GOQii_ACCOUNT_ID,Utils.PREFTYPE_STRING));
            //mHeaderMap.put("organizationApiKey", "f988n87erhv3y94154qj4uy4l");//Live
            mHeaderMap.put("organizationApiKey", "zavj59zhx4fhm2yjd344bachx");//demo
            mHeaderMap.put("signature",Utils.createSignature(context));
        }

        return mHeaderMap;
    }

    private void enqueueRequest(Call request, final REQUEST type, final RequestCallback callback) {

        request.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Utils.printLog("d", "onResponse", "" + response.toString());
                if (response.isSuccessful())
                    callback.onSuccess(type, response);
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                Utils.printLog("e", "onFailure", type.name() + " : " + t.getMessage());
                callback.onFailure(type);
            }
        });
    }

    private RequestBody getBody(REQUEST type, Map<String, Object> bodyMap) {
        Gson gson = new Gson();
        String mapJson = gson.toJson(bodyMap);
        Utils.printLog("i", "NetworkManager", "Body-JSON : " +
                type.name() + "\n" + mapJson);
//        CommonMethods.printLog("d", "objJson", "" + objJson);
        return RequestBody.create(MediaType.parse("text/plain"), mapJson);
    }

    public enum REQUEST {
        FETCH_TRACKER_SETTINGS,
        PARTNER_IDENTITY,
        BATTERY_STATUS,
        UPDATE_STATUS,
        FETCH_ALERT,
        UPDATE_TRACKER_STATUS,
        GENERATE_PRESIGNED_URL,
        TEMPERATURE_DATA

    }

    public interface RequestCallback {
        void onSuccess(REQUEST type, Response response);

        void onFailure(REQUEST type);
    }


}