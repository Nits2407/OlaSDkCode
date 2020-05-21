package com.goqii.goqiisdk.network;

import android.content.Context;
import android.os.AsyncTask;

import com.goqii.goqiisdk.ble.BleManager;
import com.goqii.goqiisdk.database.DatabaseHandler;
import com.goqii.goqiisdk.network.ApiModels.FilesPreSignedUrl;
import com.goqii.goqiisdk.network.ApiModels.GeneratePreSignedUrlData;
import com.goqii.goqiisdk.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Response;

public class GeneratePreSignedUrlResponse {

    private int code;
    private GeneratePreSignedUrlData data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public GeneratePreSignedUrlData getData() {
        return data;
    }

    public void setData(GeneratePreSignedUrlData data) {
        this.data = data;
    }

    public void generatePreSigned_url(final Context context, final ArrayList<FilesPreSignedUrl> urls, final String moduleType) {

        String fileType = urls.get(0).getFile().getAbsolutePath().substring(urls.get(0).getFile().getAbsolutePath().lastIndexOf("."));
        NetworkManager networkManager = NetworkManager.getInstance(context);
        Map<String, Object> query =  NetworkManager.getInstance(context).getDefaultQueryMap(context);
        query.put("trackerMacId", BleManager.getInstance().getGoqiiTrackerMacID());
        query.put("profile", Utils.getIdentityObject(context));
        query.put("phoneIdentity", Utils.getPhoneIdentityObject(context));
        query.put("moduleType", moduleType);
        query.put("quantity", urls.size());
        query.put("fileType", fileType);


        networkManager.request(query, NetworkManager.REQUEST.GENERATE_PRESIGNED_URL, new NetworkManager.RequestCallback() {
            @Override
            public void onSuccess(NetworkManager.REQUEST type, Response response) {
                if (type == NetworkManager.REQUEST.GENERATE_PRESIGNED_URL) {
                    GeneratePreSignedUrlResponse generatePresignedUrlResponse = (GeneratePreSignedUrlResponse) response.body();
                    if (generatePresignedUrlResponse != null && generatePresignedUrlResponse.getCode() == 200) {

                        ArrayList<FilesPreSignedUrl> tempList = new ArrayList<>();
                        for (int i = 0; i < generatePresignedUrlResponse.getData().getUrl().size(); i++) {
                            String url = generatePresignedUrlResponse.getData().getUrl().get(i);
                            FilesPreSignedUrl filesPreSignedUrl = new FilesPreSignedUrl();
                            filesPreSignedUrl.setFile(urls.get(i).getFile());
                            filesPreSignedUrl.setPreSignedUrl(url);
                            tempList.add(filesPreSignedUrl);
                        }
                        new UploadTask(context, tempList, moduleType).execute();
                    }
                }
            }

            @Override
            public void onFailure(NetworkManager.REQUEST type) {
                if (type == NetworkManager.REQUEST.GENERATE_PRESIGNED_URL) {
                }
            }
        });
    }

    protected static class UploadTask extends AsyncTask<String, Void, String> {
        okhttp3.Response uploadResponse;
        String mModuleType;
        public Context mContext;
        private ArrayList<FilesPreSignedUrl> mFilesPreSignedUrl;
        public String status;

        UploadTask(Context context, ArrayList<FilesPreSignedUrl> filesPreSignedUrl, String moduleType) {
            mContext = context;
            mFilesPreSignedUrl = filesPreSignedUrl;
            mModuleType = moduleType;
            status = (String) Utils.getPreferences(context, Utils.DRIVER_STATUS, Utils.PREFTYPE_STRING);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
//                // Obtain the url
                int NETWORK_TIMEOUT_SEC = 60;
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
                        .connectTimeout(NETWORK_TIMEOUT_SEC, TimeUnit.SECONDS)
                        .readTimeout(NETWORK_TIMEOUT_SEC, TimeUnit.SECONDS)
                        .writeTimeout(NETWORK_TIMEOUT_SEC, TimeUnit.SECONDS)
                        .build();
                // Upload the file


                for (FilesPreSignedUrl file : mFilesPreSignedUrl) {
                    Request uploadFileRequest = new Request.Builder()
                            .url(file.getPreSignedUrl())
                            .put(RequestBody.create(MediaType.parse(""), file.getFile()))
                            .build();
                    uploadResponse = client.newCall(uploadFileRequest).execute();

                    if (!status.equalsIgnoreCase("logout"))
                        DatabaseHandler.getInstance(mContext).updateStatus();
                }
            } catch (
                    Exception e) {
                Utils.printLog("e", "GeneratePreSignedUrlResponse", "Error");
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
                Utils.printLog("e","File Upload", "Data Uploaded Successfully");
        }
    }
}
