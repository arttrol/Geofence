package com.example.artem.geofence.dagger;

import com.example.artem.geofence.view.activity.MainActivityTest;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Artem on 27.01.18
 */
@Singleton
@Component(modules = {AppTestModule.class,
        PresenterTestModule.class,
        ModelTestModule.class,
        LocationTestModule.class})
public interface TestComponent extends AppComponent {

    void inject(MainActivityTest mainActivityTest);

}
