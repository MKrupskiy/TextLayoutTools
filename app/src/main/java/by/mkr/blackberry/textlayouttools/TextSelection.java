package by.mkr.blackberry.textlayouttools;

import android.view.accessibility.AccessibilityNodeInfo;

public class TextSelection {
    private AccessibilityNodeInfo _nodeInfo;
    private String _inputText;
    private int _startSelection;
    private int _endSelection;
    private int _leftMaxLetters = 30;
    private int _rightMaxLetters = 30;

    public TextSelection() {
        _nodeInfo = null;
        _inputText = null;
        _startSelection = 0;
        _endSelection = 0;
    }

    // Getters Setters
    public AccessibilityNodeInfo get_nodeInfo() {
        if (_nodeInfo != null) { _nodeInfo.refresh(); }
        return _nodeInfo;
    }
    public void set_nodeInfo(AccessibilityNodeInfo nodeInfo) {
        this._nodeInfo = nodeInfo;
    }

    public String get_inputText() {
        return _inputText;
    }
    public String get_inputTextTruncated() {
        int pos = this.get_endSelection() -1;// -1 because of corrections in the middle of the string ("hello NEW |world")
        int leftBorder = pos - _leftMaxLetters > 0 ? pos - _leftMaxLetters : 0;
        int rightBorder = pos + _rightMaxLetters > _inputText.length()-1 ? pos + _rightMaxLetters : _inputText.length()-1;
        return _inputText.substring(leftBorder, rightBorder);
    }
    public void set_inputText(String _inputText) {
        this._inputText = _inputText;
    }

    public int get_startSelection() {
        return _startSelection;
    }
    public void set_startSelection(int _startSelection) {
        this._startSelection = _startSelection < 0 ? 0 : _startSelection;
    }

    public int get_endSelection() {
        return _endSelection;
    }
    public void set_endSelection(int _endSelection) {
        this._endSelection = _endSelection < 0 ? 0 : _endSelection;
    }

    public void set_all(AccessibilityNodeInfo nodeInfo, String inputText, int startSelection, int endSelection) {
        set_nodeInfo(nodeInfo);
        set_inputText(startSelection < 0 ? "" : inputText);
        set_startSelection(startSelection);
        set_endSelection(endSelection);
    }


    // Methods
    public boolean isTextNullOrEmpty() {
        return _inputText == null || "".equals(_inputText);
    }

    public boolean isStartEqualsEnd() {
        return _startSelection == _endSelection;
    }

    public void clearAll() {
        _nodeInfo = null;
        _inputText = null;
        _startSelection = 0;
        _endSelection = 0;
    }

    public void copyFrom(TextSelection fromTextSelection) {
        set_nodeInfo(fromTextSelection.get_nodeInfo());
        set_inputText(fromTextSelection.get_inputText());
        set_startSelection(fromTextSelection.get_startSelection());
        set_endSelection(fromTextSelection.get_endSelection());
    }

    public String getSelectedText() {
        return _inputText.substring(_startSelection, _endSelection);
    }

    public String getBeforeText() {
        return _inputText.substring(0, _startSelection);
    }

    public String getAfterText() {
        return _inputText.substring(_endSelection);
    }

    public Character getLastEnteredChar() {
        if (_inputText == null || _inputText.length() == 0) {
            return null;
        } else {
            return _inputText.charAt(_endSelection - 1);
        }
    }
}
