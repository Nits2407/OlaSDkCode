package com.goqii.goqiisdk.network;

import android.content.Context;

import com.goqii.goqiisdk.BuildConfig;
import com.goqii.goqiisdk.util.Utils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class NetworkClient {

    private static final int DEFAULT_TIMEOUT = 30;
    private static Retrofit retrofit = null;

    static synchronized Retrofit getClient(Context mContext, int timeout) {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(timeout != 0 ? timeout : DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        httpClient.connectTimeout(timeout != 0 ? timeout : DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        httpClient.protocols(Collections.singletonList(Protocol.HTTP_1_1));
        httpClient.connectTimeout(10, TimeUnit.SECONDS);
        httpClient.readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//            logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            httpClient.addNetworkInterceptor(logging);
        }

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Utils.getBaseUrl(mContext))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }

        return retrofit;
    }

}