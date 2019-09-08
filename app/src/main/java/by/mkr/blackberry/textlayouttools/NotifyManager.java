package by.mkr.blackberry.textlayouttools;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class NotifyManager {
    final String LOG_TAG = "ReplacerLog";
    final String CHANNEL_ID = "by.mkr.blackberry.textlayouttools.layoutnotification";

    private Notification notificationEn;
    private Notification notificationRu;
    private android.accessibilityservice.AccessibilityService _service;
    private android.app.NotificationManager _notifyManager;

    public NotifyManager(android.accessibilityservice.AccessibilityService service) {
        _service = service;

        _notifyManager = (android.app.NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notifyChannel = null;

        // Notification service is based on NeoTheFox's app from 4pda.ru
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                notifyChannel = _notifyManager.getNotificationChannel(CHANNEL_ID);
            } catch (Exception e) {
                Log.d(LOG_TAG, "notifyChannel is null");
            }
            if (notifyChannel == null) {
                notifyChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "Current Keyboard Layout",
                        android.app.NotificationManager.IMPORTANCE_LOW);
                notifyChannel.setShowBadge(false);
                notifyChannel.enableLights(false);
                notifyChannel.enableVibration(false);
                notifyChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

                _notifyManager.createNotificationChannel(notifyChannel);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationEn = new Notification.Builder(_service, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_flag_gb)
                    .setContentTitle("English")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .build();

            notificationRu = new Notification.Builder(_service, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_flag_russia)
                    .setContentTitle("Русский")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .build();
        } else {
            notificationEn = new Notification.Builder(_service)
                    .setSmallIcon(R.drawable.ic_flag_gb)
                    .setContentTitle("English")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .build();

            notificationRu = new Notification.Builder(_service)
                    .setSmallIcon(R.drawable.ic_flag_russia)
                    .setContentTitle("Русский")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .build();
        }
    }

    public void updateNotification(Language lang) {
        switch (lang) {
            case Ru:
            case RuTrans: {
                _notifyManager.notify(123, notificationRu);
                break;
            }
            case En:
            case EnTrans: {
                _notifyManager.notify(123, notificationEn);
                break;
            }
            default: {
                break;
            }
        }
    }

    public void clearNotifications() {
        _notifyManager.cancelAll();
    }
}
