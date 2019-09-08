package by.mkr.blackberry.textlayouttools;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static by.mkr.blackberry.textlayouttools.R.string.setting_control_key;
import static by.mkr.blackberry.textlayouttools.R.string.setting_input_method;
import static by.mkr.blackberry.textlayouttools.R.string.setting_is_show_info;
import static by.mkr.blackberry.textlayouttools.R.string.setting_key2emulation_enabled;

public class ReplacerService extends android.accessibilityservice.AccessibilityService {

    final String LOG_TAG = "ReplacerLog";
    private static List<String> notHandlePackages = Arrays.asList(
            "com.android.systemui",
            //"com.blackberry.blackberrylauncher",
            "com.blackberry.keyboard"
    );

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



    @Override
    protected void onServiceConnected() {
        _notifyManager = new NotifyManager(this);
        _languageDetector = new LanguageDetector();

        _replacerSelect  = new TextSelection();
        _controlSelect  = new TextSelection();

        _wordsListRu = new WordsList(this, Language.Ru);
        _wordsListEn = new WordsList(this, Language.En);
    }


    // Tracks Hotkey presses
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        // action = 0 - press, 1 - release
        // mod 129 - BB right ctrl key, 65 - left ctrl
        // key code = 45 - Q
        try {
            boolean isHandled = false;
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            int pressAction = event.getAction();
            boolean isEnabled = sharedPrefs.getBoolean(getString(R.string.setting_shortcut_enabled_key), true);
            String selectedCtrlMod = sharedPrefs.getString(getString(setting_control_key), "129");
            boolean isKey2EmulationEnabled = sharedPrefs.getBoolean(getString(setting_key2emulation_enabled), false);

            // Change Text Layout
            if (isEnabled && pressAction == 0) {
                String keyCode = "" + event.getKeyCode();
                String keyMod = "" + event.getModifiers();
                String selectedShortCut = sharedPrefs.getString(getString(R.string.setting_shortcut_key), "" + KeyEvent.KEYCODE_Q);
                boolean isTranslit = !"0".equals(sharedPrefs.getString(getString(setting_input_method), "0"));
                boolean isSelectReplaced = sharedPrefs.getBoolean(getString(R.string.setting_select_replaced), false);
                boolean isShowInfo = sharedPrefs.getBoolean(getString(setting_is_show_info), true);
                char display = event.getDisplayLabel();
                boolean isAltEnter = keyMod.equals("18") && keyCode.equals("" + KeyEvent.KEYCODE_ENTER);

                if (isShowInfo) {
                    String logInfo = "Key: " + keyCode +
                            "\nMod: " + keyMod +
                            "\nAlt: " + event.isAltPressed() +
                            "\nShift: " + event.isShiftPressed() +
                            "\nCtrl: " + event.isCtrlPressed() +
                            "\nFunc: " + event.isFunctionPressed();
                    //Toast.makeText(this.getApplicationContext(), logInfo, Toast.LENGTH_SHORT).show();
                    logToToast(logInfo);
                }

                //Log.d(LOG_TAG, "mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; display=" + display);
                //logToFile("mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; display=" + display);
                // Ctrl+G: mod=18; code=66; action=0 - alt+enter

                if (keyMod.equals(selectedCtrlMod) && keyCode.equals(selectedShortCut)
                        || isAltEnter
                ) {
                    _isProgramChange = true;
                    log(LOG_TAG, "_isProgramChange:" + _isProgramChange);

                    if (_replacerSelect.get_nodeInfo() != null && !_replacerSelect.isTextNullOrEmpty()) {
                        // Change SELECTED text
                        if (!_replacerSelect.isStartEqualsEnd()) {
                            replaceSelectedText(_replacerSelect, isTranslit, isSelectReplaced);
                        } else {
                            // Change last Entered word


                            // Do not change via Alt+Enter
                            if (!isAltEnter) {


                                // Lang change
                                int cursorAt = _replacerSelect.get_endSelection();
                                int wordEnd = LanguageDetector.getNearestWordEnd(_replacerSelect.get_inputText(), _replacerSelect.get_endSelection());
                                log(LOG_TAG, "getNearestWordEnd: " + wordEnd + "; sel=" + _replacerSelect.get_endSelection());
                                WordWithBoundaries currentWord = LanguageDetector.getWordAtPosition(_replacerSelect.get_inputText(), wordEnd);
                                log(LOG_TAG, "Current Word: " + currentWord.Word + "; end:" + wordEnd);

                                _lastAutoReplaced = currentWord.Begin;
                                log(LOG_TAG, "_lastAutoReplaced: " + _lastAutoReplaced);

                                _replacerSelect.set_startSelection(currentWord.Begin);
                                _replacerSelect.set_endSelection(currentWord.End);
                                replaceSelectedText(_replacerSelect, isTranslit, isSelectReplaced, cursorAt);

                            }



                        }

                        isHandled = true && !isAltEnter;

                    } else {
                        log(LOG_TAG, "No text selected");
                    }
                }
            }

            // Emulate CTRL+Key combinations
            if (isKey2EmulationEnabled && pressAction == 0) {
                String keyCode = "" + event.getKeyCode();
                String keyMod = "" + event.getModifiers();




                AccessibilityNodeInfo eventNodeInfo = super.getRootInActiveWindow().findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT);
                log(LOG_TAG, "  eventNodeInfo: " + eventNodeInfo.getPackageName() +
                        "; c=" + eventNodeInfo.getClassName() +
                        "; t=" + eventNodeInfo.getText() +
                        "; s=" + eventNodeInfo.getTextSelectionStart() +
                        "; e=" + eventNodeInfo.getTextSelectionEnd()
                );

                for (int i = 0; i < eventNodeInfo.getChildCount(); i++) {
                    AccessibilityNodeInfo ch = eventNodeInfo.getChild(i);
                    log(LOG_TAG, "    c[" + i + "]: c=" + ch.getClassName() +
                            "; t=" + ch.getText() +
                            "; s=" + ch.getTextSelectionStart() +
                            "; e=" + ch.getTextSelectionEnd() +
                            "; c=" + ch.getChildCount()
                    );
                    for (int j = 0; j < ch.getChildCount(); j++) {
                        AccessibilityNodeInfo ch1 = eventNodeInfo.getChild(j);
                        log(LOG_TAG, "      ch1[" + j + "]: c=" + ch.getClassName() +
                                "; t=" + ch.getText() +
                                "; s=" + ch.getTextSelectionStart() +
                                "; e=" + ch.getTextSelectionEnd() +
                                "; c=" + ch.getChildCount()
                        );
                    }
                }








                if (keyMod.equals(selectedCtrlMod) && keyCode.equals("" + KeyEvent.KEYCODE_A)) {
                    log(LOG_TAG, "Ctrl+A");

                    if (_controlSelect.get_nodeInfo() != null && !_controlSelect.isTextNullOrEmpty()) {
                        log(LOG_TAG, "Input: " + _controlSelect.get_inputText());

                        _controlSelect.set_startSelection(0);
                        _controlSelect.set_endSelection(_controlSelect.get_inputText().length());

                        _replacerSelect.copyFrom(_controlSelect);
                        log(LOG_TAG, "Replacer: " + _replacerSelect.get_inputText() +
                                "; s=" + _replacerSelect.get_startSelection() +
                                "; e=" + _replacerSelect.get_endSelection()
                        );

                        Bundle selectArguments = new Bundle();
                        selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, 0);
                        selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, _controlSelect.get_endSelection());
                        _controlSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectArguments);
                    }
                    isHandled = true;
                } else if (keyMod.equals(selectedCtrlMod) && keyCode.equals("" + KeyEvent.KEYCODE_C)) {
                    Log.d(LOG_TAG, "Ctrl+C");
                    _controlSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_COPY);
                    isHandled = true;
                } else if (keyMod.equals(selectedCtrlMod) && keyCode.equals("" + KeyEvent.KEYCODE_V)) {
                    Log.d(LOG_TAG, "Ctrl+V");
                    _controlSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    isHandled = true;
                } else if (keyMod.equals(selectedCtrlMod) && keyCode.equals("" + KeyEvent.KEYCODE_X)) {
                    Log.d(LOG_TAG, "Ctrl+X");


                    _wordsListRu.contains(_controlSelect.get_inputText());


                    _controlSelect.get_nodeInfo().performAction(AccessibilityNodeInfo.ACTION_CUT);
                    isHandled = true;
                } else if (keyMod.equals(selectedCtrlMod) && keyCode.equals("" + KeyEvent.KEYCODE_Z)) {
                    /*
                    //logToToast("Ctrl+Z");
                    if (_controlSelect.get_nodeInfo() != null) {
                    }
                    isHandled = true;
                    */
                }
            }

            if (isHandled) {
                // Prevent default keyboard action
                return true;
            }

        } catch (Exception ex) {
            log(LOG_TAG, "! Ex2: " + ex.getMessage());
            log(LOG_TAG, "! Ex2: " + ex);
            //logToToast("! Ex: " + ex.getMessage());
        }

        _isProgramChange = false;
        log(LOG_TAG, "_isProgramChange: " + _isProgramChange);

        // Nothing to do - default keyboard action
        return super.onKeyEvent(event);
    }

    // Tracks selection changes
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        log(LOG_TAG, "Accessibility; package=" + event.getPackageName() + "; type=" + event.getEventType());
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isKey2EmulationEnabled = sharedPrefs.getBoolean(getString(setting_key2emulation_enabled), false);

        try {
            switch (event.getEventType()) {

                // Fix double letters in Russian
                case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {

                    String tempStr = event.getSource().getText().toString();
                    _controlSelect.set_all(
                            event.getSource(),
                            tempStr,
                            event.getSource().getTextSelectionStart(),
                            event.getSource().getTextSelectionEnd()
                    );
                    log(LOG_TAG, "TYPE_VIEW_TEXT_CHANGED; text=" + _controlSelect.get_inputText() +
                            "; start=" + _controlSelect.get_startSelection() +
                            "; end=" + _controlSelect.get_endSelection()
                    );


                    boolean isCorrectDoubled = sharedPrefs
                            .getBoolean(getString(R.string.setting_is_correct_double_letters), false);
                    if (!isCorrectDoubled) {
                        return;
                    }

                    log(LOG_TAG, "Lang=" + _currentLanguage + "; len=" + event.getText().toString().length());

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
                        boolean isTranslit = !"0".equals(sharedPrefs.getString(getString(setting_input_method), "0"));
                        _currentLanguage = LayoutConverter.getTextLanguage(input, isTranslit);
                    }

                    if (!isSystemReplace
                            && (_currentLanguage == Language.Ru || _currentLanguage == Language.RuTrans)
                    ) {
                        char currentLetter = input.charAt(_textLength - 2);
                        log(LOG_TAG, "TEXT_CHANGED; text=" + input +
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

                    break;
                }

                // Handle selection
                case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED: {
                    AccessibilityNodeInfo tempNodeInfo = event.getSource();
                    //AccessibilityNodeInfo tempNodeInfo = event.getSource().findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT);

                    int selectBegin = tempNodeInfo.getTextSelectionStart();
                    int selectEnd = tempNodeInfo.getTextSelectionEnd();
                    String tempStr = event.getText().toString();

                    // ??? inside HTML changes indices are not equal
                    if (tempNodeInfo.getTextSelectionStart() != tempNodeInfo.getParent().findFocus(android.view.accessibility.AccessibilityNodeInfo.FOCUS_INPUT).getTextSelectionStart()) {
                        selectBegin = 0;
                    }

                    log(LOG_TAG, "TYPE_VIEW_TEXT_SELECTION_CHANGED; " + tempStr+
                            "; s=" + selectBegin +
                            "; e=" + selectEnd +
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
                        log(LOG_TAG, "Clear replacer; " + selectBegin);





                        boolean isAutoCorrect = sharedPrefs.getBoolean(getString(R.string.setting_is_auto_correct), true);

                        log(LOG_TAG, "CHECK _isProgramChange:" + _isProgramChange);
                        if (!_isProgramChange && isAutoCorrect) {
                            boolean isTranslit = !"0".equals(sharedPrefs.getString(getString(setting_input_method), "0"));
                            boolean isSelectReplaced = false;
                            Character lastChar = _replacerSelect.getLastChar();
                            boolean isWordLetter = LanguageDetector.isWordLetter(lastChar);
                            log(LOG_TAG, "^^^^^^^^^^^^^");
                            log(LOG_TAG, "CHAR:" + lastChar + "; is word:" + isWordLetter);

                            if (!isWordLetter) {
                                // Automatic lang change
                                String[] userDict = sharedPrefs
                                        .getString(getString(R.string.setting_user_dictionary), "")
                                        .toLowerCase()
                                        .split("\n");
                                log(LOG_TAG, "userDict:" + userDict.length);

                                int cursorAt = _replacerSelect.get_endSelection();
                                int wordEnd = LanguageDetector.getNearestWordEnd(_replacerSelect.get_inputText(), _replacerSelect.get_endSelection());
                                log(LOG_TAG, "getNearestWordEnd: " + wordEnd + "; sel=" + _replacerSelect.get_endSelection());
                                WordWithBoundaries currentWord = LanguageDetector.getWordAtPosition(_replacerSelect.get_inputText(), wordEnd);
                                log(LOG_TAG, "Current Word: " + currentWord.Word + "; end:" + wordEnd);

                                log(LOG_TAG, "_lastAutoReplaced CHECK: " + _lastAutoReplaced);
                                if (currentWord.Begin == _lastAutoReplaced) {
                                    log(LOG_TAG, "Already handled begin:" + currentWord.Begin);
                                    return;
                                }
                                _lastAutoReplaced = currentWord.Begin;
                                log(LOG_TAG, "_lastAutoReplaced: " + _lastAutoReplaced);

                                Language lang = _languageDetector.getTargetLanguage(currentWord.Word, isTranslit, _wordsListRu, _wordsListEn, userDict);

                                Language textEnteredLang = LayoutConverter.getTextLanguage(currentWord.Word, isTranslit);

                                if (lang == textEnteredLang || lang == Language.Unknown) {
                                    // Input language == keyboard language
                                    // Do nothing
                                    log(LOG_TAG, "***From: " + textEnteredLang +
                                            "; To: '" + lang +
                                            "; " + currentWord.Word +
                                            "' no change" +
                                            "; s=" + currentWord.Begin +
                                            "; e=" + currentWord.End);
                                } else {
                                    // Need to change layout
                                    String replacedWord = LayoutConverter.getReplacedText(currentWord.Word, textEnteredLang);
                                    log(LOG_TAG, "***From: " + textEnteredLang +
                                            "; To: " + lang +
                                            "; '" + currentWord.Word +
                                            "' -> '" + replacedWord + "'");

                                    _replacerSelect.set_startSelection(currentWord.Begin);
                                    _replacerSelect.set_endSelection(currentWord.End);
                                    replaceSelectedText(_replacerSelect, isTranslit, isSelectReplaced, cursorAt);
                                }
                            } else {
                                _lastAutoReplaced = -1;
                                log(LOG_TAG, "_lastAutoReplaced: " + _lastAutoReplaced);
                            }
                            log(LOG_TAG, "vvvvvvvvvvvv");
                        }
                        _isProgramChange = true;
                        log(LOG_TAG, "_isProgramChange:" + _isProgramChange);













                        //_replacerSelect.clearAll();
                        return;
                    }

                    log(LOG_TAG, "text=" + tempStr + "; begin=" + selectBegin + "; end=" + selectEnd);

                    break;
                }

                // Track language changes
                case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: {
                    if (!"com.blackberry.keyboard".equals(event.getPackageName())) {
                        return;
                    }

                    log(LOG_TAG, "Toast event; package=" + event.getPackageName());

                    String text = (String) event.getText().get(0);
                    log(LOG_TAG, text);

                    boolean isTranslit = !"0".equals(sharedPrefs.getString(getString(setting_input_method), "0"));
                    boolean isShowIcon = sharedPrefs.getBoolean(getString(R.string.setting_is_show_icon), true);

                    boolean isRussian = text.contains("Русск") || text.contains("Russ");
                    _currentLanguage = isRussian
                            ? (isTranslit ? Language.RuTrans : Language.Ru)
                            : (isTranslit ? Language.EnTrans : Language.En);


                    if (isShowIcon) {
                        _notifyManager.updateNotification(_currentLanguage);
                    } else {
                        _notifyManager.clearNotifications();
                    }

                    break;
                }

                // Track input field change
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
                    if (isKey2EmulationEnabled && !notHandlePackages.contains(event.getPackageName())) {
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
                                log(LOG_TAG, "TYPE_WINDOW_STATE_CHANGED: " + _controlSelect.get_inputText()
                                        + "; s=" + _controlSelect.get_startSelection() +
                                        "; e=" + _controlSelect.get_endSelection());
                            }
                        }
                    }

                    break;
                }

                case AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED:{
                    if (isKey2EmulationEnabled && !notHandlePackages.contains(event.getPackageName())) {
                        log(LOG_TAG, "TYPE_VIEW_CONTEXT_CLICKED: " + event.getText());
                    }

                    break;
                }

                case AccessibilityEvent.TYPE_VIEW_CLICKED:{
                    if (isKey2EmulationEnabled && !notHandlePackages.contains(event.getPackageName())) {
                        //AccessibilityNodeInfo tempNodeInfo = event.getSource();
                        AccessibilityNodeInfo tempNodeInfo = event.getSource().findFocus(AccessibilityNodeInfo.FOCUS_INPUT);

                        if (tempNodeInfo != null) {
                            log(LOG_TAG, "TYPE_VIEW_CLICKED: " + event.getText() +
                                    "; c=" + tempNodeInfo.getClassName() +
                                    "; t=" + tempNodeInfo.getText() +
                                    "; s=" + tempNodeInfo.getTextSelectionStart() +
                                    "; e=" + tempNodeInfo.getTextSelectionEnd()
                            );

                            // ??? html
                            if (tempNodeInfo.getText() == null) {
                                tempNodeInfo = event.getSource();
                                log(LOG_TAG, "TYPE_VIEW_CLICKED: " + event.getText() +
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
                    if (isKey2EmulationEnabled && !notHandlePackages.contains(event.getPackageName())) {
                        //AccessibilityNodeInfo tempNodeInfo = event.getSource();
                        AccessibilityNodeInfo tempNodeInfo = event.getSource().findFocus(AccessibilityNodeInfo.FOCUS_INPUT);//not in hub

                        if (tempNodeInfo != null) {
                            log(LOG_TAG, "TYPE_VIEW_FOCUSED: " + event.getText() +
                                    "; c=" + tempNodeInfo.getClassName() +
                                    "; t=" + tempNodeInfo.getText() +
                                    "; s=" + tempNodeInfo.getTextSelectionStart() +
                                    "; e=" + tempNodeInfo.getTextSelectionEnd()+
                                    "; c=" + event.getSource().getParent().getChildCount()
                            );

                            // ??? html
                            if (tempNodeInfo.getText() == null) {
                                tempNodeInfo = event.getSource();
                                log(LOG_TAG, "TYPE_VIEW_FOCUSED: " + event.getText() +
                                        "; c=" + tempNodeInfo.getClassName() +
                                        "; t=" + tempNodeInfo.getText() +
                                        "; s=" + tempNodeInfo.getTextSelectionStart() +
                                        "; e=" + tempNodeInfo.getTextSelectionEnd()+
                                        "; c=" + event.getSource().getParent().getChildCount()
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


                default: {
                    log(LOG_TAG, "Unknown: " + event.getEventType());
                    break;
                }
            }
        } catch (Exception ex) {
            log(LOG_TAG, "! Ex: " + ex.getMessage());
            log(LOG_TAG, "! Ex: " + ex);
        }
        log(LOG_TAG, "-----");
    }


    @Override
    public void onInterrupt() {
        log(LOG_TAG,"Interrupt");
        _notifyManager.clearNotifications();
    }


    private void replaceSelectedText(TextSelection textSelect, boolean isTranslit, boolean isSelectReplaced, int newCursor) {
        // Get selected text
        String selectedText = textSelect.getSelectedText();
        String beforeText = textSelect.getBeforeText();
        String afterText = textSelect.getAfterText();
        int startSelection = textSelect.get_startSelection();
        log(LOG_TAG, "1=" + beforeText + "; t=" + selectedText + "; a=" + afterText);

        // Get converted text
        _currentLanguage = LayoutConverter.getTextLanguage(selectedText, isTranslit);
        String replacedText = LayoutConverter.getReplacedText(selectedText, _currentLanguage);

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

        _isProgramChange = true;
        log(LOG_TAG, "_isProgramChange:" + _isProgramChange);

        //textSelect.clearAll();
    }
    private void replaceSelectedText(TextSelection textSelect, boolean isTranslit, boolean isSelectReplaced) {
        replaceSelectedText(textSelect, isTranslit, isSelectReplaced, -1);
    }



    private void log(String logTag, String message) {
        Log.d(logTag, message);
        boolean isLogToFile = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.setting_is_log_to_sd), false);
        if (isLogToFile) {
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
        File sdCard = Environment.getExternalStorageDirectory();
        try {
            File file = new File(sdCard, "tlt_log.txt");
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
            //Log.d("ReplacerLog", "Ex: " + ex.getMessage());
        }
    }
}

