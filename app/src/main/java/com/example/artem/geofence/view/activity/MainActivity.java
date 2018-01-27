package com.example.artem.geofence.view.activity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.artem.geofence.GeofenceApplication;
import com.example.artem.geofence.R;
import com.example.artem.geofence.Utils;
import com.example.artem.geofence.broadcast.WiFiBroadcastReceiver;
import com.example.artem.geofence.model.GeofenceArea;
import com.example.artem.geofence.presenter.Presenter;
import com.example.artem.geofence.service.GeofenceTransitionsIntentService;
import com.example.artem.geofence.view.MainActivityView;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainActivityView {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CHECK_SETTINGS = 100;

    @Inject
    Presenter mPresenter;

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

    /**
     * Since api version 26 apps can no longer register broadcast receivers for implicit
     * broadcasts in their manifest. We have to register it from context
     * https://developer.android.com/guide/components/broadcast-exceptions.html
     */
    private WiFiBroadcastReceiver wiFiBroadcastReceiver;
    private PendingIntent mGeofencePendingIntent;

    @OnClick(R.id.button_apply)
    void onApplyClick() {
        mPresenter.onApplyClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        GeofenceApplication.getAppComponent().inject(this);

        mPresenter.setView(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wiFiBroadcastReceiver = new WiFiBroadcastReceiver();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkConnectedWiFi();
        startWiFiBroadcast();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(mPresenter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopWiFiBroadcast();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(mPresenter);
    }

    /**
     * If system location settings passed for getLocationRequest params - add geofence,
     * otherwise - show system dialog
     */
    @Override
    public void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mPresenter.getLocationRequest());
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequest);

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                checkLocationPermissions();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // no-op
                    }
                }
            }
        });
    }

    @Override
    public void showGeofenceAreaStatus(boolean inside) {
        mTextViewStatus.setText(inside ? R.string.status_inside : R.string.status_outside);
        mTextViewStatus.setTextColor(inside ? Color.GREEN : Color.RED);
    }

    @Override
    public void populateGeofenceAreaParams(GeofenceArea geofenceArea) {
        mEditTextLatitude.setText(String.valueOf(geofenceArea.getLatitude()));
        mEditTextLongitude.setText(String.valueOf(geofenceArea.getLongitude()));
        mEditTextRadius.setText(String.valueOf(geofenceArea.getRadius()));
        mEditTextWiFi.setText(geofenceArea.getWifiName());
    }

    @Override
    public void showSnackbar(@StringRes int resId, Object... args) {
        Snackbar.make(getSnackbarContainer(), getString(resId, args), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public GeofenceArea readGeofenceArea() {
        float lat = Float.parseFloat(mEditTextLatitude.getText().toString());
        float lon = Float.parseFloat(mEditTextLongitude.getText().toString());
        float radius = Float.parseFloat(mEditTextRadius.getText().toString());
        String wifi = mEditTextWiFi.getText().toString();
        return new GeofenceArea(lat, lon, radius, wifi);
    }

    @Override
    public void checkLocationPermissions() {
        Dexter.withActivity(MainActivity.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(mPresenter.getPermissionListener())
                .check();
    }

    @Override
    public View getSnackbarContainer() {
        return mContainer;
    }

    @Override
    public void onLocationChanged(Location location) {
        String log = "Current Lat: " + location.getLatitude() + " Lon: " + location.getLongitude();
        mTextViewLog.setText(log);
        Log.d(TAG, log);
    }

    @Override
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling checkLocationPermissions() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void checkConnectedWiFi() {
        String wiFiName = Utils.getWiFiName(this);
        mPresenter.setConnectedToWiFi(wiFiName);
    }

    private void startWiFiBroadcast() {
        if (wiFiBroadcastReceiver != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(wiFiBroadcastReceiver, intentFilter);
        }
    }

    private void stopWiFiBroadcast() {
        if (wiFiBroadcastReceiver != null) {
            unregisterReceiver(wiFiBroadcastReceiver);
        }
    }

}
