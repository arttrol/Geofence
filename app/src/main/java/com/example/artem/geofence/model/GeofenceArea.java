package com.example.artem.geofence.model;

/**
 * Created by Artem on 23.01.18.
 * Geofence area is defined as a combination of some geographic point, radius,
 * and specific Wifi network name.
 */

@SuppressWarnings("WeakerAccess")
public class GeofenceArea {
    private float latitude;
    private float longitude;
    private float radius;
    private String wifiName;

    public GeofenceArea() {

    }

    public GeofenceArea(float latitude, float longitude, float radius, String wifiName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.wifiName = wifiName;
    }


    @Override
    public String toString() {
        return super.toString()
                + " Lat: " + latitude
                + " Lon: " + longitude
                + " radius: " + radius + "m."
                + " wifi name: " + wifiName;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    /**
     * @return radius in meters
     */
    public float getRadius() {
        return radius;
    }

    /**
     * @param radius in meters
     */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

}
