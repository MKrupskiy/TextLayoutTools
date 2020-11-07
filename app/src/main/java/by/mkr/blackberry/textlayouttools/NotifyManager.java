package by.mkr.blackberry.textlayouttools;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;


enum IconStyle {
    Flag,
    FlagAlt1,
    TextCamel,
    TextCapital;

    public static IconStyle fromString(String x) {
        switch (x) {
            case "Flag":
                return Flag;
            case "FlagAlt1":
                return FlagAlt1;
            case "TextCamel":
                return TextCamel;
            case "TextCapital":
                return TextCapital;
            default:
                return null;
        }
    }

    public static int getResource(IconStyle style, Language lang) {
        if (lang == Language.Ukr) {
            return R.drawable.ic_flag_ukraine;
        }
        switch (style) {
            case Flag:
                return lang.isRus() ? R.drawable.ic_flag_russia : R.drawable.ic_flag_gb;
            case FlagAlt1:
                return lang.isRus() ? R.drawable.ic_flag_russia : R.drawable.ic_flag_us;
            case TextCamel:
                return lang.isRus() ? R.drawable.ic_text_ru : R.drawable.ic_text_en;
            case TextCapital:
                return lang.isRus() ? R.drawable.ic_text_ru_capital : R.drawable.ic_text_en_capital;
            default:
                return 0;
        }
    }
}


public class NotifyManager {
    final static String CHANNEL_ID = "by.mkr.blackberry.textlayouttools.layoutnotification";

    private Notification _notificationEn;
    private Notification _notificationRu;
    private Notification _notificationUkr;
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
            if (notifyChannel == null || notifyChannel != null) {
                notifyChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "Current Keyboard Layout",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notifyChannel.setShowBadge(false);
                notifyChannel.enableLights(false);
                notifyChannel.enableVibration(false);
                notifyChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

                _notifyManager.createNotificationChannel(notifyChannel);
            }
        }

        createNotifications(_service);
    }

    public void updateNotification(Language lang) {
        switch (lang) {
            case Ru:
            case RuTrans:
            case RuFull:
            case RuQwertz: {
                _notifyManager.notify(123, _notificationRu);
                break;
            }
            case En:
            case EnTrans:
            case EnFull:
            case EnQwertz: {
                _notifyManager.notify(123, _notificationEn);
                break;
            }
            case Ukr: {
                _notifyManager.notify(123, _notificationUkr);
                break;
            }
            default: {
                break;
            }
        }
    }

    public void updateNotificationButtons() {
        if(_service != null) {
            createNotifications(_service);
        } else {
            Log.d(LOG_TAG, "NotifyManager->updateNotificationButtons: service is null");
        }
    }

    private void createNotifications(android.accessibilityservice.AccessibilityService service) {
        // Icon styles
        int[] iconStyles = getIconStyles();



        // Actions
        /*
        NotificationCompat.Action action1h = LanguageNotificationReceiver
                .createNotificationAction(_service, LanguageNotificationReceiver.ACTION_MUTE_1H);

        NotificationCompat.Action action24h = LanguageNotificationReceiver
                .createNotificationAction(_service, LanguageNotificationReceiver.ACTION_MUTE_8H);

        NotificationCompat.Action actionSoundEnable = LanguageNotificationReceiver
                .createNotificationAction(_service, LanguageNotificationReceiver.ACTION_SOUND_ENABLE);
        */

        NotificationCompat.Action actionSoundSwitch = LanguageNotificationReceiver
                .createNotificationAction(_service, LanguageNotificationReceiver.ACTION_MUTE_SWITCH);

        NotificationCompat.Action actionManualSwitch = LanguageNotificationReceiver
                .createNotificationAction(_service, LanguageNotificationReceiver.ACTION_MANUAL_SWITCH);

        NotificationCompat.Action actionAutocorrectSwitch = LanguageNotificationReceiver
                .createNotificationAction(_service, LanguageNotificationReceiver.ACTION_AUTOCORRECT_SWITCH);


        PendingIntent settingsIntent = PendingIntent.getActivity(service, 0, new Intent(service, SettingsActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _notificationRu = new NotificationCompat.Builder(_service, CHANNEL_ID)
                    .setSmallIcon(iconStyles[0])
                    .setContentTitle("Русский")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    //.addAction(action1h)
                    //.addAction(action24h)
                    //.addAction(actionSoundEnable)
                    .addAction(actionSoundSwitch)
                    .addAction(actionManualSwitch)
                    .addAction(actionAutocorrectSwitch)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setColor(Color.parseColor(_service.getString(R.color.colorPrimary)))
                    .setContentIntent(settingsIntent)
                    .build();

            _notificationEn = new NotificationCompat.Builder(_service, CHANNEL_ID)
                    .setSmallIcon(iconStyles[1])
                    .setContentTitle("English")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    //.addAction(action1h)
                    //.addAction(action24h)
                    //.addAction(actionSoundEnable)
                    .addAction(actionSoundSwitch)
                    .addAction(actionManualSwitch)
                    .addAction(actionAutocorrectSwitch)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setColor(Color.parseColor(_service.getString(R.color.colorPrimary)))
                    .setContentIntent(settingsIntent)
                    .build();

            _notificationUkr = new NotificationCompat.Builder(_service, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_flag_ukraine)
                    .setContentTitle("Українська")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    //.addAction(action1h)
                    //.addAction(action24h)
                    //.addAction(actionSoundEnable)
                    .addAction(actionSoundSwitch)
                    .addAction(actionManualSwitch)
                    .addAction(actionAutocorrectSwitch)
                    .setShowWhen(false)
                    .setOnlyAlertOnce(true)
                    .setColor(Color.parseColor(_service.getString(R.color.colorPrimary)))
                    .setContentIntent(settingsIntent)
                    .build();
        } else {
            _notificationRu = new Notification.Builder(_service)
                    .setSmallIcon(iconStyles[0])
                    .setContentTitle("Русский")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContentIntent(settingsIntent)
                    .build();

            _notificationEn = new Notification.Builder(_service)
                    .setSmallIcon(iconStyles[1])
                    .setContentTitle("English")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContentIntent(settingsIntent)
                    .build();
        }
    }

    public void clearNotifications() {
        _notifyManager.cancelAll();
    }

    public int[] getIconStyles() {
        AppSettings appSettings = ReplacerService.getAppSettings();

        if (appSettings != null) {
            return new int[]{
                    IconStyle.getResource(appSettings.iconStyleRu, Language.Ru),
                    IconStyle.getResource(appSettings.iconStyleEn, Language.En)
            };
        } else {
            return new int[]{
                    R.drawable.ic_flag_russia,
                    R.drawable.ic_flag_gb
            };
        }
    }
}
