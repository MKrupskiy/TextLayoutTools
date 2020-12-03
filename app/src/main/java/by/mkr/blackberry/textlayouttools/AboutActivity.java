package by.mkr.blackberry.textlayouttools;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Calendar;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;

public class AboutActivity extends AppCompatActivity {
    private int _pressedCount = 0;
    private String _updateLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppThemeHelper.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String emailAction = "mailto:mike_113@mail.ru?subject=Text Layout Tools " + BuildConfig.VERSION_NAME + " [" + Build.MODEL + ", " + Build.VERSION.SDK_INT + "]";
                emailIntent.setData(Uri.parse(emailAction));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_email_title)));
            }
        });


        final ImageView iconImage = findViewById(R.id.iconView);
        updImg(iconImage);
        iconImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _pressedCount++;
                if (_pressedCount == 3) {
                    Animation animBlink = AnimationUtils.loadAnimation(iconImage.getContext(), R.anim.blink);
                    animBlink.setDuration(1000);
                    iconImage.startAnimation(animBlink);
                    _pressedCount = 0;
                }
            }
        });


        AppSettings appSettings = ReplacerService.getAppSettings();
        _updateLink = appSettings.updateLink;

        final Button newVersionAvailButton = findViewById(R.id.btn_update_text);
        newVersionAvailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(_updateLink)));
            }
        });
        // Initial state if update is available
        if (appSettings.isUpdateAvailable) {
            newVersionAvailButton.setVisibility(View.VISIBLE);
        }
        final TextView newVersionNotAvailButton = findViewById(R.id.text_upd_not_available);
        final TextView newVersionNoInternet = findViewById(R.id.text_upd_no_internet);

        final ImageButton updateButton = findViewById(R.id.btn_update);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newVersionAvailButton.setVisibility(View.GONE);
                newVersionNotAvailButton.setVisibility(View.GONE);
                newVersionNoInternet.setVisibility(View.GONE);
                fetchLatestVersion(newVersionAvailButton, newVersionNotAvailButton, newVersionNoInternet);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void updImg(ImageView icnImg) {
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int resId = 0;
        if (day == 31 && month == 11) resId = R.drawable.ic_icon_new_year;
        else if (day == 1 && month == 0) resId = R.drawable.ic_icon_new_year;
        else if (day == 7 && month == 0) resId = R.drawable.ic_icon_christmas_2;
        else if (day == 23 && month == 1) resId = R.drawable.ic_icon_defenders;
        else if (day == 8 && month == 2) resId = R.drawable.ic_icon_women;
        else if (day == 9 && month == 4) resId = R.drawable.ic_icon_victory;
        else if (day == 1 && month == 7) resId = R.drawable.ic_icon_pikabu;
        else if (day == 31 && month == 9) resId = R.drawable.ic_icon_halloween;
        else if (day == 25 && month == 11) resId = R.drawable.ic_icon_christmas_1;
        if (resId != 0) { icnImg.setImageResource(resId); }
    }

    private void fetchLatestVersion(final View buttonAvailable, final View buttonNotAvailable, final View buttonNoInternet) {
        final Activity that = this;
        HttpHelper.fetchLatestVersion(that, new HttpCallback() {
            @Override
            public void execute(final String[] text) {
                if (text == null || text.length < 2) {
                    Log.d(LOG_TAG, "fetchLatestVersion error: No info");
                    buttonNoInternet.setVisibility(View.VISIBLE);
                    return;
                }
                boolean isNewVersionAvailable = BuildConfig.VERSION_NAME.compareTo(text[0]) < 0;
                _updateLink = text[1];
                Log.d(LOG_TAG, "Current: " + BuildConfig.VERSION_NAME + "; Latest: " + text[0] + ". Link: " + _updateLink);

                // Update settings
                AppSettings.setSetting(R.string.setting_application_updates_available, isNewVersionAvailable, that.getApplicationContext());
                AppSettings.setSetting(R.string.setting_application_updates_link, _updateLink, that.getApplicationContext());
                // Update UI
                if (isNewVersionAvailable) {
                    buttonAvailable.setVisibility(View.VISIBLE);
                    buttonNotAvailable.setVisibility(View.GONE);
                    buttonNoInternet.setVisibility(View.GONE);
                } else {
                    buttonAvailable.setVisibility(View.GONE);
                    buttonNotAvailable.setVisibility(View.VISIBLE);
                    buttonNoInternet.setVisibility(View.GONE);
                }
            }
        });
    }
}
