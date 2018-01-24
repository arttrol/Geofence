package com.example.artem.geofence.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.artem.geofence.Const;
import com.example.artem.geofence.Utils;
import com.example.artem.geofence.model.GeofenceArea;

/**
 * Created by Artem on 23.01.18
 */

public class GeofenceAreaModel {
    public static final String PREF_GEOFENCE_STATUS = "PREF_GEOFENCE_STATUS";
    private static final String PREF_GEOFENCE_LATITUDE = "PREF_GEOFENCE_LATITUDE";
    private static final String PREF_GEOFENCE_LONGITUDE = "PREF_GEOFENCE_LONGITUDE";
    private static final String PREF_GEOFENCE_RADIUS = "PREF_GEOFENCE_RADIUS";
    private static final String PREF_GEOFENCE_WIFI_NAME = "PREF_GEOFENCE_WIFI_NAME";
    private static final String PREF_CONNECTED_TO_SPECIFIED_WIFI = "PREF_CONNECTED_TO_SPECIFIED_WIFI";
    private static final String PREF_IS_IN_GEO_AREA = "PREF_IS_IN_GEO_AREA";
    private static final String TAG = "GeofenceAreaModel";
    private SharedPreferences mPreferences;
    private Context mContext;
    private GeofenceArea mGeofenceArea;

    public GeofenceAreaModel(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }

    public GeofenceArea getGeofenceArea() {
        if (mGeofenceArea == null) {
            mGeofenceArea = new GeofenceArea();
            mGeofenceArea.setLatitude(mPreferences.getFloat(PREF_GEOFENCE_LATITUDE, Const.DEF_LATITUDE));
            mGeofenceArea.setLongitude(mPreferences.getFloat(PREF_GEOFENCE_LONGITUDE, Const.DEF_LONGITUDE));
            mGeofenceArea.setRadius(mPreferences.getFloat(PREF_GEOFENCE_RADIUS, Const.DEF_RADIUS));
            mGeofenceArea.setWifiName(mPreferences.getString(PREF_GEOFENCE_WIFI_NAME, Const.DEF_WIFI_NAME));
        }
        return mGeofenceArea;
    }

    public void setGeofenceArea(GeofenceArea geofenceArea) {
        mGeofenceArea = geofenceArea;
        mPreferences
                .edit()
                .putFloat(PREF_GEOFENCE_LATITUDE, geofenceArea.getLatitude())
                .putFloat(PREF_GEOFENCE_LONGITUDE, geofenceArea.getLongitude())
                .putFloat(PREF_GEOFENCE_RADIUS, geofenceArea.getRadius())
                .putString(PREF_GEOFENCE_WIFI_NAME, geofenceArea.getWifiName())
                .apply();
    }

    public boolean isInside() {
        return mPreferences.getBoolean(PREF_GEOFENCE_STATUS, false);
    }

    public void setConnectedToWiFi(String wiFiName) {
        boolean isConnectedToSpecifiedWiFi = getGeofenceArea().getWifiName().equals(wiFiName);
        boolean isInGeoArea = mPreferences.getBoolean(PREF_IS_IN_GEO_AREA, false);
        boolean isInside = isConnectedToSpecifiedWiFi || isInGeoArea;
        mPreferences
                .edit()
                .putBoolean(PREF_CONNECTED_TO_SPECIFIED_WIFI, isConnectedToSpecifiedWiFi)
                .putBoolean(PREF_GEOFENCE_STATUS, isInside)
                .apply();

        notifyGeofenceStatus(isInside);
    }

    public void setInGeoArea(boolean isInGeoArea) {
        boolean isConnectedToSpecifiedWiFi = mPreferences.getBoolean(PREF_CONNECTED_TO_SPECIFIED_WIFI, false);
        boolean isInside = isConnectedToSpecifiedWiFi || isInGeoArea;
        mPreferences
                .edit()
                .putBoolean(PREF_IS_IN_GEO_AREA, isInGeoArea)
                .putBoolean(PREF_GEOFENCE_STATUS, isInside)
                .apply();

        notifyGeofenceStatus(isInside);
    }

    private void notifyGeofenceStatus(boolean isInside) {
        Utils.sendGeofenceStatusNotification(mContext, isInside);
    }
}
