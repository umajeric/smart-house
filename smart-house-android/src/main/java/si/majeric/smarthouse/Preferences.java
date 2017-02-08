package si.majeric.smarthouse;

import java.util.Arrays;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.InputType;

public class Preferences extends PreferenceActivity {
	public static final String HOST_KEY = "host";
	public static final String PORT_KEY = "port";
	public static final String USERNAME_KEY = "sh_username";
	public static final String PASSWORD_KEY = "sh_password";
	public static final String HOME_SSID_KEY = "sh_home_wifi";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		initPreference(Preferences.HOST_KEY);
		initPreference(Preferences.PORT_KEY);
		initPreference(Preferences.USERNAME_KEY);
		initPreference(Preferences.PASSWORD_KEY);
		initPreference(Preferences.HOME_SSID_KEY);
	}

	protected Preference initPreference(String prefName) {
		final Preference pref = findPreference(prefName);
		if (pref instanceof EditTextPreference) {
			initPreference((EditTextPreference) pref);
		} else if (pref instanceof CheckBoxPreference) {
			initPreference((CheckBoxPreference) pref);
		} else if (pref instanceof ListPreference) {
			initPreference((ListPreference) pref);
		} else {
			initPreference(pref);
		}
		return pref;
	}

	protected void initPreference(final Preference pref) {
		if (pref == null) {
			return;
		}
		pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if (newValue != null) {
					preference.setSummary(newValue.toString());
				}
				return true;
			}
		});

		final String text = pref.toString();
		if (text != null && !"".equals(text.trim())) {
			pref.setSummary(text);
		}
	}

	protected void initPreference(final EditTextPreference pref) {
		final String passwordSet = "Password set";
		final int inputType = pref.getEditText().getInputType();
		pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) > 0) {
					if (newValue != null && !"".equals(newValue)) {
						preference.setSummary(passwordSet);
					} else {
						preference.setSummary("Enter password");
					}
				} else if (newValue != null) {
					preference.setSummary(newValue.toString());
				}
				return true;
			}
		});
		final String text = pref.getText();
		if (text != null && !"".equals(text.trim())) {
			if ((inputType & InputType.TYPE_TEXT_VARIATION_PASSWORD) > 0) {
				pref.setSummary(passwordSet);
			} else {
				pref.setSummary(text);
			}
		}
	}

	protected void initPreference(final CheckBoxPreference pref) {
		pref.setOnPreferenceChangeListener(new CheckBoxPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String newVal = Preferences.this.getString(pref.isChecked() ? android.R.string.yes : android.R.string.no);
				if (newVal != null) {
					preference.setSummary(newVal);
				}
				return true;
			}
		});
		String prefValue = this.getString(pref.isChecked() ? android.R.string.yes : android.R.string.no);
		if (prefValue != null && !"".equals(prefValue)) {
			pref.setSummary("Current value: " + prefValue);
		}
	}

	protected void initPreference(final ListPreference pref) {
		pref.setOnPreferenceChangeListener(new ListPreference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				CharSequence newVal = getValueFor(pref, newValue);
				if (newVal != null) {
					preference.setSummary(newVal);
				}
				return true;
			}
		});
		String prefValue = pref.getValue();
		if (prefValue != null && !"".equals(prefValue)) {
			pref.setSummary(pref.getEntry());
		}
	}

	private CharSequence getValueFor(final ListPreference preference, Object value) {
		int indexOf = Arrays.asList(preference.getEntryValues()).indexOf(value);
		if (indexOf > -1) {
			return preference.getEntries()[indexOf];
		}
		return null;
	}
}