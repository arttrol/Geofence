package com.example.artem.geofence.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.artem.geofence.GeofenceErrorMessages;
import com.example.artem.geofence.model.GeofenceAreaModelImpl;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by Artem on 22.01.18.
 * This class handle transitions for current geofences
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitions";

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getName());
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = getString(GeofenceErrorMessages.getErrorStringRes(geofencingEvent.getErrorCode()));
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        Log.d(TAG, "onHandleIntent: " + geofenceTransition);

        new GeofenceAreaModelImpl(getApplicationContext()).setInGeoArea(
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER);
    }

}
