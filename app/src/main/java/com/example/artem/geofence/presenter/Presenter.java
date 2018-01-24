package com.example.artem.geofence.presenter;

import com.google.android.gms.location.LocationRequest;
import com.karumi.dexter.listener.single.PermissionListener;

/**
 * Created by Artem on 23.01.18
 */

public interface Presenter {
    String USER_GEOFENCE_ID = "USER_GEOFENCE_ID";

    void onApplyClick();

    void onStart();

    void onResume();

    void onPause();

    void onStop();

    LocationRequest getLocationRequest();

    PermissionListener getPermissionListener();

}
