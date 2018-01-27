package com.example.artem.geofence.dagger;

import com.example.artem.geofence.presenter.Presenter;
import com.example.artem.geofence.view.activity.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Artem on 27.01.18
 */
@Singleton
@Component(modules = {AppModule.class, ModelModule.class, PresenterModule.class, LocationModule.class})
public interface AppComponent {

    void inject(MainActivity mainActivity);

    void inject(Presenter presenter);

}
