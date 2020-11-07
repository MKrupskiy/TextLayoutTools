package by.mkr.blackberry.textlayouttools;

import android.app.Activity;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        AppSettings appSettings = ReplacerService.getAppSettings();

        // Enabled switch
        Switch switchEnable = findViewById(R.id.switchEnableStats);
        switchEnable.setChecked(appSettings.isTrackStatistics);
        switchEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppSettings appSettings = ReplacerService.getAppSettings();
                appSettings.toggleStatistics(isChecked);
            }
        });

        // Manual
        updateValue(R.id.text_stats_manual_changes_val, appSettings.manualChangesCount);
        // Auto
        updateValue(R.id.text_stats_auto_changes_val, appSettings.autoChangesCount);
        // Dictionary words
        updateValue(R.id.text_stats_dictionary_words_val, appSettings.userDict.length);

        // Clear button
        final Activity that = this;
        Button btnClear = findViewById(R.id.btn_clear_stats);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppSettings appSettings = ReplacerService.getAppSettings();
                appSettings.clearStatistics();
                that.recreate();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void updateValue(int resId, int value) {
        TextView tvValue = findViewById(resId);
        tvValue.setText("" + value);
    }

}
