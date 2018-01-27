package com.example.artem.geofence.dagger;

import android.content.Context;

import com.example.artem.geofence.model.GeofenceAreaModel;
import com.example.artem.geofence.model.GeofenceAreaModelImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Artem on 27.01.18
 */
@Module
class ModelModule {

    @Provides
    @Singleton
    GeofenceAreaModel provideModel(Context context) {
        return new GeofenceAreaModelImpl(context);
    }
}
