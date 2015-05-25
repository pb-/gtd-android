package pb.gtd.ui;

import pb.gtd.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.settings);
		setSummaries();
	}

	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		setSummaries();
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	private void setSummaries() {
		String keys[] = { "server", "username", "password", "passphrase" };

		SharedPreferences sp = getPreferenceScreen().getSharedPreferences();

		for (String k : keys) {
			Preference connectionPref = findPreference(k);

			if (!k.equals("password") && !k.equals("passphrase")) {
				connectionPref.setSummary(sp.getString(k, ""));
			}
		}
	}
}
