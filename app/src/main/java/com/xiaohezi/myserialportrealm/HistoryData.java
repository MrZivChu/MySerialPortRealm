package com.xiaohezi.myserialportrealm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class HistoryData extends RealmObject{
    @PrimaryKey
    private int ID;
    private int ProbeID;
    private float GasValue;
    private String TimeStamp;

    public HistoryData(int id,int probeID,float gasValue,String timeStamp){
        setID(id);
        setProbeID(probeID);
        setGasValue(gasValue);
        setTimeStamp(timeStamp);
    }

    public HistoryData() {}

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getProbeID() {
        return ProbeID;
    }

    public void setProbeID(int probeID) {
        ProbeID = probeID;
    }

    public float getGasValue() {
        return GasValue;
    }

    public void setGasValue(float gasValue) {
        GasValue = gasValue;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        TimeStamp = timeStamp;
    }
}
