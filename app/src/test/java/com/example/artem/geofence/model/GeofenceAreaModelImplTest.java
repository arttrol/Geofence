package com.example.artem.geofence.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.artem.geofence.TestConst;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created by Artem on 27.01.18
 * Sample tests
 */
@SuppressWarnings("WeakerAccess")
@RunWith(RobolectricTestRunner.class)
public class GeofenceAreaModelImplTest {

    GeofenceAreaModelImpl geofenceAreaModel;

    @Before
    public void setUp() throws Exception {
        Context applicationContext = RuntimeEnvironment.application.getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        sharedPreferences.edit().putString(GeofenceAreaModel.PREF_GEOFENCE_WIFI_NAME, TestConst.WIFI_TEST_VALUE_1).commit();

        Context context = Mockito.mock(Context.class);
        Mockito.when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);
        geofenceAreaModel = new GeofenceAreaModelImpl(context);
    }

    @Test
    public void testIsInside() throws Exception {
        geofenceAreaModel.setConnectedWiFiName(TestConst.WIFI_TEST_VALUE_1);
        geofenceAreaModel.setInGeoArea(true);
        assertTrue(geofenceAreaModel.isInside());

        geofenceAreaModel.setConnectedWiFiName(TestConst.WIFI_TEST_VALUE_2);
        geofenceAreaModel.setInGeoArea(true);
        assertTrue(geofenceAreaModel.isInside());

        geofenceAreaModel.setConnectedWiFiName(TestConst.WIFI_TEST_VALUE_1);
        geofenceAreaModel.setInGeoArea(false);
        assertTrue(geofenceAreaModel.isInside());

        geofenceAreaModel.setConnectedWiFiName(TestConst.WIFI_TEST_VALUE_2);
        geofenceAreaModel.setInGeoArea(false);
        assertFalse(geofenceAreaModel.isInside());
    }

}