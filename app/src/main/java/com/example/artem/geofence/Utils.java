package com.example.artem.geofence;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.artem.geofence.model.GeofenceArea;

/**
 * Created by Artem on 23.01.18
 */

public class Utils {

    private static final String PREF_GEOFENCE_LATITUDE = "PREF_GEOFENCE_LATITUDE";
    private static final String PREF_GEOFENCE_LONGITUDE = "PREF_GEOFENCE_LONGITUDE";
    private static final String PREF_GEOFENCE_RADIUS = "PREF_GEOFENCE_RADIUS";
    private static final String PREF_GEOFENCE_WIFI_NAME = "PREF_GEOFENCE_WIFI_NAME";

    public static void setGeofenceArea(Context context, GeofenceArea geofenceArea) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putFloat(PREF_GEOFENCE_LATITUDE, geofenceArea.getLatitude())
                .putFloat(PREF_GEOFENCE_LONGITUDE, geofenceArea.getLongitude())
                .putFloat(PREF_GEOFENCE_RADIUS, geofenceArea.getRadius())
                .putString(PREF_GEOFENCE_WIFI_NAME, geofenceArea.getWifiName())
                .apply();
    }

    public static GeofenceArea getGeofenceArea(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        GeofenceArea geofenceArea = new GeofenceArea();
        geofenceArea.setLatitude(sharedPreferences.getFloat(PREF_GEOFENCE_LATITUDE, Const.DEF_LATITUDE));
        geofenceArea.setLongitude(sharedPreferences.getFloat(PREF_GEOFENCE_LONGITUDE, Const.DEF_LONGITUDE));
        geofenceArea.setRadius(sharedPreferences.getFloat(PREF_GEOFENCE_RADIUS, Const.DEF_RADIUS));
        geofenceArea.setWifiName(sharedPreferences.getString(PREF_GEOFENCE_WIFI_NAME, Const.DEF_WIFI_NAME));
        return geofenceArea;
    }
}
