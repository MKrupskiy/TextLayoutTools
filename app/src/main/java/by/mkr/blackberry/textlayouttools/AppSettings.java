package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;


public class AppSettings {
    public int selectedCtrlMod;
    public int selectedShortCut;
    public Language autocorrectDirection;
    public boolean isEnabled;
    public boolean isKey2EmulationEnabled;
    public boolean isTranslit;
    public boolean isShowInfo;
    public boolean isCorrectDoubled;
    public boolean isAutoCorrect;
    public boolean isShowIcon;
    public boolean isLogToFile;
    public boolean isSelectReplaced;
    public String[] userDict;

    public int vibrationIntensity;
    public VibrationPattern vibrationPatternRus;
    public VibrationPattern vibrationPatternEng;
    public SoundPattern soundInputRus;
    public SoundPattern soundInputEng;
    public SoundPattern soundCorrectRus;
    public SoundPattern soundCorrectEng;

    public long whenEnableNotifications;

    private Context _context;


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
        isCorrectDoubled = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_correct_double_letters), false);
        isAutoCorrect = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_auto_correct), true);
        isShowIcon = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_show_icon), true);
        isLogToFile = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_log_to_sd), false);
        isSelectReplaced = sharedPrefs.getBoolean(_context.getString(R.string.setting_select_replaced), false);
        userDict = sharedPrefs
                .getString(_context.getString(R.string.setting_user_dictionary), "")
                .toLowerCase()
                .split("\n");


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
    }
}