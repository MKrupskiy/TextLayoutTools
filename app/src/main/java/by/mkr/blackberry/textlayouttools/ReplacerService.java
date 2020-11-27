package by.mkr.blackberry.textlayouttools;


import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class ReplacerService extends android.accessibilityservice.AccessibilityService
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    final static String LOG_TAG = "ReplacerLog";
    private static List<String> _notHandlePackages = Arrays.asList(
            "by.tlt.dummyapp",
            "com.android.systemui",
            //"com.blackberry.blackberrylauncher",
            "com.blackberry.keyboard",
            "com.google.android.inputmethod.latin", // Gboard
            "ru.androidteam.rukeyboard" // rukeyboard
    );

    private static int[] _notHandleInputs = {
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_DATETIME,
            InputType.TYPE_CLASS_PHONE
    };

    private static AppSettings _appSettings;

    static TextSelection _replacerSelect;
    static TextSelection _controlSelect;

    static Language _currentLanguage;
    static Language _currentDesiredLanguage;
    static char _lastTypedLetter;
    static long _lastTypedTime;
    static int _textLength;

    private NotifyManager _notifyManager;
    private LanguageDetector _languageDetector;
    private static boolean _isProgramChange;
    private static int _lastAutoReplaced;

    private WordsList _wordsListRu;
    private WordsList _wordsListEn;

    private Toast _toast;

    private VibrationManager _vibrationManager;
    private SoundManager _soundManager;
    private FloatingIndicatorManager _floatingIndicatorManager;

    private long _lastSymPressed;

    //private boolean _isSkipApplication;
    private BlacklistItemBlockState _currentState;



    @Override
    protected void onServiceConnected() {
        log("--- onServiceConnected");

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        _appSettings = new AppSettings(getApplicationContext());

        _currentLanguage = Language.Ru;

        _notifyManager = new NotifyManager(this);
        _languageDetector = new LanguageDetector();

        _replacerSelect  = new TextSelection();
        _controlSelect  = new TextSelection();

        _wordsListRu = new WordsList(this, Language.Ru);
        _wordsListEn = new WordsList(this, Language.En);

        _vibrationManager = new VibrationManager(getApplicationContext());
        _soundManager = new SoundManager(getApplicationContext());

        _floatingIndicatorManager = new FloatingIndicatorManager(getApplicationContext());
    }


    // Tracks Hotkey presses
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        // action = 0 - press, 1 - release
        // mod 129 - BB right ctrl key, 65 - left ctrl
        // key code = 45 - Q
        try {
            // Do not act if blacklisted All
            if (_currentState == BlacklistItemBlockState.None) {
                Log.d(LOG_TAG, "blacklisted All");
                return super.onKeyEvent(event);
            }


            boolean isHandled = false;

            int pressAction = event.getAction();
            int keyCode = event.getKeyCode();
            int keyMod = event.getModifiers();
            char display = event.getDisplayLabel();


            // Handle SYM key:
            // Single press blocked
            // Double press is a default action
            if (pressAction == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_SYM
                    && keyMod == _appSettings.selectedCtrlMod
            ) {
                log("SYM pressed; dif: " + (new Date().getTime() - _lastSymPressed));
                if (new Date().getTime() - _lastSymPressed > 500) {
                    _lastSymPressed = new Date().getTime();
                    return true;
                }
                _lastSymPressed = new Date().getTime();
            }

            // Change Text Layout
            if (_appSettings.isEnabled && pressAction == KeyEvent.ACTION_DOWN) {
                boolean isAltEnter = keyMod == 18 && keyCode == KeyEvent.KEYCODE_ENTER;
                boolean isCtrlSpace = (keyMod == 12288 && keyCode == KeyEvent.KEYCODE_SPACE) // Ctrl // 20480 - right ctrl
                                    || (keyMod == 327680 && keyCode == KeyEvent.KEYCODE_SPACE) // Win key
                                    || (event.isShiftPressed() && keyCode == KeyEvent.KEYCODE_SPACE); // Shift+Space


                log("isAltEnter: " + isAltEnter + "; isCtrlSpace: " + isCtrlSpace);
                log("keyMod: " + keyMod + "; keyCode: " + keyCode);


                if (_appSettings.isShowInfo) {
                    String logInfo = "Key: " + keyCode +
                            "\nMod: " + keyMod +
                            "\nAlt: " + event.isAltPressed() +
                            "\nShift: " + event.isShiftPressed() +
                            "\nCtrl: " + event.isCtrlPressed() +
                            "\nFunc: " + event.isFunctionPressed() +
                            "\nLong: " + event.isLongPress();
                    //Toast.makeText(this.getApplicationContext(), logInfo, Toast.LENGTH_SHORT).show();
                    logToToast(logInfo);
                }

                //Log.d(LOG_TAG, "mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; display=" + display);
                //logToFile("mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; display=" + display);
                // Ctrl+G: mod=18; code=66; action=0 - alt+enter


                if ( (keyMod == _appSettings.selectedCtrlMod && keyCode == _appSettings.selectedShortCut)
                        || (isAltEnter && _appSettings.isReplaceAltEnter)
                        || (isCtrlSpace && _appSettings.isReplaceAltEnter)
                ) {
                    _isProgramChange = true;
                    log("_isProgramChange3:" + _isProgramChange);

                    if (_replacerSelect.get_nodeInfo() != null && !_replacerSelect.isTextNullOrEmpty()) {
                        // Change SELECTED text
                        if (!_replacerSelect.isStartEqualsEnd()) {
                            ActionType actionType = isAltEnter
                                    ? ActionType.AltEnterReplace
                                    : isCtrlSpace
                                        ? ActionType.CtrlSpace
                                        : ActionType.ManualChange;
                            replaceSelectedText(_replacerSelect, _appSettings.inputMethod, _appSettings.isSelectReplaced, actionType);
                        } else {
                            // Change last Entered word


                            // Do not change via Alt+Enter
                            if (!isAltEnter && !isCtrlSpace) {
                                // Lang change
                                int cursorAt = _replacerSelect.get_endSelection();
                                int wordEnd = LanguageDetector.getNearestWordEnd(_replacerSelect.get_inputText(), _replacerSelect.get_endSelection(), _currentLanguage);
                                log("getNearestWordEnd: " + wordEnd + "; sel=" + _replacerSelect.get_endSelection());
                                WordWithBoundaries currentWord = LanguageDetector.getWordAtPosition(_replacerSelect.get_inputText(), wordEnd, _currentLanguage);
                                log("Current Word: " + currentWord.Word + "; end:" + wordEnd);

                                _lastAutoReplaced = currentWord.Begin;
                                log("_lastAutoReplaced: " + _lastAutoReplaced);

                                _replacerSelect.set_startSelection(currentWord.Begin);
                                _replacerSelect.set_endSelection(currentWord.End);
                                replaceSelectedText(_replacerSelect, _appSettings.inputMethod, _appSettings.isSelectReplaced, cursorAt, ActionType.ManualChange);

                            }



                        }

                        isHandled = !isAltEnter && !isCtrlSpace;

                    } else {
                        log("No text selected");
                    }
                }

                if (isCtrlSpace) {
                    // Need to update Tray Notification
                    InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
                    InputMethodSubtype ims = imeManager.getCurrentInputMethodSubtype();
                    if (ims != null) {
                        // Able to detect by input method
                        String locale = ims.getLocale();
                        String locale2 = ims.getLanguageTag();
                        log("locale: " + locale + "; locale2: " + locale2);
                        if ( (locale != null && locale != "") || (locale2 != null && locale2 != "") ) {
                            boolean isRussian = locale.toLowerCase().contains("ru") || locale2.toLowerCase().contains("ru");
                            _currentLanguage = Language.getByInputMethod(isRussian ? Language.Ru : Language.En, _appSettings.inputMethod);
                        } else {
                            log("locale not detected: " + _currentLanguage);
                        }
                    } else {
                        // Can't detect input method (e.g. Russian KB)
                        log("InputMethodSubtype not detected: " + _currentLanguage);
                        //_currentLanguage = Language.getByInputMethod(_currentLanguage, _appSettings.inputMethod);
                    }

                    if (_appSettings.isShowIcon) {
                        log("isCtrlSpace updateNotification: " + _currentLanguage);
                        _notifyManager.updateNotification(_currentLanguage);
                    } else {
                        log("isCtrlSpace clearNotifications");
                        _notifyManager.clearNotifications();
                    }
                    if (_appSettings.isShowFloatingIcon) {
                        _floatingIndicatorManager.setLanguage(_currentLanguage);
                        log("isCtrlSpace updateNotification fl: " + _currentLanguage);
                    } else {
                        log("isCtrlSpace clearNotifications fl");
                        _floatingIndicatorManager.clearLanguage();
                    }
                    notify(_currentLanguage, ActionType.CtrlSpace);
                }
            }

            // Emulate CTRL+Key combinations
            if (_appSettings.isKey2EmulationEnabled && pressAction == KeyEvent.ACTION_DOWN) {




                //AccessibilityNodeInfo eventNodeInfo = super.getRootInActiveWindow().findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT);
                //traverseTree(eventNodeInfo, 1);







                if (keyMod == _appSettings.selectedCtrlMod && keyCode == KeyEvent.KEYCODE_A) {
                    log("Ctrl+A");

                    if (_controlSelect.get_nodeInfo() != null && !_controlSelect.isTextNullOrEmpty()) {
                        log("Input: " + _controlSelect.get_inputText());

                        _controlSelect.set_startSelection(0);
                        _controlSelect.set_endSelection(_controlSelect.get_inputText().length());

                        _replacerSelect.copyFrom(_controlSelect);
                        log("Replacer: " + _replacerSelect.get_inputText() +
                                "; s=" + _replacerSelect.get_startSelection() +
                                "; e=" + _replacerSelect.get_endSelection()
                        );

                        Bundle selectArguments = new Bundle();
                        selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0);
                        selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, _controlSelect.get_endSelection());
                        _controlSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectArguments);
                    }
                    isHandled = true;
                } else if (keyMod == _appSettings.selectedCtrlMod && keyCode == KeyEvent.KEYCODE_C) {
                    log("Ctrl+C");
                    //eventNodeInfo.performAction(AccessibilityNodeInfo.ACTION_COPY);
                    _controlSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_COPY);
                    isHandled = true;
                } else if (keyMod == _appSettings.selectedCtrlMod && keyCode == KeyEvent.KEYCODE_V) {
                    log("Ctrl+V");
                    //eventNodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    _controlSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    isHandled = true;
                    _isProgramChange = true;
                } else if (keyMod == _appSettings.selectedCtrlMod && keyCode == KeyEvent.KEYCODE_X) {
                    log("Ctrl+X");
                    //eventNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CUT);
                    _controlSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_CUT);
                    isHandled = true;
                    _isProgramChange = true;
                } else if (keyMod == _appSettings.selectedCtrlMod && keyCode == KeyEvent.KEYCODE_Z) {
                    //log("Ctrl+Z");
                    //isHandled = true;
                    /*
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Instrumentation inst = new Instrumentation();
                                inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ALT_LEFT));
                                inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                                Thread.sleep(300);
                                inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                                inst.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ALT_LEFT));
                                log(LOG_TAG, "! Key Sent");
                            }
                            catch(Exception ex){
                                log(LOG_TAG, "! Ex2: " + ex.getMessage());
                            }
                        }
                    }).start();
                    isHandled = true;
                    */
                }
            }

            if (isHandled) {
                // Prevent default keyboard action
                return true;
            }

        } catch (Exception ex) {
            log("! Ex2: " + ex);
        }

        _isProgramChange = false;
        log("_isProgramChange4: " + _isProgramChange);

        // Nothing to do - default keyboard action
        return super.onKeyEvent(event);
    }

    // Tracks selection changes
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        log("Accessibility; package=" + event.getPackageName() + "; type=" + event.getEventType());

        try {
            switch (event.getEventType()) {

                // Fix double letters in Russian
                case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {
                    if (_notHandlePackages.contains(event.getPackageName())) {
                        break;
                    }

                    if (_currentState == BlacklistItemBlockState.Autocorrect) {
                        // App from black list
                        log("Blacklisted autocorrect");
                        break;
                    }

                    String tempStr = event.getSource().getText().toString();
                    _controlSelect.set_all(
                            event.getSource(),
                            tempStr,
                            event.getSource().getTextSelectionStart(),
                            event.getSource().getTextSelectionEnd()
                    );
                    _replacerSelect.copyFrom(_controlSelect);
                    log("TYPE_VIEW_TEXT_CHANGED; text=" + _controlSelect.get_inputText() +
                            "; start=" + _controlSelect.get_startSelection() +
                            "; end=" + _controlSelect.get_endSelection()
                    );


                    if (_appSettings.isCorrectDoubled) {
                        log("Lang=" + _currentLanguage + "; len=" + event.getText().toString().length());

                        String input = event.getText().toString();
                        boolean isSystemReplace = false;

                        // Do not change upon deletion
                        if (_textLength - input.length() == 1) { // Chrome thinks that Placeholder is a text
                            _textLength = input.length();
                            _lastTypedTime = 0;
                            _lastTypedLetter = ' ';
                            return;
                        } else {
                            isSystemReplace = _textLength == input.length(); // system replaces the letter, no need to update
                            _textLength = input.length();
                        }


                        if (_currentLanguage == null || _currentLanguage == Language.Unknown) {
                            _currentLanguage = LayoutConverter.getTextLanguage(input, _appSettings.inputMethod);
                        }

                        if (!isSystemReplace
                                && (_currentLanguage == Language.Ru || _currentLanguage == Language.RuTrans)
                        ) {
                            char currentLetter = input.charAt(_textLength - 2);
                            log("TEXT_CHANGED; text=" + input +
                                    "; prev=" + _replacerSelect.get_inputText() +
                                    "; len=" + _textLength +
                                    "; char=" + currentLetter +
                                    "; last=" + _lastTypedLetter +
                                    "; time=" + _lastTypedTime +
                                    "; diff=" + (new Date().getTime() - _lastTypedTime));

                            if (LayoutConverter.isDoubled(currentLetter, _currentLanguage)) {
                                if (Character.toLowerCase(currentLetter) == Character.toLowerCase(_lastTypedLetter)
                                        && (new Date().getTime() - _lastTypedTime) < 700
                                        && !(Character.isUpperCase(currentLetter) && Character.isLowerCase(_lastTypedLetter))
                                ) {

                                    // Replace double letter with single equivalent
                                    input = input.substring(1, input.length() - 3) + LayoutConverter.getSpareLetter(_lastTypedLetter, _currentLanguage);
                                    Bundle replaceArguments = new Bundle();
                                    replaceArguments.putCharSequence(
                                            AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                            input
                                    );
                                    event.getSource().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, replaceArguments);

                                    _lastTypedTime = 0;
                                    _lastTypedLetter = ' ';
                                } else {
                                    _lastTypedTime = new Date().getTime();
                                    _lastTypedLetter = currentLetter;
                                }
                            } else {
                                _lastTypedTime = 0;
                                _lastTypedLetter = ' ';
                            }
                        }

                    }




                    autoreplace(_replacerSelect);





                    // !!! Detect language based on text. Unstable!!!
                    if (_appSettings.isDetectLanguageByText) {
                        log("--- curr lang: " + _currentLanguage + "; text: " + event.getSource().getText());
                        int selStart = event.getSource().getTextSelectionStart();
                        String charEntered = "" + event.getSource().getText().charAt(selStart - 1);
                        Language newLang = LayoutConverter.getTextLanguage(charEntered, _appSettings.inputMethod);
                        if (_currentLanguage != newLang) {
                            _currentLanguage = newLang;
                            if (_appSettings.isShowIcon) {
                                log("isDetectLanguageByText updateNotification: " + _currentLanguage);
                                _notifyManager.updateNotification(_currentLanguage);
                            } else {
                                log("isDetectLanguageByText clearNotifications");
                                _notifyManager.clearNotifications();
                            }
                            if (_appSettings.isShowFloatingIcon) {
                                log("isDetectLanguageByText updateNotification fl: " + _currentLanguage);
                                _floatingIndicatorManager.setLanguage(_currentLanguage);
                            } else {
                                log("isDetectLanguageByText clearNotifications fl");
                                _floatingIndicatorManager.clearLanguage();
                            }
                            notify(_currentLanguage, ActionType.AltEnter);
                        }
                    }





                    break;
                }

                // Handle selection
                case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED: {
                    if (_notHandlePackages.contains(event.getPackageName())) {
                        break;
                    }

                    AccessibilityNodeInfo tempNodeInfo = event.getSource();
                    //AccessibilityNodeInfo tempNodeInfo = event.getSource().findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT);

                    int selectBegin = tempNodeInfo.getTextSelectionStart();
                    int selectEnd = tempNodeInfo.getTextSelectionEnd();
                    String tempStr = event.getText().toString();

                    // ??? inside HTML changes indices are not equal
                    //if (tempNodeInfo.getTextSelectionStart() != tempNodeInfo.getParent().findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT).getTextSelectionStart()) {
                    //    selectBegin = 0;
                    //}

                    log("TYPE_VIEW_TEXT_SELECTION_CHANGED; " + tempStr+
                            "; s=" + selectBegin +
                            "; e=" + selectEnd +
                            "; t=" + tempNodeInfo.getInputType() +
                            "; d=" + tempNodeInfo.getParent().findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT).getTextSelectionStart()
                    );

                    _controlSelect.set_all(
                            tempNodeInfo,
                            tempStr.substring(1, tempStr.length() - 1),// remove extra '[]'
                            selectBegin,
                            selectEnd
                    );

                    _replacerSelect.copyFrom(_controlSelect);

                    if (selectBegin == selectEnd) {
                        // Regular text input
                        log("Clear replacer; " + selectBegin);


                        //autoreplace(_replacerSelect);



                        //_replacerSelect.clearAll();
                        return;
                    }

                    log("text=" + tempStr + "; begin=" + selectBegin + "; end=" + selectEnd);

                    break;
                }

                // Track language changes
                case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: {
                    CharSequence packageName = event.getPackageName();
                    log("Toast event; package=" + packageName);
                    if (event.getText() != null && !event.getText().isEmpty()) {
                        log((String)event.getText().get(0));
                    }

                    if (!"com.blackberry.keyboard".equals(packageName)
                        && !"android".equals(packageName)) {
                        return;
                    }
                    if (event.getText() == null || event.getText().isEmpty()) {
                        return; // If no text info
                    }

                    String text = (String) event.getText().get(0);
                    //log(text);

                    boolean isRussian = text.contains("Русск") || text.contains("Russ");
                    boolean isEnglish = text.contains("Англ") || text.contains("Eng") || text.contains("Буквы (АБВ)") || text.contains("Латиница");
                    boolean isUkr = text.contains("Украин") || text.contains("Ukrain");

                    if (!isRussian && !isEnglish && !isUkr) {
                        return; // If no language indication
                    }

                    _currentLanguage = Language.getByInputMethod(isRussian ? Language.Ru : Language.En, _appSettings.inputMethod);

                    // Quick Ukraine fix
                    if (isUkr) {
                        _currentLanguage = Language.Ukr;
                    }

                    if (_appSettings.isShowIcon) {
                        log("NOTIFICATION updateNotification: " + _currentLanguage);
                        _notifyManager.updateNotification(_currentLanguage);
                    } else {
                        log("NOTIFICATION clearNotifications");
                        _notifyManager.clearNotifications();
                    }
                    if (_appSettings.isShowFloatingIcon) {
                        log("NOTIFICATION updateNotification fl: " + _currentLanguage);
                        _floatingIndicatorManager.setLanguage(_currentLanguage);
                    } else {
                        log("NOTIFICATION clearNotifications fl");
                        _floatingIndicatorManager.clearLanguage();
                    }

                    notify(_currentLanguage, ActionType.AltEnter);
                    break;
                }

                // Track input field change
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                    CharSequence packageName = event.getPackageName();
                    if (_notHandlePackages.contains(packageName)) {
                        break;
                    }
                    //Log.d(LOG_TAG, "TYPE_WINDOW_STATE_CHANGED: " + event.getPackageName());
                    BlacklistItemBlockState newState;
                    if (_appSettings.appsBlackListAutocorrect.contains(packageName)) {
                        log("block autocorrect: " + packageName);
                        newState = BlacklistItemBlockState.Autocorrect;
                    } else {
                        if (_appSettings.appsBlackListAll.contains(packageName)) {
                            log("block all: " + packageName);
                            newState = BlacklistItemBlockState.None;
                        } else {
                            log("start handling: " + packageName);
                            newState = BlacklistItemBlockState.All;
                        }
                    }
                    if (newState != _currentState) {
                        _currentState = newState;
                        _floatingIndicatorManager.setStatus(newState);
                    }


                    if (_appSettings.isKey2EmulationEnabled && !_notHandlePackages.contains(event.getPackageName())) {
                        log("TYPE_WINDOW_STATE_CHANGED: ");
                        if (event.getSource() != null) {
                            AccessibilityNodeInfo tmpNodeInfo = event.getSource().findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT);
                            // Ensure that it has text input
                            if (tmpNodeInfo != null) {
                                _controlSelect.set_all(
                                        tmpNodeInfo,
                                        tmpNodeInfo.getText() != null ? tmpNodeInfo.getText().toString() : "",
                                        tmpNodeInfo.getTextSelectionStart() != -1 ? tmpNodeInfo.getTextSelectionStart() : 0,
                                        tmpNodeInfo.getTextSelectionEnd() != -1 ? tmpNodeInfo.getTextSelectionEnd() : 0
                                );
                                log("TYPE_WINDOW_STATE_CHANGED 2: " + _controlSelect.get_inputText()
                                        + "; s=" + _controlSelect.get_startSelection() +
                                        "; e=" + _controlSelect.get_endSelection());
                            }
                        }
                    }

                    break;
                }

                case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:{
                    if (_appSettings.isKey2EmulationEnabled && !_notHandlePackages.contains(event.getPackageName())) {
                        log("TYPE_VIEW_CONTEXT_CLICKED: " + event.getText());
                    }

                    break;
                }

                case AccessibilityEvent.TYPE_VIEW_CLICKED:{
                    if (_appSettings.isKey2EmulationEnabled && !_notHandlePackages.contains(event.getPackageName())) {
                        log("TYPE_VIEW_CLICKED 0: ");
                        //AccessibilityNodeInfo tempNodeInfo = event.getSource();
                        AccessibilityNodeInfo tempNodeInfo = event.getSource().findFocus(AccessibilityNodeInfo.FOCUS_INPUT);

                        if (tempNodeInfo != null) {
                            log("TYPE_VIEW_CLICKED 1: " + event.getText() +
                                    "; c=" + tempNodeInfo.getClassName() +
                                    "; t=" + tempNodeInfo.getText() +
                                    "; s=" + tempNodeInfo.getTextSelectionStart() +
                                    "; e=" + tempNodeInfo.getTextSelectionEnd()
                            );

                            // ??? html
                            if (tempNodeInfo.getText() == null) {
                                tempNodeInfo = event.getSource();
                                log("TYPE_VIEW_CLICKED 2: " + event.getText() +
                                        "; c=" + tempNodeInfo.getClassName() +
                                        "; t=" + tempNodeInfo.getText() +
                                        "; s=" + tempNodeInfo.getTextSelectionStart() +
                                        "; e=" + tempNodeInfo.getTextSelectionEnd()
                                );
                            }

                            String tempStr = tempNodeInfo.getText() != null ? tempNodeInfo.getText().toString() : null;
                            _controlSelect.set_all(
                                    tempNodeInfo,
                                    tempStr,
                                    tempNodeInfo.getTextSelectionStart(),
                                    tempNodeInfo.getTextSelectionEnd()
                            );

                            _replacerSelect.copyFrom(_controlSelect);
                        }
                    }

                    break;
                }

                case AccessibilityEvent.TYPE_VIEW_FOCUSED: {
                    if (_appSettings.isKey2EmulationEnabled && !_notHandlePackages.contains(event.getPackageName())) {
                        log("TYPE_VIEW_FOCUSED 0: ");
                        //AccessibilityNodeInfo tempNodeInfo = event.getSource();
                        AccessibilityNodeInfo tempNodeInfo = event.getSource().findFocus(AccessibilityNodeInfo.FOCUS_INPUT);//not in hub

                        if (tempNodeInfo != null) {
                            log("TYPE_VIEW_FOCUSED 1: " + event.getText() +
                                    "; c=" + tempNodeInfo.getClassName() +
                                    "; t=" + tempNodeInfo.getText() +
                                    "; s=" + tempNodeInfo.getTextSelectionStart() +
                                    "; e=" + tempNodeInfo.getTextSelectionEnd()+
                                    "; c=" + event.getSource().getParent().getChildCount()
                            );

                            // ??? html
                            if (tempNodeInfo.getText() == null) {
                                tempNodeInfo = event.getSource();
                                log("TYPE_VIEW_FOCUSED 2: " + event.getText() +
                                        "; c=" + tempNodeInfo.getClassName() +
                                        "; t=" + tempNodeInfo.getText() +
                                        "; s=" + tempNodeInfo.getTextSelectionStart() +
                                        "; e=" + tempNodeInfo.getTextSelectionEnd()+
                                        "; c=" + event.getSource().getParent().getChildCount()
                                );
                            }

                            //traverseTree(tempNodeInfo, 1);

                            String tempStr = tempNodeInfo.getText() != null ? tempNodeInfo.getText().toString() : null;
                            _controlSelect.set_all(
                                    tempNodeInfo,
                                    tempStr,
                                    tempNodeInfo.getTextSelectionStart(),
                                    tempNodeInfo.getTextSelectionEnd()
                            );

                            _replacerSelect.copyFrom(_controlSelect);
                        }
                    }

                    break;
                }

                case AccessibilityEvent.TYPE_VIEW_SELECTED: {
                    log("TYPE_VIEW_SELECTED");
                    break;
                }


                default: {
                    log("Unknown: " + event.getEventType());
                    break;
                }
            }
        } catch (Exception ex) {
            log("! Ex: " + ex);
        }
        log("-----");
    }


    @Override
    public void onInterrupt() {
        log("Interrupt");
        _notifyManager.clearNotifications();
        _floatingIndicatorManager.clearLanguage();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void sendTextToInput(TextSelection textSelect, String replacedText, boolean isSelectReplaced, int newCursor) {
        String selectedText = textSelect.getSelectedText();
        String beforeText = textSelect.getBeforeText();
        String afterText = textSelect.getAfterText();
        int startSelection = textSelect.get_startSelection();

        // Replace selected text
        Bundle replaceArguments = new Bundle();
        replaceArguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                beforeText + replacedText + afterText
        );

        // Select replaced text
        Bundle selectArguments = new Bundle();
        if (isSelectReplaced) {
            selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, startSelection);
            selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, startSelection + replacedText.length());
        } else {
            int endSelect = newCursor == -1
                    ? startSelection + replacedText.length()
                    : newCursor + replacedText.length() - selectedText.length(); // Count doubled letters
            selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, endSelect);
            selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, endSelect);
        }
        _replacerSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, replaceArguments);
        _replacerSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectArguments);

    }


    private void replaceSelectedText(TextSelection textSelect, InputMethod inputMethod, boolean isSelectReplaced, int newCursor, ActionType actionType) {
        // Get selected text
        String selectedText = textSelect.getSelectedText();
        String beforeText = textSelect.getBeforeText();
        String afterText = textSelect.getAfterText();
        int startSelection = textSelect.get_startSelection();
        log("1=" + beforeText + "; t=" + selectedText + "; a=" + afterText);

        // Get converted text
        _currentLanguage = LayoutConverter.getTextLanguage(selectedText, inputMethod);
        String replacedText = LayoutConverter.getReplacedText(selectedText, _currentLanguage);

        // Correct doubled capitals
        replacedText = LayoutConverter.getTextWithoutDoubledCapital(replacedText);

        sendTextToInput(textSelect, replacedText, isSelectReplaced, newCursor);

        _isProgramChange = true;
        log("_isProgramChange2:" + _isProgramChange);

        //textSelect.clearAll();

        // Notify user
        notify(_currentLanguage.getOpposite(), actionType);


        // Add replaced text to the dictionary for future corrections
        if (actionType != ActionType.AutoChange) {
            addToTempDictionary(replacedText, selectedText);
        }

        // Update Statistics if needed
        if (_appSettings.isTrackStatistics) {
            updateStatistics(actionType);
        }
    }
    private void replaceSelectedText(TextSelection textSelect, InputMethod inputMethod, boolean isSelectReplaced, ActionType actionType) {
        replaceSelectedText(textSelect, inputMethod, isSelectReplaced, -1, actionType);
    }


    private void autoreplace(TextSelection textSelect) {
        boolean isReplaceable = isReplaceableInput(textSelect.get_nodeInfo().getInputType());
        boolean isBlockedByUser = _appSettings.autocorrectDirection.isEng() && _currentLanguage.isRus()
                || _appSettings.autocorrectDirection.isRus() && _currentLanguage.isEng();
        log("TYPE:" + textSelect.get_nodeInfo().getInputType() + " " + isReplaceableInput(textSelect.get_nodeInfo().getInputType()));

        log("CHECK _isProgramChange:" + _isProgramChange);
        if (!_isProgramChange && _appSettings.isAutoCorrect && isReplaceable && !isBlockedByUser) {
            Character lastChar = textSelect.getLastEnteredChar();
            if (lastChar == null) {
                log("getLastEnteredChar is null");
                return;
            }
            boolean isWordLetter = LanguageDetector.isWordLetter(lastChar, _currentLanguage);

            log("^^^^^^^^^^^^^");
            log("CHAR:" + lastChar + "; is word:" + isWordLetter);

            if (!isWordLetter) {
                // Automatic lang change
                log("userDict:" + _appSettings.userDict.length);

                int cursorAt = textSelect.get_endSelection();
                int wordEnd = LanguageDetector.getNearestWordEnd(
                        textSelect.get_inputText(),
                        textSelect.get_endSelection() -1,// -1 because of corrections in the middle of the string ("hello NEW |world")
                        _currentLanguage);
                log("getNearestWordEnd: " + wordEnd + "; sel=" + textSelect.get_endSelection());
                WordWithBoundaries currentWord = LanguageDetector.getWordAtPosition(textSelect.get_inputText(), wordEnd, _currentLanguage);
                log("Current Word: " + currentWord.Word + "; end:" + wordEnd);

                log("_lastAutoReplaced CHECK: " + _lastAutoReplaced);
                if (currentWord.Begin == _lastAutoReplaced) {
                    log("Already handled begin:" + currentWord.Begin);
                    return;
                }
                _lastAutoReplaced = currentWord.Begin;
                log("_lastAutoReplaced: " + _lastAutoReplaced);

                Language lang = _languageDetector.getTargetLanguage(currentWord.Word, _appSettings.inputMethod, _wordsListRu, _wordsListEn, _appSettings.userDict);

                Language textEnteredLang = LayoutConverter.getTextLanguage(currentWord.Word, _appSettings.inputMethod);

                if (lang == textEnteredLang || lang == Language.Unknown) {
                    // Input language == keyboard language
                    // Check for doubled capital
                    if (LayoutConverter.isNeedDoubleCapitalCorrection(currentWord.Word)) {
                        textSelect.set_startSelection(currentWord.Begin);
                        textSelect.set_endSelection(currentWord.End);
                        sendTextToInput(textSelect, LayoutConverter.getTextWithoutDoubledCapital(currentWord.Word), false, cursorAt);
                    } else {
                        // Do nothing
                        log("***From: " + textEnteredLang +
                                "; To: '" + lang +
                                "; " + currentWord.Word +
                                "' no change" +
                                "; s=" + currentWord.Begin +
                                "; e=" + currentWord.End);
                    }
                } else {
                    // Need to change layout
                    String replacedWord = LayoutConverter.getReplacedText(currentWord.Word, textEnteredLang);
                    log("***From: " + textEnteredLang +
                            "; To: " + lang +
                            "; '" + currentWord.Word +
                            "' -> '" + replacedWord + "'");

                    textSelect.set_startSelection(currentWord.Begin);
                    textSelect.set_endSelection(currentWord.End);
                    replaceSelectedText(textSelect, _appSettings.inputMethod, false, cursorAt, ActionType.AutoChange);



                    try {
                        log("!GC:");
                        System.gc();
                    } catch (Exception ex) {
                        log("!EX GC:");
                    }

                }
            } else {
                _lastAutoReplaced = -1;
                log("_lastAutoReplaced: " + _lastAutoReplaced);
            }
            log("vvvvvvvvvvvv");
        }
        _isProgramChange = true;
        log("_isProgramChange1:" + _isProgramChange);
    }

    private void addToTempDictionary(final String replacedText, final String untranslatedText) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] replacedWords = AddToDictionaryActivity.splitWords(replacedText);
                    String[] untranslatedWords = AddToDictionaryActivity.splitWords(untranslatedText);
                    List<String> promotedWords = new ArrayList<String>();
                    List<String> promotedUntranslatedWords = new ArrayList<String>();

                    for (int i = 0; i < replacedWords.length; i++) {
                        boolean shouldPromoteToUserDict = _appSettings.userTempDict.addToTempDict(replacedWords[i]);
                        if (shouldPromoteToUserDict) {
                            log("shouldPromoteToUserDict: " + replacedWords[i]);
                            promotedWords.add(replacedWords[i]);
                            promotedUntranslatedWords.add(untranslatedWords[i]);
                        }
                    }
                    // Iterate promoted words
                    if (promotedWords.size() > 0) {
                        final String promotedStr = TextUtils.join(", ", promotedWords);
                        final String promotedUntranslatedStr = TextUtils.join(", ", promotedUntranslatedWords);
                        log("promotedStr: " + promotedStr);
                        logToToast(String.format(getString(R.string.text_toast_word_promoted_to_dict_format), promotedStr, UserTempDictionary.MAX_OCCURENCES));

                        _appSettings.updateUserTempDict(_appSettings.userTempDict);
                        AddToDictionaryActivity.addToDictionary(promotedStr, promotedUntranslatedStr);
                    }
                } catch (Exception ex) {
                    log("! Ex Thread: " + ex.getMessage());
                }
            }
        });
    }


    private void updateStatistics(ActionType actionType) {
        if (actionType.isAuto()) {
            _appSettings.increseStatisticsAutoChange();
        } else {
            _appSettings.increseStatisticsManualChange();
        }
    }


    private static boolean isReplaceableInput(int inputType) {
        // Check if any of Not Proceeding Types
        for (int notHandling : _notHandleInputs) {
            if (inputType == notHandling) {
                return false;
            }
        }
        return true;
    }



    private static void log(String message) {
        Log.d(LOG_TAG, message);
        if (_appSettings != null && _appSettings.isLogToFile) {
            logToFile(message);
        }
        //logToToast(message);
    }


    private void logToToast(String text) {
        if (_toast == null) {
            _toast = Toast.makeText(this.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        _toast.setText(text);
        _toast.show();
    }


    private static void logToFile(String text) {
        File appFolder = App.createAppFolder();
        try {
            File file = new File(appFolder, "tlt_log.txt");
            if (!file.exists())
            {
                file.createNewFile();
            }
            FileOutputStream fOut = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            osw.write(text + "\n");
            osw.flush();
            osw.close();
        } catch (Exception ex) {
            //Log.d(LOG_TAG, "Ex: " + ex.getMessage());
        }
    }

    private static String encrypt(String text) {
        StringBuilder encText = new StringBuilder();
        char newChar;
        for (int i = 0; i < text.length(); i++) {
            newChar = (char)(text.charAt(i) + 3);
            encText.append(newChar);
        }
        return encText.toString();
    }

    @TargetApi(26)
    private static void traverseTree(AccessibilityNodeInfo node, int level) {
        String outline = "";
        for (int j = 0; j < level; j++) { outline += "  "; }
        log(outline + "c=" + node.getClassName() +
                "; t=" + node.getText() +
                "; s=" + node.getTextSelectionStart() +
                "; e=" + node.getTextSelectionEnd() +
                "; c=" + node.getChildCount()
        );

        if (node.getClassName().equals("android.webkit.WebView")) {
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            traverseTree(node.getChild(i), level + 1);
        }
    }

    private void notify(Language lang, ActionType actionType) {
        if (isNotificationsEnabled()) {
            playSound(lang, actionType);
            vibrate(lang, actionType);
        }

        /*
        InputMethodManager imeManager = (InputMethodManager) getApplicationContext().getSystemService(INPUT_METHOD_SERVICE);
        //imeManager.showInputMethodPicker();
        List<InputMethodInfo> lst = imeManager.getInputMethodList();
        for (InputMethodInfo inf: lst) {
            Log.d(LOG_TAG, "INPUT id: " + inf.getId());
        }
        InputMethodSubtype ims = imeManager.getCurrentInputMethodSubtype();
        String locale = ims.getLocale();
        String mode = ims.getMode();
        String langTag = ims.getLanguageTag();
        int kb = getResources().getConfiguration().keyboard;
        //String locale = ims.getLanguageTag();
        //String locale = Locale.getDefault().getLanguage(); // Default system lang
        Log.d(LOG_TAG, "LANG: '" + locale + "'");
        logToToast("LANG: '" + locale + "'\nmode:" + mode + "'\nlangTag:" + langTag + "'\nkb:" + kb);
        */
    }

    public boolean isNotificationsEnabled() {
        return _appSettings.whenEnableNotifications <= Calendar.getInstance().getTimeInMillis();
    }

    private void vibrate(Language lang, ActionType actionType) {
        switch (lang) {
            case Ru:
            case RuTrans:
            case RuFull: {
                if (actionType == ActionType.AltEnterReplace || actionType == ActionType.CtrlSpace) {
                    // Vibrate only if no Input vibration
                    if (_appSettings.vibrationPatternRus == VibrationPattern.None) {
                        _vibrationManager.vibrate(_appSettings.vibrationPatternRus, _appSettings.vibrationIntensity);
                    }
                } else {
                    _vibrationManager.vibrate(_appSettings.vibrationPatternRus, _appSettings.vibrationIntensity);
                }
                break;
            }
            case En:
            case EnTrans:
            case EnFull: {
                if (actionType == ActionType.AltEnterReplace || actionType == ActionType.CtrlSpace) {
                    // Vibrate only if no Input vibration
                    if (_appSettings.vibrationPatternRus == VibrationPattern.None) {
                        _vibrationManager.vibrate(_appSettings.vibrationPatternEng, _appSettings.vibrationIntensity);
                    }
                } else {
                    _vibrationManager.vibrate(_appSettings.vibrationPatternEng, _appSettings.vibrationIntensity);
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    private void playSound(Language lang, ActionType actionType) {
        switch (lang) {
            case Ru:
            case RuTrans:
            case RuFull: {
                if (actionType == ActionType.AltEnterReplace) {
                    // Play only if no Input sound
                    if (_appSettings.soundInputRus == SoundPattern.None) {
                        _soundManager.play(_appSettings.soundCorrectRus);
                    }
                } else if (actionType == ActionType.AltEnter || actionType == ActionType.CtrlSpace) {
                    _soundManager.play(_appSettings.soundInputRus);
                } else {
                    _soundManager.play(_appSettings.soundCorrectRus);
                }
                break;
            }
            case En:
            case EnTrans:
            case EnFull: {
                if (actionType == ActionType.AltEnterReplace) {
                    // Play only if no Input sound
                    if (_appSettings.soundInputRus == SoundPattern.None) {
                        _soundManager.play(_appSettings.soundCorrectEng);
                    }
                } else if (actionType == ActionType.AltEnter || actionType == ActionType.CtrlSpace) {
                    _soundManager.play(_appSettings.soundInputEng);
                } else {
                    _soundManager.play(_appSettings.soundCorrectEng);
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    public static AppSettings getAppSettings() {
        return _appSettings;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Log.d(LOG_TAG+1, "Setting changed: " + key);
        // Skip if stats update. No need to reload settings
        if (getString(R.string.setting_statistics_manual_changes).equals(key)
            || getString(R.string.setting_statistics_auto_changes).equals(key)){
            return;
        }

        _appSettings.bindSettings(sharedPreferences);


        // Preview Vibration
        if (getString(R.string.setting_vibration_intensity).equals(key)) {
            vibrate(Language.Ru, ActionType.AltEnter);
            if (!_vibrationManager.hasAmplitudeControl()) {
                logToToast(getString(R.string.pref_hint_vibration_intensity));
            }
        }
        if (getString(R.string.setting_vibration_pattern_rus).equals(key)
        ) {
            vibrate(Language.Ru, ActionType.AltEnter);
        }
        if (getString(R.string.setting_vibration_pattern_eng).equals(key)) {
            vibrate(Language.En, ActionType.AltEnter);
        }

        // Preview Input Sound
        if (getString(R.string.setting_sound_input_rus).equals(key)) {
            playSound(Language.Ru, ActionType.AltEnter);
        }
        if (getString(R.string.setting_sound_input_eng).equals(key)) {
            playSound(Language.En, ActionType.AltEnter);
        }

        // Preview Correct Sound
        if (getString(R.string.setting_sound_correct_rus).equals(key)) {
            playSound(Language.Ru, ActionType.ManualChange);
        }
        if (getString(R.string.setting_sound_correct_eng).equals(key)) {
            playSound(Language.En, ActionType.ManualChange);
        }

        // Icon settings
        if (getString(R.string.setting_icon_style_ru).equals(key)
                || getString(R.string.setting_icon_style_en).equals(key)
                || getString(R.string.setting_is_auto_correct).equals(key)
                || getString(R.string.setting_when_enable_notifications).equals(key)
                || getString(R.string.setting_shortcut_enabled_key).equals(key)
                || getString(R.string.setting_is_show_icon).equals(key)
        ) {
            if (_notifyManager == null) {
                _notifyManager = new NotifyManager(this);
            } else {
                _notifyManager.updateNotificationButtons();
            }
            _notifyManager.updateNotification(_currentLanguage);
        }

        // Floating Icon settings
        if (getString(R.string.setting_floating_icon_flag_size).equals(key)
                || getString(R.string.setting_is_show_floating_icon).equals(key)
                || getString(R.string.setting_floating_icon_is_unlocked).equals(key)
                || getString(R.string.setting_floating_icon_style_ru).equals(key)
                || getString(R.string.setting_floating_icon_style_en).equals(key)
                || getString(R.string.setting_floating_icon_animation).equals(key)
                || getString(R.string.setting_floating_icon_text_ru).equals(key)
                || getString(R.string.setting_floating_icon_text_en).equals(key)
                || getString(R.string.setting_floating_icon_text_color).equals(key)
                || getString(R.string.setting_floating_icon_background_color).equals(key)
                || getString(R.string.setting_floating_icon_opacity).equals(key)
                || getString(R.string.setting_floating_icon_reset_position).equals(key)
                || getString(R.string.setting_floating_icon_is_show_lang_picker).equals(key)
        ) {
            if (_floatingIndicatorManager == null) {
                _floatingIndicatorManager = new FloatingIndicatorManager(this);
            }
            _floatingIndicatorManager.updateSettings();
            _floatingIndicatorManager.setLanguage(_currentLanguage);
        }
    }
}

