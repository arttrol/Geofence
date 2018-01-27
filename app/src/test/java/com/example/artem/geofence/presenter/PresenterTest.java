package com.example.artem.geofence.presenter;

import com.example.artem.geofence.R;
import com.example.artem.geofence.TestConst;
import com.example.artem.geofence.view.MainActivityView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Artem on 27.01.18
 * Sample tests
 */
@SuppressWarnings("WeakerAccess")
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class PresenterTest {

    @Mock
    MainActivityView mockView;

    Presenter presenter;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        presenter = new Presenter();
        presenter.setView(mockView);
    }

    @Test
    public void testSetConnectedToWiFi() throws Exception {
        presenter.setConnectedToWiFi(TestConst.WIFI_TEST_VALUE_1);
        assertEquals(presenter.mGeofenceAreaModel.getConnectedWiFiName(), TestConst.WIFI_TEST_VALUE_1);
    }


    @Test
    public void testOnApplyClick() throws Exception {
        presenter.onApplyClick();

        verify(mockView).hideKeyboard();
        verify(mockView).checkLocationPermissions();
        verify(mockView).showSnackbar(R.string.snackbar_fill_all_fields);
        verify(mockView).readGeofenceArea();
        verifyNoMoreInteractions(mockView);
    }

}