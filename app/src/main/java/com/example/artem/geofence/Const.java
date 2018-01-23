package com.example.artem.geofence;

/**
 * Created by Artem on 22.01.18
 */

public interface Const {

    long GEOFENCE_EXPIRATION_IN_MILLISECONDS = 1000 * 60 * 60 * 24;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    long LOCATION_UPDATE_INTERVAL = 4 * 1000; // 4 sec

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    long LOCATION_FASTEST_UPDATE_INTERVAL = 2 * 1000; // 2 sec

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    long LOCATION_MAX_WAIT_TIME = LOCATION_UPDATE_INTERVAL * 2;

    float DEF_LATITUDE = 50.45f;
    float DEF_LONGITUDE = 30.524f;
    float DEF_RADIUS = 100f;
    String DEF_WIFI_NAME = "Xiaomi_FF24_5G";
}
