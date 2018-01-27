package com.example.artem.geofence.model;

/**
 * Created by Artem on 23.01.18
 */

public interface GeofenceAreaModel {

    String PREF_GEOFENCE_STATUS = "PREF_GEOFENCE_STATUS";
    String PREF_GEOFENCE_WIFI_NAME = "PREF_GEOFENCE_WIFI_NAME";

    GeofenceArea getGeofenceArea();

    void setGeofenceArea(GeofenceArea geofenceArea);

    boolean isInside();

    void setConnectedWiFiName(String wiFiName);

    String getConnectedWiFiName();

    void setInGeoArea(boolean isInGeoArea);
}
