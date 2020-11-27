package by.mkr.blackberry.textlayouttools;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.ContextCompat;

import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.List;

import static by.mkr.blackberry.textlayouttools.ReplacerService.LOG_TAG;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppThemeHelper.setSettingsTheme(this);
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ShortcutPreferenceFragment.class.getName().equals(fragmentName)
                || AutocorrectPreferenceFragment.class.getName().equals(fragmentName)
                || ApplicationPreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || FloatingIndicatorPreferenceFragment.class.getName().equals(fragmentName)
                || AdditionalPreferenceFragment.class.getName().equals(fragmentName)
                || ExperimentalPreferenceFragment.class.getName().equals(fragmentName)
                || BackupPreferenceFragment.class.getName().equals(fragmentName);
    }


    /**
     * This fragment shows Shortcut preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ShortcutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_shortcut);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_input_method)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_control_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_shortcut_key)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows Shortcut preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AutocorrectPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_autocorrect);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_user_dictionary)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_autocorrect_direction)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows Shortcut preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ApplicationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_application);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_application_theme)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows Shortcut preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_sound_input_rus)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_sound_input_eng)));

            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_sound_correct_rus)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_sound_correct_eng)));

            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_vibration_pattern_rus)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_vibration_pattern_eng)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows Shortcut preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FloatingIndicatorPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_floating_indicator);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_floating_icon_style_ru)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_floating_icon_style_en)));

            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_floating_icon_text_ru)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_floating_icon_text_en)));

            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_floating_icon_animation)));

            // Bind custom code preference
            Preference button = findPreference(getString(R.string.setting_floating_icon_reset_position));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // Get trigger value
                    SharedPreferences sharedPrefs = preference.getSharedPreferences();
                    boolean prevValue = sharedPrefs.getBoolean(getString(R.string.setting_floating_icon_reset_position), false);
                    // Save inverted value to notify ReplacerService
                    SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
                    prefsEditor.putInt(getString(R.string.setting_floating_icon_position_x), 0);
                    prefsEditor.putInt(getString(R.string.setting_floating_icon_position_y), 0);
                    prefsEditor.putBoolean(getString(R.string.setting_floating_icon_reset_position), !prevValue);
                    prefsEditor.apply();
                    return true;
                }
            });

            // Main switcher
            checkPermissionsAndSetDefault(this.getActivity());
        }

        @Override
        public void onResume() {
            super.onResume();
            checkPermissionsAndSetDefault(this.getActivity());
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void checkPermissionsAndSetDefault(Activity activity) {
            Preference showIcon = findPreference(getString(R.string.setting_is_show_floating_icon));
            if (Settings.canDrawOverlays(this.getContext())) {
                // OK
                Log.d(LOG_TAG, "canDrawOverlays: OK");
                showIcon.setEnabled(true);
            } else {
                Log.d(LOG_TAG, "canDrawOverlays: denied. Disabling setting");
                showIcon.setEnabled(false);
                // Need to recreate to see the updated setting
                if (((SwitchPreference)showIcon).isChecked()) {
                    // reset
                    SharedPreferences.Editor ed = showIcon.getEditor();
                    ed.putBoolean(showIcon.getKey(), false);
                    ed.commit();
                    activity.recreate();
                }
            }
        }
    }

    /**
     * This fragment shows Shortcut preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AdditionalPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_additional);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_icon_style_ru)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.setting_icon_style_en)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows Shortcut preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ExperimentalPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_experimental);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            // Log to SD switcher
            final Activity thisActivity = this.getActivity();
            Preference buttonSave = findPreference(getString(R.string.setting_is_log_to_sd));
            buttonSave.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (((SwitchPreference)preference).isChecked()) {
                        checkPermission(thisActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows Shortcut preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class BackupPreferenceFragment extends PreferenceFragment {
        private static final int PICKFILE_RESULT_CODE = 9113;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_backup);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            // Bind custom code preference
            // Save
            Preference buttonSave = findPreference(getString(R.string.setting_save_settings_to_file));
            buttonSave.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AppSettings _appSettings = ReplacerService.getAppSettings();
                    boolean isSaved = _appSettings.saveToFile();
                    Toast.makeText(preference.getContext(),
                            isSaved ? getString(R.string.text_toast_settings_saved) : getString(R.string.text_toast_settings_saved_failed),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            // Restore
            Preference buttonRestore = findPreference(getString(R.string.setting_restore_settings_from_file));
            buttonRestore.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
                    chooseFile.setType("application/json");
                    chooseFile.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivityForResult(
                            Intent.createChooser(chooseFile, getString(R.string.text_toast_pick_file)),
                            PICKFILE_RESULT_CODE
                    );
                    return true;
                }
            });

            checkPermission(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            // When a file is selected
            switch (requestCode) {
                case PICKFILE_RESULT_CODE:
                    if (resultCode == RESULT_OK) {
                        Uri fileUri = data.getData();
                        //String filePath = fileUri.getPath();
                        AppSettings _appSettings = ReplacerService.getAppSettings();
                        boolean isRestored = _appSettings.loadFromFile(fileUri);
                        Toast.makeText(getContext(),
                                isRestored ? getString(R.string.text_toast_settings_restored) : getString(R.string.text_toast_settings_restored_failed),
                                Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    public static void checkPermission(Activity activity, String permissionName) {
        if (ContextCompat.checkSelfPermission(activity, permissionName) == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "permission granted: " + permissionName);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{ permissionName }, 1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "permission granted");
        } else {
            Log.d(LOG_TAG, "permission denied");
            Toast.makeText(this.getApplicationContext(), getString(R.string.text_toast_sd_permission_required), Toast.LENGTH_SHORT).show();
        }
    }
}
