package by.mkr.blackberry.textlayouttools;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Random;


public class AboutActivity extends AppCompatActivity {
    private int _pressedCount = 0;
    private int _pressedUpdatesCount = 0;
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
        if (appSettings.isUpdateAvailable()) {
            newVersionAvailButton.setVisibility(View.VISIBLE);
        }
        final TextView newVersionNotAvailButton = findViewById(R.id.text_upd_not_available);
        final TextView newVersionNoInternet = findViewById(R.id.text_upd_no_internet);
        final SwipeRefreshLayout swipeContainer = findViewById(R.id.appblacklist_swipe_container);

        final ImageButton updateButton = findViewById(R.id.btn_update);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchLatestVersion(newVersionAvailButton, newVersionNotAvailButton, newVersionNoInternet, swipeContainer);
            }
        });

        // Refresh swipe
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchLatestVersion(newVersionAvailButton, newVersionNotAvailButton, newVersionNoInternet, swipeContainer);
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
        else if (day == 31 && month == 9) resId = R.drawable.ic_icon_halloween;
        else if (day == 25 && month == 11) resId = R.drawable.ic_icon_christmas_1;
        if (resId != 0) { icnImg.setImageResource(resId); }
    }
    private void pl() {
        _pressedUpdatesCount++;
        if (_pressedUpdatesCount == 5) {
            int resId = 0;
            Random rand = new Random();
            switch (rand.nextInt(7)) {
                case 0: resId = R.raw.sorceress_pissed1; break;
                case 1: resId = R.raw.sorceress_pissed2; break;
                case 2: resId = R.raw.sorceress_pissed3; break;
                case 3: resId = R.raw.sorceress_pissed4; break;
                case 4: resId = R.raw.sorceress_what5; break;
                case 5: resId = R.raw.sorceress_yes1; break;
                case 6: resId = R.raw.sorceress_yes3; break;
                default: resId = R.raw.sorceress_yes1; break;
            }
            SoundManager.play(this.getApplicationContext(), resId);
            _pressedUpdatesCount = 0;
        }
    }

    private void fetchLatestVersion(final View buttonAvailable, final View buttonNotAvailable, final View buttonNoInternet, final SwipeRefreshLayout swipeContainer) {
        final Activity that = this;
        buttonAvailable.setVisibility(View.GONE);
        buttonNotAvailable.setVisibility(View.GONE);
        buttonNoInternet.setVisibility(View.GONE);
        swipeContainer.setRefreshing(true);

        HttpHelper.fetchLatestVersion(that, new HttpCallback() {
            @Override
            public void execute(final String[] text) {
                swipeContainer.setRefreshing(false);

                if (text == null || text.length < 2) {
                    ReplacerService.log("fetchLatestVersion error: No info");
                    buttonNoInternet.setVisibility(View.VISIBLE);
                    return;
                }

                boolean isNewVersionAvailable = BuildConfig.VERSION_NAME.compareTo(text[0]) < 0;
                _updateLink = text[1];
                ReplacerService.log("Current: " + BuildConfig.VERSION_NAME + "; Latest: " + text[0] + ". Link: " + _updateLink);

                // Update settings
                AppSettings.setSetting(R.string.setting_application_updates_available_ver, text[0], that.getApplicationContext());
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

        pl();
    }
}
