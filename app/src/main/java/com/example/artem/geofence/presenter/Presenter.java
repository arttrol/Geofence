package com.example.artem.geofence.presenter;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.example.artem.geofence.Const;
import com.example.artem.geofence.GeofenceApplication;
import com.example.artem.geofence.GeofenceErrorMessages;
import com.example.artem.geofence.R;
import com.example.artem.geofence.model.GeofenceArea;
import com.example.artem.geofence.model.GeofenceAreaModel;
import com.example.artem.geofence.view.MainActivityView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;

import javax.inject.Inject;

/**
 * Created by Artem on 23.01.18
 */

public class Presenter implements OnCompleteListener<Void>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "Presenter";
    private static final String USER_GEOFENCE_ID = "USER_GEOFENCE_ID";

    @Inject
    GeofenceAreaModel mGeofenceAreaModel;

    @Inject
    GeofencingClient mGeofencingClient;

    @Inject
    FusedLocationProviderClient mFusedLocationClient;

    private MainActivityView mView;
    private GeofenceArea mGeofenceArea;
    private LocationCallback mLocationCallback;


    public Presenter() {
        GeofenceApplication.getAppComponent().inject(this);
        mGeofenceArea = mGeofenceAreaModel.getGeofenceArea();
    }

    public void setView(MainActivityView view) {
        mView = view;
        initLocationCallback();
    }


    public void onApplyClick() {
        mView.hideKeyboard();
        try {
            mGeofenceArea = mView.readGeofenceArea();
            mGeofenceAreaModel.setGeofenceArea(mGeofenceArea);
        } catch (NullPointerException | NumberFormatException e) {
            mView.showSnackbar(R.string.snackbar_fill_all_fields);
        }
        mView.checkLocationPermissions();
    }


    public void onResume() {
        mView.populateGeofenceAreaParams(mGeofenceArea);
        mView.showGeofenceAreaStatus(mGeofenceAreaModel.isInside());
        mView.checkLocationSettings();
    }

    public void onPause() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    private void startGeofenceAreaObservation() {
        mGeofencingClient.addGeofences(getGeofencingRequest(), mView.getGeofencePendingIntent())
                .addOnCompleteListener(this);

        // Transition for new geofence (by last known location) will not send.
        // So we have to check last known location and distance to it.
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Location dest = new Location(location);
                            dest.setLatitude(mGeofenceArea.getLatitude());
                            dest.setLongitude(mGeofenceArea.getLongitude());
                            mGeofenceAreaModel.setInGeoArea(
                                    location.distanceTo(dest) <= mGeofenceArea.getRadius());
                            mView.onLocationChanged(location);
                        }
                    }
                });
        mFusedLocationClient.requestLocationUpdates(getLocationRequest(),
                mLocationCallback,
                null);
    }


    public LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Const.LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Const.LOCATION_FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public PermissionListener getPermissionListener() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                startGeofenceAreaObservation();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                //no-op

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };

        PermissionListener snackbarPermissionListener = SnackbarOnDeniedPermissionListener.Builder
                .with(mView.getSnackbarContainer(), R.string.snackbar_message_enable_location_access)
                .withOpenSettingsButton(R.string.snackbar_btn_settings)
                .withDuration(Snackbar.LENGTH_INDEFINITE)
                .build();

        return new CompositePermissionListener(permissionListener, snackbarPermissionListener);
    }

    private GeofencingRequest getGeofencingRequest() {
        Geofence geofence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this geofence.
                .setRequestId(USER_GEOFENCE_ID)
                .setCircularRegion(mGeofenceArea.getLatitude(),
                        mGeofenceArea.getLongitude(),
                        mGeofenceArea.getRadius())
                .setExpirationDuration(Const.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }


    private void initLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    mView.onLocationChanged(location);
                }
            }
        };
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            mView.showSnackbar(R.string.snackbar_geofence_info,
                    mGeofenceArea.getLatitude(),
                    mGeofenceArea.getLongitude(),
                    mGeofenceArea.getRadius());
            Log.d(TAG, "geofence added " + mGeofenceArea.toString());
        } else {
            mView.showSnackbar(GeofenceErrorMessages.getErrorStringRes(task.getException()));
        }
    }

    public void setConnectedToWiFi(String wiFiName) {
        mGeofenceAreaModel.setConnectedWiFiName(wiFiName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(GeofenceAreaModel.PREF_GEOFENCE_STATUS)) {
            mView.showGeofenceAreaStatus(mGeofenceAreaModel.isInside());
        }
    }
}
