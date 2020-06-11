package com.goqii.goqiisdk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.goqii.goqiisdk.BuildConfig;
import com.goqii.goqiisdk.ble.BleManager;
import com.goqii.goqiisdk.model.TemperatureModel;
import com.goqii.goqiisdk.network.ApiModels.FilesPreSignedUrl;
import com.goqii.goqiisdk.network.GeneratePreSignedUrlResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Utils {
    /*Latest VALUES*/
    public static final String BATTERY_POWER = "battery_power";
    public static final String MACADDRESS = "macaddress";
    public static final String TEMP_MACADDRESS = "temp_macaddress";
    public static final String HEARTRATE = "heartRate";
    public static final String TEMPERATURE = "temperature";

    public static final String POOLING_TEMPERATURE = "pooling_temperature";
    public static final String POOLING_HEARTRATE = "pooling_heartrate";

    /*Settings API*/
    public static final String APICALLTIME = "apicalltime";
    public static final String APIMINCALLTIME = "apimincalltime";
    public static final String APIMAXCALLTIME = "apimaxcalltime";
    public static final String BATTERYAPICALLTIME = "batteryapicalltime";
    public static final String BATTERSTATUSALERTPERCENTAGE = "batterStatusAlertPercentage ";
    public static final String PREVIOUSTRACKERID = "previousTrackerId";
    public static final String TRACKERNOTWEARALERTTIME = "trackerNotWearAlertTime";
    public static final String APIBASEURL = "apiBaseUrl";
    public static final String FETCHALERTTIME = "fetchAlertTime";
    public static final String DEBUG_LEVEL = "debug_level";

    public static final String PROFILE_DATA = "profile_data";
    public static final String GOQii_TOKEN = "goqii_token";
    public static final String GOQii_ACCOUNT_ID = "goqii_account_id";
    public static final String IS_SETTINGS_API = "is_settings_api";
    public static final String IS_SYNC_ON = "is_sync_on";
    public static final String KEY_END_POINTS = "key_end_points";
    public static final String IS_LINKED = "is_linked";
    public static final String IS_LOG_DISABLED = "is_log_disabled";
    private static final String PHONE_IDENTITY = "phone_identity";
    public static final String DEVICENAME = "devicename";
    public static final String DRIVER_STATUS = "driver_status";
    public static final String BASE_URL = "base_url";


    public final static int PREFTYPE_BOOLEAN = 0;
    public final static int PREFTYPE_INT = 1;
    public final static int PREFTYPE_STRING = 2;
    private final static int PREFTYPE_LONG = 3;
    private static final String SERVER_URL = "https://apiola.goqii.com/organization/";

    public static String byte2Hex(byte[] data) {
        if (data != null && data.length > 0) {
            StringBuilder sb = new StringBuilder(data.length);
            for (byte tmp : data) {
                sb.append(String.format("%02X ", tmp));
            }
            return sb.toString();
        }
        return "no data";
    }

    public static void printStackTrace(Exception exception) {
        if (BuildConfig.DEBUG) {
            exception.printStackTrace();
        }
    }

    public static void saveStringPreferences(Context context, String strKey, String strValue) {
        try {
            if (context != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (strValue != null)
                    editor.putString(strKey, strValue);
                else
                    editor.putString(strKey, "");
                editor.apply();
            }
        } catch (Exception e) {
            printStackTrace(e);
        }
    }

    public static void saveIntPreferences(Context context, String strKey, int intValue) {
        try {
            if (context != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(strKey, intValue);
                editor.apply();
            }
        } catch (Exception e) {
            printStackTrace(e);
        }
    }

    public static void saveBooleanPreferences(Context context, String strKey, boolean flag) {
        try {
            if (context != null) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(strKey, flag);
                editor.apply();
            }
        } catch (Exception e) {
            printStackTrace(e);
        }
    }

    public static Object getPreferences(Context context, String key, int preferenceDataType) {
        Object value = null;
        SharedPreferences sharedPreferences;
        try {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            switch (preferenceDataType) {
                case PREFTYPE_BOOLEAN:
                    value = sharedPreferences.getBoolean(key, false);
                    break;
                case PREFTYPE_INT:
                    value = sharedPreferences.getInt(key, 0);
                    break;
                case PREFTYPE_STRING:
                    value = sharedPreferences.getString(key, "");
                    break;
                case PREFTYPE_LONG:
                    value = sharedPreferences.getLong(key, 0L);
                    break;

            }
            //}
        } catch (Exception e) {
            printStackTrace(e);
            switch (preferenceDataType) {
                case PREFTYPE_BOOLEAN:
                    value = false;
                    break;
                case PREFTYPE_INT:
                    value = 0;
                    break;
                case PREFTYPE_STRING:
                    value = "";
                    break;
                case PREFTYPE_LONG:
                    value = 0L;
                    break;
            }
        }

        return value;
    }

    public static void printLog(String type, String title, String description) {
        boolean isLogDisabled = BleManager.getInstance().isLogDisabled();
        if (!isLogDisabled) {
            if (type.equalsIgnoreCase("e"))
                Log.e("" + title, "" + description);

            if (type.equalsIgnoreCase("i"))
                Log.i("" + title, "" + description);

            if (type.equalsIgnoreCase("w"))
                Log.w("" + title, "" + description);

            if (type.equalsIgnoreCase("d"))
                Log.d("" + title, "" + description);

            if (type.equalsIgnoreCase("v"))
                Log.v("" + title, "" + description);
        }
    }

    public static float celsiusTofahrenheit(float temperature) {
        NumberFormat format = NumberFormat.getInstance(new Locale("en","US"));
        String temString = format.format(temperature);
        try {
            Number number = format.parse(temString);
            temperature = number.floatValue();
            DecimalFormat decimalFormat = new DecimalFormat("##.#", new DecimalFormatSymbols(Locale.US));
            return Float.parseFloat(decimalFormat.format(temperature * 1.8 + 32));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temperature;
    }

    public static String formatDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static boolean isNetworkAvailable(Context activity) {
        try {
            if (activity == null)
                return false;

            ConnectivityManager cm =
                    (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return (activeNetwork != null &&
                    activeNetwork.isConnected());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getIdentityObject(Context context) {
        return (String) Utils.getPreferences(context, Utils.PROFILE_DATA, Utils.PREFTYPE_STRING);
    }

    public static String getPhoneIdentityObject(Context context) {
        return (String) Utils.getPreferences(context, Utils.PHONE_IDENTITY, Utils.PREFTYPE_STRING);
    }


    public static String listToString(ArrayList<TemperatureModel> tempList) {
        Gson gson = new Gson();
        return gson.toJson(tempList);
    }

    public static int genrateSecureRand() {
        SecureRandom rand = new SecureRandom();
        String id = String.format("%04d", rand.nextInt(10000));
        return Integer.parseInt(id);
    }

    public static void getCurrentVersion(Context context) {
        try {
            ApplicationInfo app = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = app.metaData;

            //printLog("e", "Ola", "" + bundle.getInt("GOQii_ACCOUNT_ID", 0) + "\n" + bundle.getString("GOQii_TOKEN"));
            Utils.saveStringPreferences(context, Utils.GOQii_ACCOUNT_ID, "" + bundle.getInt("GOQii_ACCOUNT_ID", 0));
            Utils.saveStringPreferences(context, Utils.GOQii_TOKEN, bundle.getString("GOQii_TOKEN"));

            if (((String) Utils.getPreferences(context, Utils.GOQii_ACCOUNT_ID, Utils.PREFTYPE_STRING)).equalsIgnoreCase("0"))
                Utils.saveStringPreferences(context, Utils.GOQii_ACCOUNT_ID, "20041809");
            if ((Utils.getPreferences(context, Utils.GOQii_TOKEN, Utils.PREFTYPE_STRING)) == null || TextUtils.isEmpty((String) Utils.getPreferences(context, Utils.GOQii_TOKEN, Utils.PREFTYPE_STRING)))
                Utils.saveStringPreferences(context, Utils.GOQii_TOKEN, "78yhr1d4fh5oq8cj8282j954f");


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static String createSignature(Context context) {
        String accountId = (String) Utils.getPreferences(context, Utils.GOQii_ACCOUNT_ID, Utils.PREFTYPE_STRING);
        String accountApiKey = (String) Utils.getPreferences(context, Utils.GOQii_TOKEN, Utils.PREFTYPE_STRING);
        return computeSHAHash(accountId + accountApiKey);
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    static String computeSHAHash(String text) {
        MessageDigest md;
        byte[] sha1hash = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                md.update(text.getBytes(StandardCharsets.ISO_8859_1), 0, text.length());
            }
            sha1hash = md.digest();
        } catch (Exception e) {
            printStackTrace(e);
        }

        if (sha1hash != null) {
            return convertToHex(sha1hash);
        }
        return "";
    }


    public static void getPhoneIdentity(Context context) {
        JSONObject phoneIdentity = new JSONObject();
        try {
            phoneIdentity.put("SERIAL", Build.SERIAL);
            phoneIdentity.put("MODEL", Build.MODEL);
            phoneIdentity.put("MANUFACTURE", Build.MANUFACTURER);
            phoneIdentity.put("BRAND", Build.BRAND);
            phoneIdentity.put("SDK", Build.VERSION.SDK);
            phoneIdentity.put("VERSION", Build.VERSION.RELEASE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        saveStringPreferences(context, PHONE_IDENTITY, phoneIdentity.toString());
    }

    public static String getBaseUrl(Context mContext) {
        if (mContext != null) {
            String url = (String) Utils.getPreferences(mContext.getApplicationContext(), Utils.BASE_URL, Utils.PREFTYPE_STRING);
            if (TextUtils.isEmpty(url))
                return SERVER_URL;
            else
                return url;
        } else
            return SERVER_URL;
    }

    public static void writeToFile(Context context, String data, String tableName) {
        File saveFile = null;
        try {
            File sdCardDir = getLocalDirectory(context,"Data");
            String filename = tableName + ".json";
            saveFile = new File(sdCardDir, filename);
            FileWriter fw = new FileWriter(saveFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(data);
            bw.flush();
        } catch (IOException e) {
           e.printStackTrace();
        }
        uploadOnS3(context, saveFile);
        //printLog("e", "Exported Successfully.", "Steps reached Successfully.");
    }

    private static void uploadOnS3(Context context, File saveFile) {
        String moduleType = "OlaData";
        if (saveFile != null) {
            ArrayList<FilesPreSignedUrl> urls = new ArrayList<>();
            FilesPreSignedUrl filesPreSignedUrl = new FilesPreSignedUrl();
            filesPreSignedUrl.setFile(saveFile);
            urls.add(filesPreSignedUrl);
            if (urls.size() > 0)
                new GeneratePreSignedUrlResponse().generatePreSigned_url(context, urls, moduleType);
            //printLog("e", "Exported Successfully.", "Exported Successfully.");
        }
    }

    private static File getLocalDirectory(Context context, String subDirectory) {
        File cacheDir = new File(context.getCacheDir().getPath() +
                File.separator + "GOQiiSDK");

        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        File tempDir = new File(cacheDir + File.separator + subDirectory);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return tempDir;
    }
}
