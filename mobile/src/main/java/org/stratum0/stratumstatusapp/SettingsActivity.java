package org.stratum0.stratumstatusapp;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

public class SettingsActivity extends Activity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            //TODO: anpassen
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
        super.onCreate(savedInstanceState);

        //TODO: doesn't seem to be needed?!?
        //setupActionBar();

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GeneralPreferenceFragment generalPreferenceFragment = new GeneralPreferenceFragment();
        fragmentTransaction.replace(android.R.id.content, generalPreferenceFragment);
        fragmentTransaction.commit();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("name"));
            bindPreferenceSummaryToValue(findPreference("api_url"));

            bindPreferenceSummaryToValue(findPreference("ssh_server"));
            bindPreferenceSummaryToValue(findPreference("ssh_user_open"));
            bindPreferenceSummaryToValue(findPreference("ssh_user_close"));
//            bindPreferenceSummaryToValue(findPreference("ssh_which_wlans"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || resultCode == 0) return;

        Uri fileUri = data.getData();

        switch (requestCode) {
            case SSHKey.RequestSSHPrivateKeyFileImport:
                Log.d(this.getClass().getName(), String.format("Trying to open file %s", fileUri.getPath()));

                try {
                    BufferedReader sshImportFileReader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(fileUri)));
                    StringBuilder sshImportedKey = new StringBuilder();
                    String line;

                    while ((line = sshImportFileReader.readLine()) != null) {
                        sshImportedKey.append(line).append("\n");
                    }
                    sshImportedKey.deleteCharAt(sshImportedKey.length() - 1);

                    sshImportFileReader.close();

                    SSHKey sshKey = SSHKey.getInstance(this);
                    sshKey.setPrivateKey(sshImportedKey.toString());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case SSHKey.RequestSSHPublicKeyFileExport:
                Log.d(this.getClass().getName(), String.format("Trying to write public key to file %s", fileUri.getPath()));

                try {
                    BufferedWriter sshExportFileWriter = new BufferedWriter(new OutputStreamWriter(getContentResolver().openOutputStream(fileUri)));

                    SSHKey sshKey = SSHKey.getInstance(this);
                    String publicKey = sshKey.getPublicKey();

                    sshExportFileWriter.write(publicKey);
                    sshExportFileWriter.close();

                    Toast.makeText(this, "Exported public key to " + fileUri.getPath(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case SSHKey.RequestSSHPrivateKeyFileExport:
                Log.d(this.getClass().getName(), String.format("Trying to write private key to file %s", fileUri.getPath()));

                try {
                    BufferedWriter sshExportFileWriter = new BufferedWriter(new OutputStreamWriter(getContentResolver().openOutputStream(fileUri)));

                    SSHKey sshKey = SSHKey.getInstance(this);
                    String privateKey = sshKey.getPrivateKey();

                    sshExportFileWriter.write(privateKey);
                    sshExportFileWriter.close();

                    Toast.makeText(this, "Exported private key to " + fileUri.getPath(), Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        HashMap<String,Integer> permissionMap = new HashMap<>();
        for(int i = 0; i<permissions.length; i++) {
            permissionMap.put(permissions[i], grantResults[i]);
        }

        switch (requestCode) {
            case SSHKey.RequestPermissionRead:
                if(permissionMap.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    SSHKey sshKey = SSHKey.getInstance(this);
                    sshKey.importPrivateKeyFromFile();
                }
                break;
            case SSHKey.RequestPermissionWrite:
                if(permissionMap.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    SSHKey sshKey = SSHKey.getInstance(this);
                    sshKey.exportPublicKeyToFile();
                }
                break;
        }
    }

}
