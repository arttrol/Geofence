package com.example.artem.geofence;

/**
 * Created by Artem on 22.01.18
 */

public interface Const {

    long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 1000 * 60 * 60 * 24;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    long LOCATION_UPDATE_INTERVAL = 10 * 1000; // 10 sec

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    long LOCATION_FASTEST_UPDATE_INTERVAL = 5 * 1000; // 5 sec

    float DEF_LATITUDE = 50.45f;
    float DEF_LONGITUDE = 30.524f;
    float DEF_RADIUS = 11000f;
    String DEF_WIFI_NAME = "WiredSSID";//"Xiaomi_FF24";
}
