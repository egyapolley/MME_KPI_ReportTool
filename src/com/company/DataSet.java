package com.company;

import java.util.List;
import java.util.Map;

public class DataSet {

    private String dataTime;
    private String file_name;
    private List<Map<String, List<String>>> mapList;

    public DataSet(String dataTime, String file_name, List<Map<String, List<String>>> mapList) {
        this.dataTime = dataTime;
        this.file_name = file_name;
        this.mapList = mapList;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public List<Map<String, List<String>>> getMapList() {
        return mapList;
    }

    public void setMapList(List<Map<String, List<String>>> mapList) {
        this.mapList = mapList;
    }

    @Override
    public String toString() {
        return "DataSet{" +
                "dataTime='" + dataTime + '\'' +
                ", file_name='" + file_name + '\'' +
                ", mapList=" + mapList +
                '}';
    }
}
