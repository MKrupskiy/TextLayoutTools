package by.mkr.blackberry.textlayouttools;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.util.Log;
import java.util.Date;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;


enum NetworkState {
    Any,
    WiFi,
    Mobile,
    None;

    public static NetworkState fromString(String x) {
        switch (x) {
            case "Any":
                return Any;
            case "WiFi":
                return WiFi;
            case "Mobile":
                return Mobile;
            case "None":
                return None;
            default:
                return null;
        }
    }

    public static String getDefault() {
        return WiFi.toString();
    }

    public boolean isOn() {
        return this != None;
    }
}

public class CheckVersionReceiver extends BroadcastReceiver {
    public static final String ACTION_CHECK_UPDATE = "by.mkr.blackberry.textlayouttools.layoutnotification.handlers.action.CHECK_UPDATE";
    private static final int RECEIVER_REQUEST_CODE = 9113;
    private static final int HOURS = 1000 * 60 * 60;
    private static final int CHECK_PERIOD = 17 * HOURS;

    public CheckVersionReceiver() {
        super();
    }

    public CheckVersionReceiver(Context context, boolean shouldStart) {
        super();
        if (shouldStart) {
            setAlarm(context);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, ACTION_CHECK_UPDATE);
        //Acquire the lock
        wl.acquire();

        String action = intent.getAction();
        Log.d(LOG_TAG, "CheckVersionReceiver: " + new Date());

        switch (action) {
            case ACTION_CHECK_UPDATE: {
                AppSettings appSettings = ReplacerService.getAppSettings();
                NetworkState ns = getCurrentConnectionType(context);
                boolean isWiFiAllowed = appSettings.checkForUpdates == NetworkState.Any || appSettings.checkForUpdates == NetworkState.WiFi;
                boolean isMobileAllowed = appSettings.checkForUpdates == NetworkState.Any;

                if ((ns == NetworkState.WiFi && isWiFiAllowed) || (ns == NetworkState.Mobile && isMobileAllowed)) {
                    checkNewVersion(context);
                }
                break;
            }
            default: {
                break;
            }
        }

        //Release the lock
        wl.release();
    }



    public void setAlarm(Context context) {
        cancelAlarm(context);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CheckVersionReceiver.class);
        intent.setAction(ACTION_CHECK_UPDATE);
        //intent.putExtra(ACTION_CHECK_UPDATE, Boolean.FALSE);
        PendingIntent pi = PendingIntent.getBroadcast(context, RECEIVER_REQUEST_CODE, intent, 0);
        //After a period
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), CHECK_PERIOD , pi);
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, CheckVersionReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, RECEIVER_REQUEST_CODE, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void setOnetimeTimer(Context context) {
        cancelAlarm(context);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CheckVersionReceiver.class);
        intent.setAction(ACTION_CHECK_UPDATE);
        //intent.putExtra(ACTION_CHECK_UPDATE, Boolean.TRUE);
        PendingIntent pi = PendingIntent.getBroadcast(context, RECEIVER_REQUEST_CODE, intent, 0);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
    }


    public NetworkState getCurrentConnectionType(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            if (activeInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return NetworkState.WiFi;
            }
            if (activeInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return NetworkState.Mobile;
            }
        }
        return NetworkState.None;
    }

    private static void checkNewVersion(final Context context) {
        HttpHelper.fetchLatestVersion(null, new HttpCallback() {
            @Override
            public void execute(final String[] text) {
                if (text == null || text.length < 2) {
                    Log.d(LOG_TAG, "checkNewVersion error: No info");
                    return;
                }
                Log.d(LOG_TAG, "CheckVersionReceiver: Version: " + BuildConfig.VERSION_NAME +  "; Available: " + text[0] + "; comp: " + (BuildConfig.VERSION_NAME.compareTo(text[0])));
                boolean isNewVersionAvailable = BuildConfig.VERSION_NAME.compareTo(text[0]) < 0;
                AppSettings.setSetting(R.string.setting_application_updates_available, isNewVersionAvailable, context);
                AppSettings.setSetting(R.string.setting_application_updates_link, text[1], context);
            }
        });
    }
}
