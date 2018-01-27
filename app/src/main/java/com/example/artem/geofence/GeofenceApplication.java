package com.example.artem.geofence;

import android.app.Application;

import com.example.artem.geofence.dagger.AppComponent;
import com.example.artem.geofence.dagger.AppModule;
import com.example.artem.geofence.dagger.DaggerAppComponent;

/**
 * Created by Artem on 27.01.18
 * <p>
 * The goal of this assignment is to create an Android application that will detect if the device is located inside of a geofence area.
 * Geofence area is defined as a combination of some geographic point, radius, and specific Wifi network name. A device is considered to be inside of the geofence area if the device is connected to the specified WiFi network or remains geographically inside the defined circle.
 * Note that if device coordinates are reported outside of the zone, but the device still connected to the specific Wifi network, then the device is treated as being inside the geofence area.
 * Application activity should provide controls to configure the geofence area and display current status: inside OR outside.
 * Once you have completed this task, host your source code on GitHub and a README file in the root with instructions. Keep in mind we prefer some commits history in the repo vs single commit. Tests are welcome and encouraged.
 */

public class GeofenceApplication extends Application {

    private static AppComponent sAppComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }
}
