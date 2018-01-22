package com.example.artem.geofence.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.artem.geofence.Const;
import com.example.artem.geofence.R;
import com.example.artem.geofence.intent.GeofenceTransitionsIntentService;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
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

    @BindView(R.id.editTextLatitude)
    EditText mEditTextLatitude;

    @BindView(R.id.editTextLongitude)
    EditText mEditTextLongitude;

    @BindView(R.id.editTextRadius)
    EditText mEditTextRadius;

    private CompositePermissionListener mPermissionListener;
    private PendingIntent mGeofencePendingIntent;
    private GeofencingClient mGeofencingClient;
    private Geofence mGeofence;

    @OnClick(R.id.buttonApply)
    void onApplyClick() {
        configGeofence();
        addGeofence();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        configGeofence();
        mGeofencingClient = LocationServices.getGeofencingClient(this);
        initPermissionListeners();
        addGeofence();
    }


    @Override
    public void onComplete(@NonNull Task<Void> task) {
        Log.d(TAG, "onComplete: " + task.isSuccessful());
        // TODO: High 22.01.18
    }

    private void showSnackbar(String message) {
        Snackbar.make(mContainer, message, Snackbar.LENGTH_LONG).show();
    }

    private void configGeofence() {
        double lat;
        double lon;
        float radius;
        try {
            lat = Double.parseDouble(mEditTextLatitude.getText().toString());
            lon = Double.parseDouble(mEditTextLongitude.getText().toString());
            radius = Float.parseFloat(mEditTextRadius.getText().toString());
        } catch (NullPointerException e) {
            showSnackbar(getString(R.string.snackbar_fill_all_fields));
            return;
        }

        mGeofence = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this geofence.
                .setRequestId(USER_GEOFENCE_ID)
                .setCircularRegion(lat, lon, radius)
                .setExpirationDuration(Const.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        showSnackbar(getString(R.string.snackbar_geofence_info, lat, lon, radius));
    }

    private void initPermissionListeners() {
        PermissionListener permissionListener = new PermissionListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
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
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(mGeofence);
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

}
