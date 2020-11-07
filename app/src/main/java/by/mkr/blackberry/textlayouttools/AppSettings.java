package by.mkr.blackberry.textlayouttools;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;


public class AppSettings {
    public int version = 1;
    public int selectedCtrlMod;
    public int selectedShortCut;
    public Language autocorrectDirection;
    public InputMethod inputMethod;
    public IconStyle iconStyleRu;
    public IconStyle iconStyleEn;
    public FloatingIconStyle floatingIconStyleRu;
    public FloatingIconStyle floatingIconStyleEn;
    public boolean isEnabled;
    public boolean isKey2EmulationEnabled;
    public boolean isTranslit;
    public boolean isShowInfo;
    public boolean isReplaceAltEnter;
    public boolean isCorrectDoubled;
    public boolean isAutoCorrect;
    public boolean isShowIcon;
    public boolean isShowFloatingIcon;
    public boolean isLogToFile;
    public boolean isSelectReplaced;
    public boolean isFloatingIconUnlocked;
    public boolean isDetectLanguageByText;
    public boolean isFloatingIconShowLangPicker;
    public boolean isTrackStatistics;
    public String floatingIconTextRu;
    public String floatingIconTextEn;
    public String[] userDict;
    public List<String> appsBlackListAutocorrect;
    public List<String> appsBlackListAll;

    public int vibrationIntensity;
    //public int floatingIconTextSize; // Use Flag size instead
    public int floatingIconFlagSize;
    public int floatingIconTextColor;
    public int floatingIconBackgroundColor;
    public int floatingIconPositionX;
    public int floatingIconPositionY;
    public VibrationPattern vibrationPatternRus;
    public VibrationPattern vibrationPatternEng;
    public SoundPattern soundInputRus;
    public SoundPattern soundInputEng;
    public SoundPattern soundCorrectRus;
    public SoundPattern soundCorrectEng;
    public FloatingIconAnimation floatingIconAnimation;

    public transient long whenEnableNotifications;
    public float opacity;

    public transient UserTempDictionary userTempDict;

    private transient Context _context;
    public transient int manualChangesCount;
    public transient int autoChangesCount;


