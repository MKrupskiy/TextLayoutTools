package by.mkr.blackberry.textlayouttools;

import android.content.ClipData;
import android.content.Context;

public class ClipboardManager {

    private Context _context;

    public ClipboardManager(Context appContext) {
        _context = appContext;
    }

    public String getClipboardedText() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) _context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null) return "";
        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null) return "";
        ClipData.Item item = clip.getItemAt(0);
        if (item == null) return "";
        CharSequence textToPaste = item.getText();
        if (textToPaste == null) return "";

        return textToPaste.toString();
    }

    public void setClipboardedText(String textToCopy) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) _context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(null, textToCopy);
        if (clipboard == null) return;
        clipboard.setPrimaryClip(clip);
    }
}
