package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;


public class AppSettings {
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
    public boolean isCorrectDoubled;
    public boolean isAutoCorrect;
    public boolean isShowIcon;
    public boolean isShowFloatingIcon;
    public boolean isLogToFile;
    public boolean isSelectReplaced;
    public boolean isFloatingIconUnlocked;
    public boolean isDetectLanguageByText;
    public boolean isFloatingIconShowLangPicker;
    public String floatingIconTextRu;
    public String floatingIconTextEn;
    public String[] userDict;

    public int vibrationIntensity;
    public int floatingIconTextSize;
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

    public long whenEnableNotifications;
    public float opacity;

    public UserTempDictionary userTempDict;

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
                .replaceAll("\n{2,}", "\n")
                .replaceAll("\n+$", "")
                .replaceAll("^\n+", "")
                .split("\n");

        String userTempDictionaryStr = sharedPrefs.getString(_context.getString(R.string.setting_user_temp_dictionary), "");
        userTempDict = new UserTempDictionary(userTempDictionaryStr);


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

        isShowFloatingIcon = sharedPrefs.getBoolean(_context.getString(R.string.setting_is_show_floating_icon), true);

        String FloatingIconStyleRuStr = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_style_ru), "Flag");
        floatingIconStyleRu = FloatingIconStyle.fromString(FloatingIconStyleRuStr);

        String FloatingIconStyleEnStr = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_style_en), "Flag");
        floatingIconStyleEn = FloatingIconStyle.fromString(FloatingIconStyleEnStr);

        floatingIconTextRu = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_text_ru), "RUS");
        floatingIconTextEn = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_text_en), "ENG");

        floatingIconTextSize = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_text_size), 4) + 10;
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

        String floatingIconAnimationStr = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_animation), "FadeIn");
        floatingIconAnimation = FloatingIconAnimation.fromString(floatingIconAnimationStr);
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
    }

    public void updateUserTempDict(UserTempDictionary newUserDict) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        SharedPreferences.Editor edit = sharedPrefs.edit();
        edit.putString(getString(R.string.setting_user_temp_dictionary), newUserDict.toString());
        edit.apply();
    }
}