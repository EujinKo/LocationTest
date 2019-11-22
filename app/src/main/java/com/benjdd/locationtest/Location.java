package com.benjdd.locationtest;

import java.util.ArrayList;

public class Location {

    public String locationName = "";
    public String locationColor = "";
    public ArrayList<double[]> locations = new ArrayList<double[]>();

    public Location(String locationInfo) {
        String[] sp = locationInfo.split("\\|");
        locationName = sp[0];
        locationColor = sp[1];
        for (int i = 2; i < sp.length; i++) {
            double lat = Double.parseDouble(sp[i].split(",")[0]);
            double lng = Double.parseDouble(sp[i].split(",")[1]);
            double[] latLng = {lat, lng};
            locations.add(latLng);
        }
    }
}
