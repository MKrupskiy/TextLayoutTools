package by.mkr.blackberry.textlayouttools;


import android.content.SharedPreferences;
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

public class ReplacerService extends android.accessibilityservice.AccessibilityService {

    final String LOG_TAG = "ReplacerLog";
    static AccessibilityNodeInfo _nodeInfo;
    static String _inputText;
    static int _startSelection;
    static int _endSelection;

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
            boolean isTranslit = sharedPrefs.getString(getString(R.string.setting_input_method), "0").equals("0") ? false : true;
            char display = event.getDisplayLabel();

            Log.d(LOG_TAG, "mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; display=" + display);
            //logToFile("mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; display=" + display);
            // Ctrl+G: mod=18; code=66; action=0 - alt+enter

            if (keyMod.equals(selectedCtrlMod) && keyCode.equals(selectedShortCut)
                || keyMod.equals("18") && keyCode.equals("" + KeyEvent.KEYCODE_ENTER)
            ) {

                Log.d(LOG_TAG, "Ctrl+G: mod=" + keyMod + "; code=" + keyCode + "; action=" + pressAction + "; text=" + _inputText + ";" + _startSelection + ";" + _endSelection);
                try {
                    if (_nodeInfo != null && _inputText != null && _inputText != "" && _startSelection != _endSelection) {
                        // Get selected text
                        String selectedText = _inputText.substring(_startSelection, _endSelection); // remove extra '['
                        String beforeText = _inputText.substring(0, _startSelection);
                        String afterText = _inputText.substring(_endSelection, _inputText.length());
                        Log.d(LOG_TAG, "1=" + beforeText + "; t=" + selectedText + "; a=" + afterText);

                        // Get converted text
                        Language textLanguage = LayoutConverter.getTextLanguage(selectedText, isTranslit);
                        String replacedText = LayoutConverter.getReplacedText(selectedText, textLanguage);

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
        Log.d(LOG_TAG,"Accessibility event; package=" + event.getPackageName());
        Log.d(LOG_TAG, "_selectedText=" + _inputText);
        try {
            /*if (_nodeInfo != null || (_selectedText != null && _selectedText != "")) {
                // is in process
                return;
            }*/

            final int selectBegin = event.getSource().getTextSelectionStart();
            final int selectEnd = event.getSource().getTextSelectionEnd();
            if (selectBegin == selectEnd) {
                _nodeInfo = null;
                _inputText = null;
                _startSelection = 0;
                _endSelection = 0;
                return;
            }
            Log.d(LOG_TAG,"text=" + event.getText());
            Log.d(LOG_TAG,"selectBegin=" + selectBegin + "; selectEnd=" + selectEnd);

            _nodeInfo = event.getSource();
            _inputText = event.getText().toString();
            _inputText = _inputText.substring(1, _inputText.length() - 1);
            //String text = event.getText().toString().substring(selectBegin + 1, selectEnd + 1); // remove extra '['
            _startSelection = selectBegin;
            _endSelection = selectEnd;
        } catch (Exception ex) {
            Log.d(LOG_TAG, "Ex: " + ex.getMessage());
        }
        Log.d(LOG_TAG, "-----");
    }

    @Override
    public void onInterrupt() {
        Log.d(LOG_TAG,"Interrupt");
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

