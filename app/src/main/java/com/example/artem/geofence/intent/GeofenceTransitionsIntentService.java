package com.example.artem.geofence.intent;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.example.artem.geofence.R;
import com.example.artem.geofence.activity.MainActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import static android.content.ContentValues.TAG;

/**
 * Created by Artem on 22.01.18.
 */

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String CHANNEL_ID = "com.example.artem.geofence.CHANNEL_ID";

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getName());
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.e(TAG, "onHandleIntent: " + intent);
        if (geofencingEvent.hasError()) {
            // TODO: High 23.01.18 handle errors
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            boolean isInside = geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER;
            sendNotification(isInside ? "inside" : "outside", isInside ? Color.GREEN : Color.RED);
        } else {
            Log.e(TAG, "Transition invalid type" + geofenceTransition);
        }

    }

    private void sendNotification(String notificationTitle, int color) {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }

        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setColor(color)
                .setContentTitle(notificationTitle)
                .setContentText(getString(R.string.notification_text_return_to_app))
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        mNotificationManager.notify(0, builder.build());
    }
}
