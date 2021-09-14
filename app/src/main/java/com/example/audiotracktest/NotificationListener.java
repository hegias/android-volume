package com.example.audiotracktest;

import android.content.ComponentName;
import android.service.notification.NotificationListenerService;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    public NotificationListener() {
        super();
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onNotificationPosted (StatusBarNotification sbn) {
        if(sbn != null && sbn.getNotification() != null) {
            Log.i("notifcation", sbn.getNotification().toString());
            String title = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TITLE).toString();
            String text = sbn.getNotification().extras.getCharSequence(Notification.EXTRA_TEXT).toString();
            Log.i("notifcation", "title: " + title + " text: " + text);

            // Qui il vostro codice
        }
    }
    @Override
    public void onNotificationRemoved (StatusBarNotification sbn) {
        Log.i("notifcation", "onNotificationRemoved");
    }
    public Boolean VerifyNotificationPermission() {
        String theList = android.provider.Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        String[] theListList = theList.split(":");
        String me = (new ComponentName(this, NotificationListener.class)).flattenToString();
        for ( String next : theListList ) {
            if ( me.equals(next) ) return true;
        }
        return false;
    }
}
