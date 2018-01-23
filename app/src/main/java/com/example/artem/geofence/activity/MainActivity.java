package com.example.artem.geofence.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.artem.geofence.Const;
import com.example.artem.geofence.GeofenceErrorMessages;
import com.example.artem.geofence.R;
import com.example.artem.geofence.Utils;
import com.example.artem.geofence.model.GeofenceArea;
import com.example.artem.geofence.service.GeofenceTransitionsIntentService;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnCompleteListener<Void> {

    private static final String TAG = "MainActivity";
    private static final String USER_GEOFENCE_ID = "USER_GEOFENCE_ID";

    @BindView(R.id.activity_main)
    View mContainer;

    @BindView(R.id.edit_text_latitude)
    EditText mEditTextLatitude;

    @BindView(R.id.edit_text_longitude)
    EditText mEditTextLongitude;

    @BindView(R.id.edit_text_radius)
    EditText mEditTextRadius;

    @BindView(R.id.edit_text_wi_fi)
    EditText mEditTextWiFi;

    @BindView(R.id.text_view_status)
    TextView mTextViewStatus;

    @BindView(R.id.text_view_log)
    TextView mTextViewLog;

    private CompositePermissionListener mPermissionListener;
    private PendingIntent mGeofencePendingIntent;
    private GeofencingClient mGeofencingClient;
    private GeofenceArea mGeofenceArea;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private OnSuccessListener<Location> onLastLocationSuccessListener;

    @OnClick(R.id.button_apply)
    void onApplyClick() {
        hideKeyboard();
        configGeofence();
        addGeofence();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mGeofencingClient = LocationServices.getGeofencingClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGeofenceArea = Utils.getGeofenceArea(this);

        populateGeofenceAreaParams();
        initLocationCallback();
        initPermissionListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addGeofence();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            showSnackbar(getString(R.string.snackbar_geofence_info,
                    mGeofenceArea.getLatitude(),
                    mGeofenceArea.getLongitude(),
                    mGeofenceArea.getRadius()));
            Log.d(TAG, "geofence added " + mGeofenceArea.toString());
        } else {
            showSnackbar(R.string.snackbar_geofence_add_error);
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void populateGeofenceAreaParams() {
        mEditTextLatitude.setText(String.valueOf(mGeofenceArea.getLatitude()));
        mEditTextLongitude.setText(String.valueOf(mGeofenceArea.getLongitude()));
        mEditTextRadius.setText(String.valueOf(mGeofenceArea.getRadius()));
        mEditTextWiFi.setText(mGeofenceArea.getWifiName());
    }

    private void showSnackbar(@StringRes int resId) {
        showSnackbar(getString(resId));
    }

    private void showSnackbar(String message) {
        Snackbar.make(mContainer, message, Snackbar.LENGTH_LONG).show();
    }

    private void configGeofence() {
        try {
            float lat = Float.parseFloat(mEditTextLatitude.getText().toString());
            float lon = Float.parseFloat(mEditTextLongitude.getText().toString());
            float radius = Float.parseFloat(mEditTextRadius.getText().toString());
            String wifi = mEditTextWiFi.getText().toString();
            mGeofenceArea = new GeofenceArea(lat, lon, radius, wifi);
            Utils.setGeofenceArea(this, mGeofenceArea);
        } catch (NullPointerException e) {
            showSnackbar(getString(R.string.snackbar_fill_all_fields));
        }
    }

    private void initPermissionListeners() {
        PermissionListener permissionListener = new PermissionListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(onLastLocationSuccessListener);
                mFusedLocationClient.requestLocationUpdates(getLocationRequest(),
                        mLocationCallback,
                        null);
                mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                        .addOnCompleteListener(MainActivity.this);
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
                .with(mContainer, R.string.snackbar_message_enable_location_access)
                .withOpenSettingsButton(R.string.snackbar_btn_settings)
                .withDuration(Snackbar.LENGTH_INDEFINITE)
                .build();

        mPermissionListener = new CompositePermissionListener(permissionListener, snackbarPermissionListener);
    }

    private void addGeofence() {
        Dexter.withActivity(MainActivity.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(mPermissionListener)
                .check();
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

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofence() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Const.LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Const.LOCATION_FASTEST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setMaxWaitTime(Const.LOCATION_MAX_WAIT_TIME);
        return locationRequest;
    }


    private void initLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    logLocation(location);
                }
            }
        };

        onLastLocationSuccessListener = new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    logLocation(location);
                }
            }
        };
    }

    private void logLocation(Location location) {
        String log = "Current lat: " + location.getLatitude() + " lon: " + location.getLongitude();
        mTextViewLog.setText(log);
        Log.d(TAG, log);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
