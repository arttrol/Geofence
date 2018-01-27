package com.example.artem.geofence;

import com.example.artem.geofence.dagger.AppComponent;
import com.example.artem.geofence.dagger.DaggerTestComponent;

/**
 * Created by Artem on 27.01.18
 */

public class TestApplication extends GeofenceApplication {
    @Override
    protected AppComponent buildComponent() {
        return DaggerTestComponent.builder().build();
    }
}
