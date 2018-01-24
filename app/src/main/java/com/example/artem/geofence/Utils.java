package com.example.artem.geofence;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.artem.geofence.view.activity.MainActivity;

/**
 * Created by Artem on 23.01.18
 */

public class Utils {
    private static final String CHANNEL_ID = "com.example.artem.geofence.CHANNEL_ID";
    private static final String TAG = "GeofenceAreaModel";

    private Utils() {
    }

    public static void sendGeofenceStatusNotification(Context context, boolean isInside) {
        Utils.sendNotification(context,
                context.getString(isInside ? R.string.status_inside : R.string.status_outside),
                isInside ? Color.GREEN : Color.RED);
    }

    private static void sendNotification(Context context, String notificationTitle, int color) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager == null) {
            Log.e(TAG, "sendNotification. mNotificationManager is null");
            return;
        }

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setColor(color)
                .setContentTitle(notificationTitle)
                .setContentText(context.getString(R.string.notification_text_return_to_app))
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        mNotificationManager.notify(0, builder.build());
    }

    public static String getWiFiName(Context context) {
        String wiFiName = null;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            //If the SSID can be decoded as UTF-8, it will be returned surrounded by
            //double quotation marks. Remove it
            if (ssid.startsWith("\"")) {
                wiFiName = ssid.substring(1, ssid.length() - 1);
            }
        }
        return wiFiName;
    }
}
