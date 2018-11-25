package com.inha.wifidetector;

public class WifiData {
    int RSSI;
    double latitude;
    double longitude;
    String MAC;
    String AP_MAC;

    String room;
    int corner;
    String time;

    protected WifiData clone(){
        WifiData data = new WifiData();
        data.RSSI = this.RSSI;
        data.latitude = this.latitude;
        data.longitude = this.longitude;
        data.MAC = this.MAC;
        data.AP_MAC = this.AP_MAC;

        data.room = this.room;
        data.corner = this.corner;
        data.time = this.time;

        return data;
    }
}
