package com.example.artem.geofence.dagger;

import android.content.Context;

import com.example.artem.geofence.model.GeofenceAreaModel;
import com.example.artem.geofence.model.GeofenceAreaModelImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

/**
 * Created by Artem on 27.01.18
 */
@Module
class ModelTestModule {

    @Provides
    @Singleton
    GeofenceAreaModel provideModel() {
        return mock(GeofenceAreaModelImpl.class);
    }
}
