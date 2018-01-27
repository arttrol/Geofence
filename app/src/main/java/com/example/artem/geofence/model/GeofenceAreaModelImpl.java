package com.example.artem.geofence.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.artem.geofence.Const;
import com.example.artem.geofence.Utils;

/**
 * Created by Artem on 23.01.18
 */

public class GeofenceAreaModelImpl implements GeofenceAreaModel {
    private static final String PREF_GEOFENCE_LATITUDE = "PREF_GEOFENCE_LATITUDE";
    private static final String PREF_GEOFENCE_LONGITUDE = "PREF_GEOFENCE_LONGITUDE";
    private static final String PREF_GEOFENCE_RADIUS = "PREF_GEOFENCE_RADIUS";
    private static final String PREF_CONNECTED_WIFI_NAME = "PREF_CONNECTED_WIFI_NAME";
    private static final String PREF_IS_IN_GEO_AREA = "PREF_IS_IN_GEO_AREA";
    private SharedPreferences mPreferences;
    private Context mContext;
    private GeofenceArea mGeofenceArea;

    public GeofenceAreaModelImpl(Context context) {
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
        checkConnectedToWiFi();
    }

    public boolean isInside() {
        return mPreferences.getBoolean(PREF_GEOFENCE_STATUS, false);
    }

    public String getConnectedWiFiName() {
        return mPreferences.getString(PREF_CONNECTED_WIFI_NAME, "");
    }

    public void setConnectedWiFiName(String wiFiName) {
        mPreferences.edit()
                .putString(PREF_CONNECTED_WIFI_NAME, wiFiName)
                .apply();
        checkConnectedToWiFi();
    }

    private void checkConnectedToWiFi() {
        boolean isInGeoArea = mPreferences.getBoolean(PREF_IS_IN_GEO_AREA, false);
        boolean isInside = isConnectedToSpecifiedWiFi() || isInGeoArea;
        if (isInside() != isInside) {
            mPreferences
                    .edit()
                    .putBoolean(PREF_GEOFENCE_STATUS, isInside)
                    .apply();

            notifyGeofenceStatus(isInside);
        }
    }

    private boolean isConnectedToSpecifiedWiFi() {
        return getGeofenceArea().getWifiName().equals(getConnectedWiFiName());
    }

    public void setInGeoArea(boolean isInGeoArea) {
        boolean isInside = isConnectedToSpecifiedWiFi() || isInGeoArea;
        SharedPreferences.Editor edit = mPreferences.edit();
        edit.putBoolean(PREF_IS_IN_GEO_AREA, isInGeoArea);

        if (isInside() != isInside) {
            edit.putBoolean(PREF_GEOFENCE_STATUS, isInside).apply();
            notifyGeofenceStatus(isInside);
        } else {
            edit.apply();
        }
    }

    private void notifyGeofenceStatus(boolean isInside) {
        Utils.sendGeofenceStatusNotification(mContext, isInside);
    }
}
