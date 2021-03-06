package re.jcg.playmusicexporter.activities;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import re.jcg.playmusicexporter.BuildConfig;
import re.jcg.playmusicexporter.R;
import re.jcg.playmusicexporter.services.ExportAllJob;
import re.jcg.playmusicexporter.services.ExportAllService;
import re.jcg.playmusicexporter.settings.PlayMusicExporterPreferences;

import java.text.SimpleDateFormat;
import java.util.List;

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
    private static final String TAG = "MusicExporter_Settings";
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE_ALBA_PATH = 0;
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE_GROUPS_PATH = 1;
    private static final int REQUEST_CODE_OPEN_DOCUMENT_TREE_AUTO_PATH = 2;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
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
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        if (BuildConfig.DEBUG) {
            loadHeadersFromResource(R.xml.pref_debug_header, target);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ExportPreferenceFragment.class.getName().equals(fragmentName)
                || AutomationPreferenceFragment.class.getName().equals(fragmentName)
                || AboutPreferenceFragment.class.getName().equals(fragmentName)
                || DebugPreferenceFragment.class.getName().equals(fragmentName);
    }

    public static class ExportPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_export);
            setHasOptionsMenu(true);


            findPreference("preference_alba_export_path").setOnPreferenceClickListener(preference -> {
                startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT_TREE"), REQUEST_CODE_OPEN_DOCUMENT_TREE_ALBA_PATH);
                return true;
            });
            findPreference("preference_groups_export_path").setOnPreferenceClickListener(preference -> {
                startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT_TREE"), REQUEST_CODE_OPEN_DOCUMENT_TREE_GROUPS_PATH);
                return true;
            });
        }

        public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
            switch (requestCode) {
                case REQUEST_CODE_OPEN_DOCUMENT_TREE_ALBA_PATH:
                    if (resultCode == RESULT_OK) {
                        Uri treeUri = resultData.getData();
                        PlayMusicExporterPreferences.init(getActivity());
                        PlayMusicExporterPreferences.setAlbaExportPath(treeUri);
                        getActivity().getContentResolver().takePersistableUriPermission(treeUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        Log.i(TAG, "Selected " + treeUri.toString());
                    }
                    break;
                case REQUEST_CODE_OPEN_DOCUMENT_TREE_GROUPS_PATH:
                    if (resultCode == RESULT_OK) {
                        Uri treeUri = resultData.getData();
                        PlayMusicExporterPreferences.init(getActivity());
                        PlayMusicExporterPreferences.setGroupsExportPath(treeUri);
                        getActivity().getContentResolver().takePersistableUriPermission(treeUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        Log.i(TAG, "Selected " + treeUri.toString());
                    }
                    break;
                default:
                    Log.i(TAG, "Received activityResult with unknown requestCode");

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

    public static class AutomationPreferenceFragment extends PreferenceFragment {


        /**
         * Little helper method to get the context.
         * If the Android version is high enough, the newer {@link PreferenceFragment#getContext()} is used,
         * else, the older {@link PreferenceFragment#getActivity()}, since activities are contexts too.
         *
         * @return The context.
         */
        @Override
        public Context getContext() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return super.getContext();
            } else {
                return super.getActivity();
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_automation);
            setHasOptionsMenu(true);
            //Schedules an export with the current settings.
            ExportAllJob.scheduleExport(getContext());

            setupOnClickListeners();
        }

        private void setupOnClickListeners() {
            findPreference(PlayMusicExporterPreferences.AUTO_EXPORT_PATH).setOnPreferenceClickListener(preference -> {
                startActivityForResult(new Intent("android.intent.action.OPEN_DOCUMENT_TREE"), REQUEST_CODE_OPEN_DOCUMENT_TREE_AUTO_PATH);
                return true;
            });

            //For every setting that is used to define the export job, setup a listener that
            setupRescheduler(PlayMusicExporterPreferences.AUTO_EXPORT_REQUIRE_CHARGING);
            setupRescheduler(PlayMusicExporterPreferences.AUTO_EXPORT_REQUIRE_UNMETERED);
            setupRescheduler(PlayMusicExporterPreferences.AUTO_EXPORT_ENABLED);
            setupRescheduler(PlayMusicExporterPreferences.AUTO_EXPORT_FREQUENCY);
        }

        private void setupRescheduler(String preference) {
            findPreference(preference).setOnPreferenceClickListener(preference1 -> {
                ExportAllJob.scheduleExport(getContext());
                return false;
            });
        }

        public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
            switch (requestCode) {
                case REQUEST_CODE_OPEN_DOCUMENT_TREE_AUTO_PATH:
                    if (resultCode == RESULT_OK) {
                        Uri treeUri = resultData.getData();
                        PlayMusicExporterPreferences.init(getActivity());
                        PlayMusicExporterPreferences.setAutoExportPath(treeUri);
                        getActivity().getContentResolver().takePersistableUriPermission(treeUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        Log.i(TAG, "Selected " + treeUri.toString());
                    }
                    break;
                default:
                    Log.i(TAG, "Received activityResult with unknown requestCode");

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

    public static class AboutPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_about);
            setHasOptionsMenu(true);
            findPreference("about_version_number").setSummary(BuildConfig.VERSION_NAME);
            findPreference("about_build_date")
                    .setSummary(SimpleDateFormat.getDateInstance().format(BuildConfig.BUILD_TIME));

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

    public static class DebugPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_debug);
            setHasOptionsMenu(true);

            findPreference("debug_trigger_export_all").setOnPreferenceClickListener(preference -> {
                ExportAllService.startExport(getActivity());
                return true;
            });
            findPreference("debug_test_crash_handler").setOnPreferenceClickListener(preference -> {
                throw new IllegalStateException("Test for the crash handler.");
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

}
