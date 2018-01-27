package com.example.artem.geofence.dagger;

import com.example.artem.geofence.presenter.Presenter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Artem on 27.01.18
 */
@Module
class PresenterModule {

    @Provides
    @Singleton
    Presenter providePresenter(){
        return new Presenter();
    }
}
