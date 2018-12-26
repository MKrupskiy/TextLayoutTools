package by.mkr.blackberry.textlayouttools;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;




public class ReplacerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        CharSequence text = intent
                .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        boolean readonly = intent
                .getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);
        /*
        Boolean isEnabled = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.switch_enabled_preference), true);
        */
        Boolean isEnabled = true;

        Log.d("ReplacerLog", (readonly ? "ReadOnly": "Editable") + "; " + (isEnabled ? "Enabled": "Disabled"));

        if (isEnabled) {
            // process the text
            Language textLanguage = LayoutConverter.getTextLanguage(text);
            String replacedText = LayoutConverter.getReplacedText(text, textLanguage);
            Log.d("ReplacerLog", "original=" + text + "; replaced=" + replacedText);

            if (!readonly) {
                intent.putExtra(Intent.EXTRA_PROCESS_TEXT, replacedText);
                setResult(RESULT_OK, intent);
            } else {
                Toast.makeText(this, replacedText, Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }
}
