package by.mkr.blackberry.textlayouttools;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppThemeHelper.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        AppThemeHelper.setMenuTheme(this, R.id.toolbar);

        // Floating Action Button
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                String emailAction = "mailto:mike_113@mail.ru?subject=Text Layout Tools " + BuildConfig.VERSION_NAME + " [" + Build.MODEL + ", " + Build.VERSION.SDK_INT + "]";
                emailIntent.setData(Uri.parse(emailAction));
                startActivity(Intent.createChooser(emailIntent, getString(R.string.contact_email_title)));
            }
        });

        // Accessibility Button
        Button btnAccessibility = findViewById(R.id.btnAccessibilityOpen);
        btnAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });

        initMuteButtons();

        //boolean isDarkMode = isDarkMode();
        //Log.d(LOG_TAG+1, "isDarkMode: " + isDarkMode);

        // Check if service running
        if (!isServiceRunning(ReplacerService.class)) {
            showServiceDialog();
        }


        //Intent intent = new Intent(this, SettingsActivity.class);
        //intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.ExperimentalPreferenceFragment.class.getName());
        //intent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
        //startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            //Snackbar.make(findViewById(R.id.mainActivity), "Settings clicked", Snackbar.LENGTH_LONG)
            //        .setAction("Action", null).show();
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_statistics) {
            startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
            return true;
        }

        if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void showServiceDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this, AppTheme.getThemeResId(this.getApplicationContext())).create();
        alertDialog.setTitle(getString(R.string.main_service_not_running_alert));
        alertDialog.setIcon(R.drawable.ic_keyboard_google_24dp);
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.dialog_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.main_button_accessibility),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void initMuteButtons() {
        // Mute 1h Button
        Button btnMute1h = findViewById(R.id.btnMute1h);
        btnMute1h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PendingIntent pendingIntent = LanguageNotificationReceiver
                            .createNotificationAction(MainActivity.this, LanguageNotificationReceiver.ACTION_MUTE_1H)
                            .actionIntent;
                    pendingIntent.send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Mute 24h Button
        Button btnMute24h = findViewById(R.id.btnMute24h);
        btnMute24h.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PendingIntent pendingIntent = LanguageNotificationReceiver
                            .createNotificationAction(MainActivity.this, LanguageNotificationReceiver.ACTION_MUTE_8H)
                            .actionIntent;
                    pendingIntent.send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Mute Enable Button
        Button btnEnable = findViewById(R.id.btnMuteEnable);
        btnEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PendingIntent pendingIntent = LanguageNotificationReceiver
                            .createNotificationAction(MainActivity.this, LanguageNotificationReceiver.ACTION_SOUND_ENABLE)
                            .actionIntent;
                    pendingIntent.send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*private boolean isDarkMode() {
        int flags = getApplicationContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK;
        boolean isNight = false;
        switch (flags) {
            case Configuration.UI_MODE_NIGHT_YES:
                isNight = true;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                break;
            default:
                break;
        }
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        return isNight;
    }*/
}
