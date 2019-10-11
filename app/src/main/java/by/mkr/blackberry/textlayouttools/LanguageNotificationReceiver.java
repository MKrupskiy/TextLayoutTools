package by.mkr.blackberry.textlayouttools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

import org.jetbrains.annotations.NotNull;


public class LanguageNotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_MUTE_1H = "by.mkr.blackberry.textlayouttools.layoutnotification.handlers.action.MUTE_1H";
    public static final String ACTION_MUTE_8H = "by.mkr.blackberry.textlayouttools.layoutnotification.handlers.action.MUTE_8H";
    public static final String ACTION_ENABLE = "by.mkr.blackberry.textlayouttools.layoutnotification.handlers.action.ENABLE";

    private static int mel = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case ACTION_MUTE_1H: {
                updateMuted(context, 1);
                showToUser(context, 1);
                mel(context, 1);
                break;
            }
            case ACTION_MUTE_8H: {
                updateMuted(context, 8);
                showToUser(context, 8);
                mel(context, 8);
                break;
            }
            case ACTION_ENABLE: {
                updateMuted(context, -1);
                showToUser(context, -1);
                mel(context, -1);
                break;
            }
            default: {
                break;
            }
        }
    }


    public static NotificationCompat.Action createNotificationAction(@NotNull Context context, @NotNull String actionString) {
        int requestCode = 1001;
        String text = context.getString(R.string.text_btn_mute_1h);
        int drawableId = R.drawable.ic_chevron_right_black_24dp;

        switch (actionString) {
            case LanguageNotificationReceiver.ACTION_MUTE_1H: {
                requestCode = 1001;
                text = context.getString(R.string.text_btn_mute_1h);
                drawableId = R.drawable.ic_chevron_right_black_24dp;
                break;
            }
            case LanguageNotificationReceiver.ACTION_MUTE_8H: {
                requestCode = 1002;
                text = context.getString(R.string.text_btn_mute_8h);
                drawableId = R.drawable.ic_chevron_right_black_24dp;
                break;
            }
            case LanguageNotificationReceiver.ACTION_ENABLE: {
                requestCode = 1003;
                text = context.getString(R.string.text_btn_mute_enable);
                drawableId = R.drawable.ic_chevron_right_black_24dp;
                break;
            }
            default: {
                break;
            }
        }
        Intent intent = new Intent(context, LanguageNotificationReceiver.class);
        intent.setAction(actionString);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(drawableId, text, pendingIntent).build();
        return action;
    }


    private static long addHours(int hoursCount) {
        // If in the past
        if (hoursCount < 0) {
            return 0;
        }
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR, hoursCount);
        return now.getTimeInMillis();
    }

    private static void updateMuted(Context context, int hoursCount) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putLong(context.getString(R.string.setting_when_enable_notifications), addHours(hoursCount));
        prefsEditor.commit();
    }

    private static void showToUser(Context context, int hours) {
        if (hours <= 0) {
            Toast.makeText(context, context.getString(R.string.text_toast_mute_enable), Toast.LENGTH_SHORT).show();
        } else {
            Calendar now = Calendar.getInstance();
            now.add(Calendar.HOUR, hours);
            String text = context.getString(R.string.text_toast_mute_general) + " " +
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(now.getTime());
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    private void mel(Context context, int hoursCount) {
        switch (hoursCount) {
            case 1: {
                mel = mel == 0
                        ? 1
                        : mel == 2
                            ? 3
                            : mel == 4
                                ? 5
                                : 0;
                break;
            }
            case 8: {
                mel = mel == 5
                        ? 6
                        : mel == 6
                            ? smt(context)
                            : 0;

                break;
            }
            case -1: {
                mel = mel == 1
                        ? 2
                        : mel == 3
                            ? 4
                            : 0;
                break;
            }
            default: {
                break;
            }
        }
    }
    private int smt(Context context) {
        Notification notif = new NotificationCompat.Builder(context, NotifyManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_grasshopper)
                .setContentTitle("К"+"узнечик н"+"айден!")
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setColor(Color.GREEN)
                .build();

        NotificationManager notifyManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyManager.notify(111, notif);

        return 0;
    }
}