    public AppSettings(Context context) {
        _context = context;
        bindSettings(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public void bindSettings(SharedPreferences sharedPrefs) {
        String selectedCtrlModStr = sharedPrefs.getString(_context.getString(R.string.setting_control_key), "129");
        selectedCtrlMod = Integer.parseInt(selectedCtrlModStr);
        String selectedShortCutStr = sharedPrefs.getString(_context.getString(R.string.setting_shortcut_key), "" + KeyEvent.KEYCODE_Q);
        selectedShortCut = Integer.parseInt(selectedShortCutStr);
        String autocorrectDirectionStr = sharedPrefs.getString(_context.getString(R.string.setting_autocorrect_direction), "Unknown");
        autocorrectDirection = Language.fromString(autocorrectDirectionStr);
        isEnabled = sharedPrefs.getBoolean(_context.getString(R.string.setting_shortcut_enabled_key), true);
        isKey2EmulationEnabled = sharedPrefs.getBoolean(_context.getString(R.string.setting_key2emulation_enabled), false);
        isTranslit = !"0".equals(sharedPrefs.getString(_context.getString(R.string.setting_input_method), "0"));
        isShowInfo = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_show_info), false);
        isReplaceAltEnter = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_replace_alt_enter), true);
        isCorrectDoubled = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_correct_double_letters), false);
        isAutoCorrect = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_auto_correct), true);
        isShowIcon = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_show_icon), true);
        isLogToFile = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_log_to_sd), false);
        isSelectReplaced = sharedPrefs.getBoolean(_context.getString(R.string.setting_select_replaced), false);
        userDict = sharedPrefs
                .getString(_context.getString(R.string.setting_user_dictionary), "")
                .toLowerCase()
                .replaceAll("\n{2,}", "\n")
                .replaceAll("\n+$", "")
                .replaceAll("^\n+", "")
                .split("\n");

        String userTempDictionaryStr = sharedPrefs.getString(_context.getString(R.string.setting_user_temp_dictionary), "");
        userTempDict = new UserTempDictionary(userTempDictionaryStr);

        appsBlackListAutocorrect = Arrays.asList(sharedPrefs
                .getString(_context.getString(R.string.setting_apps_black_list_autocorrect), "")
                .split("\n"));
        appsBlackListAll = Arrays.asList(sharedPrefs
                .getString(_context.getString(R.string.setting_apps_black_list_all), "")
                .split("\n"));


        vibrationIntensity = sharedPrefs.getInt(_context.getString(R.string.setting_vibration_intensity), 5) * 10; // 100%

        String vibrationPatternRusStr = sharedPrefs.getString(_context.getString(R.string.setting_vibration_pattern_rus), "SingleShort");
        vibrationPatternRus = VibrationPattern.fromString(vibrationPatternRusStr);
        String vibrationPatternEngStr = sharedPrefs.getString(_context.getString(R.string.setting_vibration_pattern_eng), "DoubleShort");
        vibrationPatternEng = VibrationPattern.fromString(vibrationPatternEngStr);

        String soundInputRusStr = sharedPrefs.getString(_context.getString(R.string.setting_sound_input_rus), "Switch");
        soundInputRus = SoundPattern.fromString(soundInputRusStr);
        String soundInputEngStr = sharedPrefs.getString(_context.getString(R.string.setting_sound_input_eng), "Switch");
        soundInputEng = SoundPattern.fromString(soundInputEngStr);

        String soundCorrectRusStr = sharedPrefs.getString(_context.getString(R.string.setting_sound_correct_rus), "Ru");
        soundCorrectRus = SoundPattern.fromString(soundCorrectRusStr);
        String soundCorrectEngStr = sharedPrefs.getString(_context.getString(R.string.setting_sound_correct_eng), "En");
        soundCorrectEng = SoundPattern.fromString(soundCorrectEngStr);

        whenEnableNotifications = sharedPrefs.getLong(_context.getString(R.string.setting_when_enable_notifications), 0);

        String InputMethodStr = sharedPrefs.getString(_context.getString(R.string.setting_input_method), "Qwerty");
        inputMethod = InputMethod.fromString(InputMethodStr);

        String IconStyleRuStr = sharedPrefs.getString(_context.getString(R.string.setting_icon_style_ru), "Flag");
        iconStyleRu = IconStyle.fromString(IconStyleRuStr);

        String IconStyleEnStr = sharedPrefs.getString(_context.getString(R.string.setting_icon_style_en), "Flag");
        iconStyleEn = IconStyle.fromString(IconStyleEnStr);

        isShowFloatingIcon = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_show_floating_icon), false);

        String FloatingIconStyleRuStr = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_style_ru), "Flag");
        floatingIconStyleRu = FloatingIconStyle.fromString(FloatingIconStyleRuStr);

        String FloatingIconStyleEnStr = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_style_en), "Flag");
        floatingIconStyleEn = FloatingIconStyle.fromString(FloatingIconStyleEnStr);

        floatingIconTextRu = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_text_ru), "RUS");
        floatingIconTextEn = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_text_en), "ENG");

        //floatingIconTextSize = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_text_size), 4) + 10;
        floatingIconFlagSize = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_flag_size), 40) + 40;

        floatingIconTextColor = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_text_color), 0xFFFFFFFF);
        floatingIconBackgroundColor = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_background_color), 0x22000000);

        int opacityInt = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_opacity), 20);
        opacity = 1 - (float) opacityInt / 100;

        floatingIconPositionX = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_position_x), 0);
        floatingIconPositionY = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_position_y), 0);

        isFloatingIconUnlocked = sharedPrefs.getBoolean(_context.getString(R.string.setting_floating_icon_is_unlocked), true);

        isDetectLanguageByText = sharedPrefs.getBoolean(_context.getString(R.string.setting_check_text_for_language), false);

        isFloatingIconShowLangPicker = sharedPrefs.getBoolean(_context.getString(R.string.setting_floating_icon_is_show_lang_picker), true);

        isTrackStatistics = sharedPrefs.getBoolean(_context.getString(R.string.setting_statistics_should_track), true);

        String floatingIconAnimationStr = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_animation), "FadeIn");
        floatingIconAnimation = FloatingIconAnimation.fromString(floatingIconAnimationStr);



        manualChangesCount = sharedPrefs.getInt(_context.getString(R.string.setting_statistics_manual_changes), 0);
        autoChangesCount = sharedPrefs.getInt(_context.getString(R.string.setting_statistics_auto_changes), 0);
    }

    public String getString(int resourceId) {
        return _context.getString(resourceId);
    }

    public void updateUserDict(String[] newUserDict) {
        String newUserDictStr = TextUtils
                .join("\n", newUserDict)
                .replaceAll("\n{2,}", "\n");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putString(getString(R.string.setting_user_dictionary), newUserDictStr);
        edit.apply();
        updateLinedStringSetting(R.string.setting_apps_black_list_autocorrect, Arrays.asList(newUserDict));
    }

    public void updateUserTempDict(UserTempDictionary newUserDict) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putString(getString(R.string.setting_user_temp_dictionary), newUserDict.toString());
        edit.apply();
    }

    public void updateAppsBlackListAutocorrect(List<String> newBlackList) {
        updateLinedStringSetting(R.string.setting_apps_black_list_autocorrect, newBlackList);
    }

    public void updateAppsBlackListAll(List<String> newBlackList) {
        updateLinedStringSetting(R.string.setting_apps_black_list_all, newBlackList);
    }

    public void toggleStatistics(boolean enabled) {
        isTrackStatistics = enabled;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putBoolean(getString(R.string.setting_statistics_should_track), enabled);
        edit.apply();
    }

    public void increseStatisticsManualChange() {
        manualChangesCount++;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putInt(getString(R.string.setting_statistics_manual_changes), manualChangesCount);
        edit.apply();
    }

    public void increseStatisticsAutoChange() {
        autoChangesCount++;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putInt(getString(R.string.setting_statistics_auto_changes), autoChangesCount);
        edit.apply();
    }

    public void clearStatistics() {
        manualChangesCount = 0;
        autoChangesCount = 0;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putInt(getString(R.string.setting_statistics_manual_changes), 0);
        edit.putInt(getString(R.string.setting_statistics_auto_changes), 0);
        edit.apply();
    }

    public boolean saveToFile() {
        try {
            Gson gson = new Gson();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
            String json = gson.toJson(/*sharedPrefs*/this);
            Log.d(LOG_TAG, "saveToFile: " + json);

            File appFolder = App.createAppFolder();

            File file = new File(appFolder, "settings.json");
            if (!file.exists())
            {
                file.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            osw.write(json);
            osw.flush();
            osw.close();

            return true;
        } catch (Exception ex) {
            Log.d(LOG_TAG, "Ex saveToFile: " + ex.getMessage());
            return false;
        }
    }

    public boolean loadFromFile(Uri fileUri) {
        try {
            ContentResolver cr = _context.getContentResolver();
            InputStream inputStream = cr.openInputStream(fileUri);
            if (inputStream == null) {
                throw new IOException("Unable to obtain input stream from URI");
            }

            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
            AppSettings data = gson.fromJson(reader, AppSettings.class);
            Log.d(LOG_TAG, "Loaded settings version: " + data.version);
            if (data.version == 0) {
                throw new Exception("Wrong settings file");
            }

            reader.close();

            return this.replaceSettings(data);
        } catch (Exception ex) {
            Log.d(LOG_TAG, "Ex loadFromFile: " + ex.getMessage());
            return false;
        }
    }

    private boolean replaceSettings(AppSettings newSettings) {
        try {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
            SharedPreferences.Editor edit = sharedPrefs.edit();


            if (newSettings.selectedCtrlMod != 0) {
                edit.putString(getString(R.string.setting_control_key), "" + newSettings.selectedCtrlMod);
            }


            if (newSettings.selectedShortCut != 0) {
                edit.putString(getString(R.string.setting_shortcut_key), "" + newSettings.selectedShortCut);
            }
            if (newSettings.autocorrectDirection != null) {
                edit.putString(getString(R.string.setting_autocorrect_direction), "" + newSettings.autocorrectDirection);
            }
            edit.putBoolean(getString(R.string.setting_shortcut_enabled_key), newSettings.isEnabled);
            edit.putBoolean(getString(R.string.setting_key2emulation_enabled), newSettings.isKey2EmulationEnabled);
            edit.putBoolean(getString(R.string.setting_is_show_info), newSettings.isShowInfo);
            edit.putBoolean(getString(R.string.setting_is_replace_alt_enter), newSettings.isReplaceAltEnter);
            edit.putBoolean(getString(R.string.setting_is_correct_double_letters), newSettings.isCorrectDoubled);
            edit.putBoolean(getString(R.string.setting_is_auto_correct), newSettings.isAutoCorrect);
            edit.putBoolean(getString(R.string.setting_is_show_icon), newSettings.isShowIcon);
            edit.putBoolean(getString(R.string.setting_is_log_to_sd), newSettings.isLogToFile);
            edit.putBoolean(getString(R.string.setting_select_replaced), newSettings.isSelectReplaced);
            if (newSettings.userDict != null) {
                edit.putString(getString(R.string.setting_user_dictionary), TextUtils.join("\n", newSettings.userDict));
            }
            // Skip as it's temporary
            //String userTempDictionaryStr = sharedPrefs.getString(_context.getString(R.string.setting_user_temp_dictionary), "");
            //userTempDict = new UserTempDictionary(userTempDictionaryStr);
            if (newSettings.appsBlackListAutocorrect != null) {
                edit.putString(getString(R.string.setting_apps_black_list_autocorrect), TextUtils.join("\n", newSettings.appsBlackListAutocorrect));
            }
            if (newSettings.appsBlackListAll != null) {
                edit.putString(getString(R.string.setting_apps_black_list_all), TextUtils.join("\n", newSettings.appsBlackListAll));
            }
            edit.putInt(getString(R.string.setting_vibration_intensity), newSettings.vibrationIntensity / 10);
            if (newSettings.vibrationPatternRus != null) {
                edit.putString(getString(R.string.setting_vibration_pattern_rus), "" + newSettings.vibrationPatternRus);
            }
            if (newSettings.vibrationPatternEng != null) {
                edit.putString(getString(R.string.setting_vibration_pattern_eng), "" + newSettings.vibrationPatternEng);
            }
            if (newSettings.soundInputRus != null) {
                edit.putString(getString(R.string.setting_sound_input_rus), "" + newSettings.soundInputRus);
            }
            if (newSettings.soundInputEng != null) {
                edit.putString(getString(R.string.setting_sound_input_eng), "" + newSettings.soundInputEng);
            }
            if (newSettings.soundCorrectRus != null) {
                edit.putString(getString(R.string.setting_sound_correct_rus), "" + newSettings.soundCorrectRus);
            }
            if (newSettings.soundCorrectEng != null) {
                edit.putString(getString(R.string.setting_sound_correct_eng), "" + newSettings.soundCorrectEng);
            }
            // Skip as it's temporary
            //whenEnableNotifications = sharedPrefs.getLong(_context.getString(R.string.setting_when_enable_notifications), 0);
            if (newSettings.inputMethod != null) {
                edit.putString(getString(R.string.setting_input_method), "" + newSettings.inputMethod);
            }
            if (newSettings.iconStyleRu != null) {
                edit.putString(getString(R.string.setting_icon_style_ru), "" + newSettings.iconStyleRu);
            }
            if (newSettings.iconStyleEn != null) {
                edit.putString(getString(R.string.setting_icon_style_en), "" + newSettings.iconStyleEn);
            }
            edit.putBoolean(getString(R.string.setting_is_show_floating_icon), newSettings.isShowFloatingIcon);
            if (newSettings.floatingIconStyleRu != null) {
                edit.putString(getString(R.string.setting_floating_icon_style_ru), "" + newSettings.floatingIconStyleRu);
            }
            if (newSettings.floatingIconStyleEn != null) {
                edit.putString(getString(R.string.setting_floating_icon_style_en), "" + newSettings.floatingIconStyleEn);
            }
            if (newSettings.floatingIconTextRu != null) {
                edit.putString(getString(R.string.setting_floating_icon_text_ru), "" + newSettings.floatingIconTextRu);
            }
            if (newSettings.floatingIconTextEn != null) {
                edit.putString(getString(R.string.setting_floating_icon_text_en), "" + newSettings.floatingIconTextEn);
            }
            if (newSettings.floatingIconFlagSize != 0) {
                edit.putInt(getString(R.string.setting_floating_icon_flag_size), newSettings.floatingIconFlagSize - 40);
            }
            if (newSettings.floatingIconTextColor != 0) {
                edit.putInt(getString(R.string.setting_floating_icon_text_color), newSettings.floatingIconTextColor);
            }
            if (newSettings.floatingIconBackgroundColor != 0) {
                edit.putInt(getString(R.string.setting_floating_icon_background_color), newSettings.floatingIconBackgroundColor);
            }
            edit.putInt(getString(R.string.setting_floating_icon_opacity), Math.round((1 - newSettings.opacity) * 100));
            edit.putInt(getString(R.string.setting_floating_icon_position_x), newSettings.floatingIconPositionX);
            edit.putInt(getString(R.string.setting_floating_icon_position_y), newSettings.floatingIconPositionY);
            edit.putBoolean(getString(R.string.setting_floating_icon_is_unlocked), newSettings.isFloatingIconUnlocked);
            edit.putBoolean(getString(R.string.setting_check_text_for_language), newSettings.isDetectLanguageByText);
            edit.putBoolean(getString(R.string.setting_floating_icon_is_show_lang_picker), newSettings.isFloatingIconShowLangPicker);
            edit.putBoolean(getString(R.string.setting_statistics_should_track), newSettings.isTrackStatistics);
            if (newSettings.floatingIconAnimation != null) {
                edit.putString(getString(R.string.setting_floating_icon_animation), "" + newSettings.floatingIconAnimation);
            }


            edit.apply();
            Log.d(LOG_TAG, "replaceSettings: " + newSettings.version);

            return true;
        } catch (Exception ex) {
            Log.d(LOG_TAG, "Ex replaceSettings: " + ex.getMessage());
            return false;
        }
    }

    private void updateLinedStringSetting(int settingId, List<String> newPropArray) {
        String newPropArrayStr = TextUtils
                .join("\n", newPropArray)
                .replaceAll("\n{2,}", "\n");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putString(getString(settingId), newPropArrayStr);
        edit.apply();
    }
}