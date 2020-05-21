package com.goqii.goqiisdk.model;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/28.
 */

public class DeviceBean {
    private List<Map<String,String>> dataList;
    private boolean finish;

    public List<Map<String, String>> getDataList() {
        return dataList;
    }

    public void setDataList(List<Map<String, String>> dataList) {
        this.dataList = dataList;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }
}
