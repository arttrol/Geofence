package com.example.artem.geofence.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

/**
 * Created by Artem on 27.01.18
 */
@Module
public class AppTestModule {

    @Provides
    @Singleton
    Context provideContext() {
        return mock(Context.class);
    }
}
