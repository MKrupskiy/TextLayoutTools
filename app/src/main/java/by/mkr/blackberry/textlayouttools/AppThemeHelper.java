package by.mkr.blackberry.textlayouttools;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.widget.Toolbar;

enum AppTheme {
    Light,
    Dark,
    Blue;

    public static AppTheme fromString(String x) {
        switch (x) {
            case "Light":
                return Light;
            case "Dark":
                return Dark;
            case "Blue":
                return Blue;
        }
        return null;
    }

    public static String getDefault() {
        return Light.toString();
    }

    public static int getThemeResId(Context context) {
        String themeStr = AppSettings.getSetting(R.string.setting_application_theme, AppTheme.getDefault(), context);
        AppTheme theme = AppTheme.fromString(themeStr);
        switch (theme) {
            case Light:
                return R.style.AlertDialogStyle;
            case Dark:
                return R.style.AlertDialogStyle_Dark;
            case Blue:
                return R.style.AlertDialogStyle_Blue;
            default:
                return R.style.AlertDialogStyle;
        }
    }
}

public class AppThemeHelper {
    private static int THEME_SETTING_ID = R.string.setting_application_theme;

    public static void setTheme(Activity activity) {
        String themeStr = AppSettings.getSetting(THEME_SETTING_ID, AppTheme.getDefault(), activity.getApplicationContext());
        AppTheme theme = AppTheme.fromString(themeStr);
        switch (theme) {
            case Light:
                break;
            case Dark:
                activity.setTheme(R.style.AppThemeDark);
                break;
            case Blue:
                activity.setTheme(R.style.AppThemeBlue);
                break;
            default:
                break;
        }
    }
    public static void setSettingsTheme(Activity activity) {
        String themeStr = AppSettings.getSetting(THEME_SETTING_ID, AppTheme.getDefault(), activity.getApplicationContext());
        AppTheme theme = AppTheme.fromString(themeStr);
        switch (theme) {
            case Light:
                break;
            case Dark:
                activity.setTheme(R.style.AppThemeDarkSettings);
                break;
            case Blue:
                activity.setTheme(R.style.AppThemeBlueSettings);
                break;
            default:
                break;
        }
    }
    public static void setMenuTheme(Activity activity, int toolbarId) {
        String themeStr = AppSettings.getSetting(THEME_SETTING_ID, AppTheme.getDefault(), activity.getApplicationContext());
        AppTheme theme = AppTheme.fromString(themeStr);
        Toolbar toolbar = activity.findViewById(toolbarId);
        switch (theme) {
            case Light:
                toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
                break;
            case Dark:
                toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
                break;
            case Blue:
                toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Blue);
                break;
            default:
                break;
        }
    }
}
