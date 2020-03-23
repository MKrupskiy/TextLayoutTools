package by.mkr.blackberry.textlayouttools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
    public FloatingIconGravityHoriz floatingIconGravityHoriz;
    public FloatingIconGravityVert floatingIconGravityVert;
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
    public String floatingIconTextRu;
    public String floatingIconTextEn;
    public String[] userDict;

    public int vibrationIntensity;
    public int floatingIconTextSize;
    public int floatingIconFlagSize;
    public int floatingIconTextColor;
    public int floatingIconBackgroundColor;
    public VibrationPattern vibrationPatternRus;
    public VibrationPattern vibrationPatternEng;
    public SoundPattern soundInputRus;
    public SoundPattern soundInputEng;
    public SoundPattern soundCorrectRus;
    public SoundPattern soundCorrectEng;

    public long whenEnableNotifications;
    public float opacity;

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

        floatingIconTextRu = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_text_ru), "Rus");
        floatingIconTextEn = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_text_en), "Eng");

        floatingIconTextSize = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_text_size), 2) + 10;
        floatingIconFlagSize = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_flag_size), 30) + 30;

        floatingIconTextColor = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_text_color), 0xFFFFFFFF);
        floatingIconBackgroundColor = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_background_color), 0x22000000);
        floatingIconGravityHoriz = FloatingIconGravityHoriz.fromInt(sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_gravity_horiz), 2));

        String floatingIconGravityVertStr = sharedPrefs.getString(_context.getString(R.string.setting_floating_icon_gravity_vert), "Bottom");
        floatingIconGravityVert = FloatingIconGravityVert.fromString(floatingIconGravityVertStr);

        int opacityInt = sharedPrefs.getInt(_context.getString(R.string.setting_floating_icon_opacity), 20);
        opacity = 1 - (float)opacityInt / 100;
    }
}