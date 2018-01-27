package com.example.artem.geofence.view.activity;

import com.example.artem.geofence.BuildConfig;
import com.example.artem.geofence.TestApplication;
import com.example.artem.geofence.dagger.TestComponent;
import com.example.artem.geofence.presenter.Presenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import static org.mockito.Mockito.verify;

/**
 * Created by Artem on 27.01.18.
 * Sample tests
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        application = TestApplication.class)
public class MainActivityTest {

    @Inject
    Presenter presenter;

    private MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) TestApplication.getAppComponent()).inject(this);

        mainActivity = Robolectric.buildActivity(MainActivity.class).create().get();
    }

    @Test
    public void testOnResume() throws Exception {
        mainActivity.onResume();
        verify(presenter).onResume();
    }

}