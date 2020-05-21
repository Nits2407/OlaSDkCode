package com.goqii.goqiisdk.ble.CommandFiles;


import com.goqii.goqiisdk.ble.BleManager;
import com.goqii.goqiisdk.model.AutomicHeart;
import com.goqii.goqiisdk.model.BondModel;
import com.goqii.goqiisdk.model.SetTime;
import com.goqii.goqiisdk.util.Utils;

import java.util.Calendar;

public class CommandSendRequest {


    public static void sendBindingRequestToV3Band() {
        int passCode = Utils.genrateSecureRand();
        int p2 = passCode % 1000;
        int p3 = p2 % 100;
        int p4 = p3 % 10;
        BondModel bondModel = new BondModel();
        bondModel.setP1(passCode / 1000);
        bondModel.setP2(p2 / 100);
        bondModel.setP3(p3 / 10);
        bondModel.setP4(p4);

        byte[] value = new byte[16];
        value[0] = DeviceConst.CMD_SEND_BINDING_REQUEST;
        value[1] = (byte) bondModel.getP1();
        value[2] = (byte) bondModel.getP2();
        value[3] = (byte) bondModel.getP3();
        value[4] = (byte) bondModel.getP4();
        sendCommand(value);
        BleManager.getInstance().bondRequestTimer();
    }

    public static void getBandBatteryStatus() {
        byte[] value = new byte[16];
        value[0] = DeviceConst.CMD_Get_BatteryLevel;
        sendCommand(value);
    }

    public static void setDateTimeToBand() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        SetTime setTime = new SetTime();
        setTime.setYear(year);
        setTime.setMonth(month);
        setTime.setDay(day);
        setTime.setHour(hour);
        setTime.setMinute(min);
        setTime.setSecond(second);
        setTime.setWeekDay(weekDay);

        byte[] value = new byte[16];
        value[0] = DeviceConst.CMD_SET_TIME;
        value[1] = ResolveUtil.getTimeValue(year);
        value[2] = ResolveUtil.getTimeValue(month);
        value[3] = ResolveUtil.getTimeValue(day);
        value[4] = ResolveUtil.getTimeValue(hour);
        value[5] = ResolveUtil.getTimeValue(min);
        value[6] = ResolveUtil.getTimeValue(second);
        value[7] = (byte) weekDay;
        int timeZone = ResolveUtil.getTimeZone();
        // this is not a standard documentation
        if (timeZone > 0) {
            value[9] = (byte) (0x80 + timeZone / 256);
            value[8] = (byte) (timeZone % 256);
        } else {
            //don't remove - sign
            value[9] = (byte) (-timeZone / 256);
            value[8] = (byte) (-timeZone % 256);
        }
        sendCommand(value);
    }

    private static void sendCommand(byte[] value) {
        byte crc = 0;
        for (int i = 0; i < value.length - 1; i++) {
            crc += value[i];
        }
        value[value.length - 1] = (byte) (crc & 0xff);
        BleManager.getInstance().writeValue(value);
    }

    public static void setRealTimeMode(boolean enable) {
        byte[] value = new byte[16];
        value[0] = DeviceConst.CMD_START_REAL_TIME;
        if (enable) {
            value[1] = 1;
            //value[2] = 1;
        } else {
            value[1] = 0;
            value[2] = 0;
        }
        sendCommand(value);
    }

    public static void switchHeartRateClicked(boolean checked) {
        AutomicHeart automicHeart = new AutomicHeart();
        if (checked) {
            automicHeart.setOpen(2);
        } else {
            automicHeart.setOpen(0);
        }
        automicHeart.setStartHour(0);
        automicHeart.setStartMinute(0);
        automicHeart.setEndHour(23);
        automicHeart.setEndMinute(55);
        automicHeart.setWeek(127);
        automicHeart.setTime(1);

        byte[] value = new byte[16];
        int time = automicHeart.getTime();
        value[0] = DeviceConst.CMD_Set_AutoHeart;
        value[1] = (byte) automicHeart.getOpen();
        value[2] = ResolveUtil.getTimeValue(automicHeart.getStartHour());
        value[3] = ResolveUtil.getTimeValue(automicHeart.getStartMinute());
        value[4] = ResolveUtil.getTimeValue(automicHeart.getEndHour());
        value[5] = ResolveUtil.getTimeValue(automicHeart.getEndMinute());
        value[6] = (byte) automicHeart.getWeek();
        value[7] = (byte) (time & 0xff);

        sendCommand(value);
    }
}
