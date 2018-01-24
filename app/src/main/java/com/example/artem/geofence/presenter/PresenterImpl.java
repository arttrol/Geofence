package com.example.artem.geofence.presenter;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.example.artem.geofence.Const;
import com.example.artem.geofence.GeofenceErrorMessages;
import com.example.artem.geofence.R;
import com.example.artem.geofence.Utils;
import com.example.artem.geofence.model.GeofenceArea;
import com.example.artem.geofence.model.GeofenceAreaModel;
import com.example.artem.geofence.service.GeofenceTransitionsIntentService;
import com.example.artem.geofence.view.MainActivityView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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

/**
 * Created by Artem on 23.01.18
 */

public class PresenterImpl implements Presenter,
        OnCompleteListener<Void>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "PresenterImpl";

    private final GeofencingClient mGeofencingClient;
    private final FusedLocationProviderClient mFusedLocationClient;
    private MainActivityView mView;
    private GeofenceAreaModel mGeofenceAreaModel;
    private GeofenceArea mGeofenceArea;
    private LocationCallback mLocationCallback;
    private PendingIntent mGeofencePendingIntent;

    public PresenterImpl(MainActivityView view) {
        this.mView = view;
        mGeofenceAreaModel = new GeofenceAreaModel(mView.getContext());
        mGeofenceArea = mGeofenceAreaModel.getGeofenceArea();

        mView.populateGeofenceAreaParams(mGeofenceArea);
        mGeofencingClient = LocationServices.getGeofencingClient(mView.getContext());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mView.getContext());
        initLocationCallback();
    }


    @Override
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


    @Override
    public void onStart() {
        checkConnectedWiFi();
        PreferenceManager.getDefaultSharedPreferences(mView.getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        mView.showGeofenceAreaStatus(mGeofenceAreaModel.isInside());
        mView.checkLocationSettings();
    }

    @Override
    public void onPause() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onStop() {
        PreferenceManager.getDefaultSharedPreferences(mView.getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressLint("MissingPermission")
    private void startGeofenceAreaObservation() {
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
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


    @Override
    public LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Const.LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Const.LOCATION_FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
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


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mView.getContext(), GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling checkLocationPermissions() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(mView.getContext().getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }


    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            mView.showSnackbar(mView.getContext().getString(R.string.snackbar_geofence_info,
                    mGeofenceArea.getLatitude(),
                    mGeofenceArea.getLongitude(),
                    mGeofenceArea.getRadius()));
            Log.d(TAG, "geofence added " + mGeofenceArea.toString());
        } else {
            mView.showSnackbar(R.string.snackbar_geofence_add_error);
            String errorMessage = GeofenceErrorMessages.getErrorString(mView.getContext(), task.getException());
            Log.w(TAG, errorMessage);
        }
    }


    private void checkConnectedWiFi() {
        String wiFiName = Utils.getWiFiName(mView.getContext());
        mGeofenceAreaModel.setConnectedToWiFi(wiFiName);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(GeofenceAreaModel.PREF_GEOFENCE_STATUS)) {
            mView.showGeofenceAreaStatus(mGeofenceAreaModel.isInside());
        }
    }
}
