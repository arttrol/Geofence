package com.example.artem.geofence.dagger;

import com.example.artem.geofence.presenter.Presenter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

/**
 * Created by Artem on 27.01.18
 */
@Module
public class PresenterTestModule {

    @Provides
    @Singleton
    Presenter providePresenter(){
        return mock(Presenter.class);
    }
}
