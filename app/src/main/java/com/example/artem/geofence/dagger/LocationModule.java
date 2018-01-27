package com.example.artem.geofence.dagger;

import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Artem on 27.01.18
 */
@Module
class LocationModule {

    @Provides
    @Singleton
    GeofencingClient provideGeofencingClient(Context context) {
        return LocationServices.getGeofencingClient(context);
    }

    @Provides
    @Singleton
    FusedLocationProviderClient provideFusedLocationProviderClient(Context context) {
        return LocationServices.getFusedLocationProviderClient(context);
    }
}
