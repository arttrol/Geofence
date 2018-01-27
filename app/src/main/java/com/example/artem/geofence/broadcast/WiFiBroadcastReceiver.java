package com.example.artem.geofence.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.example.artem.geofence.Utils;
import com.example.artem.geofence.model.GeofenceAreaModelImpl;

/**
 * Created by Artem on 23.01.18.
 * <p>
 * Check connected wifi network
 * This broadcast should be registered for android.net.wifi.STATE_CHANGE events.
 */

public class WiFiBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            String wiFiName = null;
            if (info != null && info.isConnected()) {
                wiFiName = Utils.getWiFiName(context);
            }

            new GeofenceAreaModelImpl(context).setConnectedWiFiName(wiFiName);
        }

    }
}
