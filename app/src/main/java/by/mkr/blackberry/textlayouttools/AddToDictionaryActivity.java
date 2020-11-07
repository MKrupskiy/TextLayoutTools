package by.mkr.blackberry.textlayouttools;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;


public class AddToDictionaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get selected text
        Intent intent = getIntent();
        String text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString().toLowerCase();

        if (addToDictionary(text, null)) {
            Toast.makeText(this, getString(R.string.text_toast_word_added), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.text_toast_word_in_dict_already), Toast.LENGTH_SHORT).show();
        }

        finish();
    }


    public static boolean addToDictionary(String text, String untranslatedText) {
        String[] words = splitWords(text);
        // Get user dictionary without gaps by a single word
        AppSettings appSettings = ReplacerService.getAppSettings();
        String[] userDict = appSettings.userDict;
        boolean isAdded = false;

        // Check if word has already been added
        StringBuilder textBuilder = new StringBuilder();
        for (String word : words) {
            if (getElementIndex(userDict, word) == -1) {
                textBuilder.append(textBuilder.toString().isEmpty() ? "" : "\n").append(word);
                isAdded = true;
            }
        }
        text = textBuilder.toString();

        if (isAdded) {
            ArrayList<String> newUserDict = new ArrayList<String>();
            if (untranslatedText != null && untranslatedText.length() > 0) {
                newUserDict = removeFromDict(userDict, untranslatedText);
            } else {
                Collections.addAll(newUserDict, userDict);
            }
            // Save the word into the dictionary
            newUserDict.add(text);

            String[] newUserDictArr = new String[newUserDict.size()];
            newUserDict.toArray(newUserDictArr);
            appSettings.updateUserDict(newUserDictArr);
        }

        return isAdded;
    }

    public static String[] splitWords(String text) {
        if (text != null) {
            return text.split("[^\\w$]+");
        } else {
            return null;
        }

    }

    private static int getElementIndex(String[] userDict, String word) {
        for (int i = 0; i < userDict.length; i++) {
            if (userDict[i].toLowerCase().equals(word.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    private static ArrayList<String> removeFromDict(String[] userDict, String untranslatedText) {
        String[] words = splitWords(untranslatedText);
        ArrayList<String> newUserDict = new ArrayList<String>();

        for (int i = 0; i < userDict.length; i++) {
            if (getElementIndex(words, userDict[i]) == -1) {
                newUserDict.add(userDict[i]);
            }
        }

        return newUserDict;
    }

    @Override
    public void finish() {
        super.finish();
    }
}
