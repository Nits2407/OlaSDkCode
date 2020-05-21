package com.goqii.goqiisdk.network;

import com.goqii.goqiisdk.network.ApiModels.BaseResponse;
import com.goqii.goqiisdk.network.ApiModels.PoolingResponse;
import com.goqii.goqiisdk.network.ApiModels.TrackerSettingResponse;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

public interface APIFactory {


    @POST("fetch_tracker_setting")
    Call<TrackerSettingResponse> getSettings(@QueryMap Map<String, Object> query);

    @POST("fetch_alert")
    Call<PoolingResponse> fetchAlert(@QueryMap Map<String, Object> query);

    @POST("partner_identity")
    Call<BaseResponse> sendIdentity(@QueryMap Map<String, Object> query);

    @POST("battery_status")
    Call<BaseResponse> sendBattery(@QueryMap Map<String, Object> query);

    @POST("update_status")
    Call<BaseResponse> updateStatus(@QueryMap Map<String, Object> query);

    @POST("update_tracker_status")
    Call<BaseResponse> updateTrackerStatus(@QueryMap Map<String, Object> query);

    @POST("hr_temperature")
    Call<BaseResponse> sendTemperatureData(@QueryMap Map<String, Object> query, @Body RequestBody body);

    @POST
    Call<PoolingResponse> fetchAlert(@Url String url, @QueryMap Map<String, Object> query);

    @POST
    Call<BaseResponse> sendIdentity(@Url String url, @QueryMap Map<String, Object> query);

    @POST
    Call<BaseResponse> sendBattery(@Url String url, @QueryMap Map<String, Object> query);

    @POST
    Call<BaseResponse> updateStatus(@Url String url, @QueryMap Map<String, Object> query);

    @POST
    Call<BaseResponse> updateTrackerStatus(@Url String url, @QueryMap Map<String, Object> query);

    @POST
    Call<BaseResponse> sendTemperatureData(@Url String url, @QueryMap Map<String, Object> query, @Body RequestBody body);


    @POST("https://qdvvi7j7k7.execute-api.ap-south-1.amazonaws.com/default/generate_presigned_url_for_ola")
    Call<GeneratePreSignedUrlResponse> generate_presigned_url(@QueryMap Map<String, Object> headerMap);
}