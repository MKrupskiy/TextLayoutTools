package by.mkr.blackberry.textlayouttools;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


public class AddToDictionaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get selected text
        Intent intent = getIntent();
        String text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString().toLowerCase();
        String[] words = text.split("[^\\w]+");

        // Get user dictionary without gaps by a single word
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userDictStr = sharedPrefs.getString(getString(R.string.setting_user_dictionary), "")
                .replaceAll("\n{2,}", "\n")
                .replaceAll("\n+$", "");
        String[] userDict = userDictStr.split("\n");
        boolean isAdded = false;
        text = "";

        // Check if word has been added yet
        for (String word : words) {
            boolean isWordExists = false;
            for (String userDictWord : userDict) {
                if (userDictWord.toLowerCase().equals(word.toLowerCase())) {
                    isWordExists = true;
                }
            }
            if (!isWordExists) {
                text += (text.isEmpty() ? "" : "\n") + word;
                isAdded = true;
            }
        }

        if (isAdded) {
            // Save the word into the dictionary
            SharedPreferences.Editor edit = sharedPrefs.edit();
            edit.putString(getString(R.string.setting_user_dictionary), userDictStr + (userDictStr.isEmpty() ? "" : "\n") + text);
            edit.commit();
            Toast.makeText(this, getString(R.string.text_toast_word_added), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.text_toast_word_in_dict_already), Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
