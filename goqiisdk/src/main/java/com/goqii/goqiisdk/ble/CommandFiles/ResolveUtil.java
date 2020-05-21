package com.goqii.goqiisdk.ble.CommandFiles;


import com.goqii.goqiisdk.util.Utils;

import java.text.NumberFormat;
import java.util.Date;
import java.util.TimeZone;


public class ResolveUtil {

    static String getDeviceBattery(byte[] value) {
        int battery = getValue(value[1], 0);
        return String.valueOf(battery);
    }

    static String[] getActivityData(byte[] value) {
        if (value.length > 4) {
            String[] activityData = new String[6];
            int step = 0;
            float cal = 0;
            float distance = 0;
            int time = 0;
            int heart;
            float temp = 0;
            for (int i = 1; i < 5; i++) {
                step += getValue(value[i], i - 1);
            }
            for (int i = 5; i < 9; i++) {
                cal += getValue(value[i], i - 5);
            }
            for (int i = 9; i < 13; i++) {
                distance += getValue(value[i], i - 9);
            }
            for (int i = 13; i < 17; i++) {
                time += getValue(value[i], i - 13);
            }
            if (value.length > 20) {
                heart = getValue(value[21], 0);
                for (int i = 22; i < 24; i++) {
                    temp += getValue(value[i], i - 22);
                }
            } else {
                heart = getValue(value[17], 0);
                for (int i = 18; i < 20; i++) {
                    temp += getValue(value[i], i - 18);
                }
            }

            NumberFormat numberFormat = NumberFormat.getNumberInstance();
            numberFormat.setMinimumFractionDigits(2);
            activityData[0] = String.valueOf(step);
            activityData[1] = numberFormat.format(cal / 100);
            activityData[2] = numberFormat.format(distance / 100);
            activityData[3] = String.valueOf(time);
            activityData[4] = String.valueOf(heart);
            numberFormat.setMinimumFractionDigits(1);
            activityData[5] = numberFormat.format(Utils.celsiusTofahrenheit(temp / 10));

            return activityData;
        }
        return null;
    }

    private static int getValue(byte b, int count) {
        return (int) ((b & 0xff) * Math.pow(256, count));
    }

    static byte getTimeValue(int value) {
        String data = value + "";
        int m = Integer.parseInt(data, 16);
        return (byte) m;
    }

    static int getTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
//Import part : x.0 for double number
        double offsetFromUtc = tz.getOffset(now.getTime()) / 3600000.0;
        return (int) (offsetFromUtc * 60);
    }


}
