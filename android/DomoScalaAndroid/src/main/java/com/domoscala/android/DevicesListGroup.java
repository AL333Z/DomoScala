package com.domoscala.android;


import java.util.ArrayList;
import java.util.List;

public class DevicesListGroup {

    public final String room;
    public final List<DevicesListItem> deviceItems = new ArrayList<>();

    public DevicesListGroup(String room){
        this.room = room;
    }


    public class DevicesListItem {

        public final String deviceName;
        public final String deviceType;

        /**
         * This is for example "21,3 °C"
         */
        public String currentValue = "";

        public DevicesListItem(String deviceName, String deviceType){
            this.deviceName = deviceName;
            this.deviceType = deviceType;
        }
    }
}
