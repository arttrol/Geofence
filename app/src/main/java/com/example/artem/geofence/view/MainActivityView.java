package com.example.artem.geofence.view;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.support.annotation.StringRes;
import android.view.View;

import com.example.artem.geofence.model.GeofenceArea;

/**
 * Created by Artem on 23.01.18
 */

public interface MainActivityView {

    void showGeofenceAreaStatus(boolean inside);

    void populateGeofenceAreaParams(GeofenceArea geofenceArea);

    void onLocationChanged(Location location);

    void hideKeyboard();

    GeofenceArea readGeofenceArea();

    void showSnackbar(@StringRes int stringResId, Object... args);

    void checkLocationSettings();

    void checkLocationPermissions();

    View getSnackbarContainer();

    PendingIntent getGeofencePendingIntent();
}
