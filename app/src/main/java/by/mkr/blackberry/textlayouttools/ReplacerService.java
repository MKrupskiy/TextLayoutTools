package by.mkr.blackberry.textlayouttools;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

public class ReplacerService extends android.accessibilityservice.AccessibilityService {

    final String LOG_TAG = "ReplacerLog";
    static AccessibilityNodeInfo _nodeInfo;
    static String _inputText;
    static int _startSelection;
    static int _endSelection;
    static Language _currentLanguage;
    static char _lastTypedLetter;
    static long _lastTypedTime;
    static int _textLength;

    String channelId = "by.mkr.blackberry.textlayouttools.layoutnotification";
    private Notification notificationEn;
    private Notification notificationRu;



    @Override
    protected void onServiceConnected() {
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notifyChannel = null;

        // Notification service is based on NeoTheFox's app from 4pda.ru
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                notifyChannel = notifyManager.getNotificationChannel(channelId);
            } catch (Exception e) {
                Log.d(LOG_TAG, "notifyChannel is null");
            }
            if (notifyChannel == null) {
                notifyChannel = new NotificationChannel(
                        channelId,
                        "Current Keyboard Layout",
                        NotificationManager.IMPORTANCE_LOW);
                notifyChannel.setShowBadge(false);
                notifyChannel.enableLights(false);
                notifyChannel.enableVibration(false);
                notifyChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
                notifyManager.createNotificationChannel(notifyChannel);
            }
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationEn = new Notification.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_flag_gb)
                    .setContentTitle("English")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .build();

            notificationRu = new Notification.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_flag_russia)
                    .setContentTitle("Русский")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .build();
        } else {
            notificationEn = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_flag_gb)
                    .setContentTitle("English")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .build();

            notificationRu = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_flag_russia)
                    .setContentTitle("Русский")
                    .setVisibility(Notification.VISIBILITY_SECRET)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .build();
        }
    }


    // Tracks Hotkey presses
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        // action = 0 - press, 1 - release
        // mod 129 - BB right ctrl key, 65 - left ctrl
        // key code = 45 - Q

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        int pressAction = event.getAction();
        boolean isEnabled = sharedPrefs.getBoolean(getString(R.string.setting_shortcut_enabled_key), true);

        if (isEnabled && pressAction == 0) {
            String keyCode = "" + event.getKeyCode();
            String keyMod = "" + event.getModifiers();
            String selectedCtrlMod = sharedPrefs.getString(getString(R.string.setting_control_key), "129");
            String selectedShortCut = sharedPrefs.getString(getString(R.string.setting_shortcut_key), "" + KeyEvent.KEYCODE_Q);
            boolean isTranslit = !sharedPrefs.getString(getString(R.string.setting_input_method), "0").equals("0");
            char display = event.getDisplayLabel();

            //Log.d(LOG_TAG, "mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; display=" + display);
            //logToFile("mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; display=" + display);
            // Ctrl+G: mod=18; code=66; action=0 - alt+enter

            if (keyMod.equals(selectedCtrlMod) && keyCode.equals(selectedShortCut)
                    || keyMod.equals("18") && keyCode.equals("" + KeyEvent.KEYCODE_ENTER)
            ) {

                Log.d(LOG_TAG, "Replace: mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; text=" + _inputText + ";" + _startSelection + ";" + _endSelection);
                try {
                    if (_nodeInfo != null && _inputText != null && !"".equals(_inputText) && _startSelection != _endSelection) {
                        // Get selected text
                        String selectedText = _inputText.substring(_startSelection, _endSelection);
                        String beforeText = _inputText.substring(0, _startSelection);
                        String afterText = _inputText.substring(_endSelection, _inputText.length());
                        Log.d(LOG_TAG, "1=" + beforeText + "; t=" + selectedText + "; a=" + afterText);

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
                        boolean isSelectReplaced = sharedPrefs.getBoolean(getString(R.string.setting_select_replaced), false);
                        Bundle selectArguments = new Bundle();
                        if (isSelectReplaced) {
                            selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, _startSelection);
                            selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, _startSelection + replacedText.length());
                        } else {
                            selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, _startSelection + replacedText.length());
                            selectArguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, _startSelection + replacedText.length());
                        }
                        _nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, replaceArguments);
                        _nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectArguments);



                        _nodeInfo = null;
                        _inputText = null;
                        _startSelection = 0;
                        _endSelection = 0;
                    } else {
                        Log.d(LOG_TAG, "No text selected");
                    }
                } catch (Exception ex) {
                    Log.d(LOG_TAG, "Ex: " + ex.getMessage());
                }
            }
        }
        return super.onKeyEvent(event);
    }

    // Tracks selection changes
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(LOG_TAG, "Accessibility; package=" + event.getPackageName() + "; type=" + event.getEventType());
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            switch (event.getEventType()) {

                // Fix double letters in Russian
                case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {
                    boolean isCorrectDoubled = sharedPrefs
                            .getBoolean(getString(R.string.setting_is_correct_double_letters), false);
                    if (!isCorrectDoubled) {
                        return;
                    }

                    Log.d(LOG_TAG, "Lang; text=" + _currentLanguage + "; len=" + event.getText().toString().length());

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
                        boolean isTranslit = !sharedPrefs.getString(getString(R.string.setting_input_method), "0").equals("0");
                        _currentLanguage = LayoutConverter.getTextLanguage(input, isTranslit);
                    }

                    if (!isSystemReplace
                            && (_currentLanguage == Language.Ru || _currentLanguage == Language.RuTrans)
                    ) {
                        char currentLetter = input.charAt(_textLength - 2);
                        Log.d(LOG_TAG, "TEXT_CHANGED; text=" + input +  "prev=" + _inputText + " len=" + _textLength + "; char=" + currentLetter + "; last=" + _lastTypedLetter + "; time=" + _lastTypedTime + "; diff=" + (new Date().getTime() - _lastTypedTime));

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
                    _nodeInfo = event.getSource();

                    final int selectBegin = _nodeInfo.getTextSelectionStart();
                    final int selectEnd = _nodeInfo.getTextSelectionEnd();
                    if (selectBegin == selectEnd) {
                        _nodeInfo = null;
                        _inputText = null;
                        _startSelection = 0;
                        _endSelection = 0;
                        return;
                    }

                    _inputText = event.getText().toString();
                    _inputText = _inputText.substring(1, _inputText.length() - 1);// remove extra '[]'
                    _startSelection = selectBegin;
                    _endSelection = selectEnd;

                    Log.d(LOG_TAG, "text=" + _inputText + "; begin=" + selectBegin + "; end=" + selectEnd);

                    break;
                }

                // Track language changes
                case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: {
                    if (!"com.blackberry.keyboard".equals(event.getPackageName())) {
                        return;
                    }

                    Log.d(LOG_TAG, "Toast event; package=" + event.getPackageName());

                    String text = (String) event.getText().get(0);
                    Log.d(LOG_TAG, text);

                    boolean isTranslit = !sharedPrefs.getString(getString(R.string.setting_input_method), "0").equals("0");
                    boolean isShowIcon = sharedPrefs.getBoolean(getString(R.string.setting_is_show_icon), true);

                    boolean isRussian = text.contains("Русск") || text.contains("Russ");
                    _currentLanguage = isRussian
                            ? (isTranslit ? Language.RuTrans : Language.Ru)
                            : (isTranslit ? Language.EnTrans : Language.En);


                    if (isShowIcon) {
                        updateNotification(_currentLanguage);
                    } else {
                        clearNotifications();
                    }

                    break;
                }

                default: {
                    break;
                }
            }
        } catch (Exception ex) {
            Log.d(LOG_TAG, "Ex: " + ex.getMessage());
        }
        Log.d(LOG_TAG, "-----");
    }

    @Override
    public void onInterrupt() {
        Log.d(LOG_TAG,"Interrupt");
        clearNotifications();
    }


    private void updateNotification(Language lang) {
        NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        switch (lang) {
            case Ru:
            case RuTrans: {
                notifyManager.notify(123, notificationRu);
                break;
            }
            case En:
            case EnTrans: {
                notifyManager.notify(123, notificationEn);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void clearNotifications() {
        NotificationManager iconManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        iconManager.cancelAll();
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
            Log.d("ReplacerLog", "Ex: " + ex.getMessage());
        }
    }
}

