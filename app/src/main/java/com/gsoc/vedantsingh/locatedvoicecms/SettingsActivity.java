package com.gsoc.vedantsingh.locatedvoicecms;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gsoc.vedantsingh.locatedvoicecms.utils.LGUtils;
import com.gsoc.vedantsingh.locatedvoicecms.utils.PoisGridViewAdapter;
import com.jcraft.jsch.Session;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */


public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.za
        bindPreferenceSummaryToValue(findPreference("Machines"));
        bindPreferenceSummaryToValue(findPreference("User"));
        bindPreferenceSummaryToValue(findPreference("Password"));
        bindPreferenceSummaryToValue(findPreference("HostName"));
        bindPreferenceSummaryToValue(findPreference("Port"));
        bindPreferenceSummaryToValue(findPreference("AdminPassword"));
//        bindPreferenceSummaryToValue(findPreference("pref_kiosk_mode"));
//        bindPreferenceSummaryToValue(findPreference("ServerIp"));
//        bindPreferenceSummaryToValue(findPreference("ServerPort"));
        bindPreferenceSummaryToValue(findPreference("AIServerIP"));
        bindPreferenceSummaryToValue(findPreference("AIServerPort"));

        Preference testConnButton = new Preference(this);
        testConnButton.setTitle("AI Server Connection Test");
        testConnButton.setSummary("Click Here to check your connection with the AI Server");
        testConnButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Handle button click here
                String connectionStatus;
                Session session = null;
                session = LGUtils.checkConnectionStatus(session, getApplicationContext());
                if(LGUtils.checkAIServerConnection(session, getApplicationContext())){
                    connectionStatus = "Connected";
                } else {
                    connectionStatus = "Disconnected";
                }

                Toast toast = new Toast(getApplicationContext());
                View toast_view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.toast_text, null);
                TextView toasttext = toast_view.findViewById(R.id.toasttext);
                toasttext.setText("AI Server " + connectionStatus);
                toast.setView(toast_view);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 100);
                toast.show();
                return true;
            }
        });
        getPreferenceScreen().addPreference(testConnButton);

        Preference testAudioButton = new Preference(this);
        testAudioButton.setTitle("AI Server Audio Generation Test");
        testAudioButton.setSummary("Click Here to test AI Server Audio");
        testAudioButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Handle button click here
                onTestButtonClicked();
                return true;
            }
        });
        getPreferenceScreen().addPreference(testAudioButton);
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);


        // Trigger the listener immediately with the preference's
        // current value.
        if (preference.getKey().equals("pref_kiosk_mode")) {
            onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getKey(), false));
        } else if (preference.getKey().contains("Password")) {
            onPreferencePasswordChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        } else {
            onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        }
    }

    private void onTestButtonClicked(){
        PoisGridViewAdapter.playBarkAudioFromText(getApplicationContext(), "This is AI server test");
    }

    private void onPreferencePasswordChange(Preference preference, String string) {
        EditText edit = ((EditTextPreference) preference).getEditText();
        String pref = edit.getTransformationMethod().getTransformation(string, edit).toString();
        preference.setSummary(pref);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference.getKey().contains("Password")) {
            EditText edit = ((EditTextPreference) preference).getEditText();
            String pref = edit.getTransformationMethod().getTransformation(stringValue, edit).toString();
            preference.setSummary(pref);

        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);


        }
        return true;
    }

    /**
     * Això és per a que un cop entrem a settings, al tornar enrere(osigui a LGPCActivity) continui tal qual estava, és a dir,
     * que el contingut del DetailFragment sigui el mateix que abans i no estigui en blanc.
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
