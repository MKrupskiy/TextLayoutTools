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
        AppThemeHelper.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        AppSettings appSettings = ReplacerService.getAppSettings();
        if (appSettings == null) {
            return;
        }

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
        updateValue(R.id.text_stats_manual_changes_val, appSettings.manualChangesCount,0);
        // Auto
        updateValue(R.id.text_stats_auto_changes_val, appSettings.autoChangesCount, 0);
        // Dictionary words
        updateValue(R.id.text_stats_dictionary_words_val, appSettings.userDict.length, 0);
        // Blacklisted apps union
        updateValue(R.id.text_stats_blacklist_union_val, appSettings.appsBlackListAll.size() + appSettings.appsBlackListAutocorrect.size(), 0);

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
    }

    private void updateValue(int resId, int value, int total) {
        TextView tvValue = findViewById(resId);
        if (total == 0) {
            tvValue.setText("" + value);
        } else {
            tvValue.setText(value + " / " + total);
        }
    }

}
