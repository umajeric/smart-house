package si.majeric.smarthouse.wear;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.text.InputType;

import java.util.Arrays;

public class Preferences extends PreferenceActivity {
	public static final String HOST_KEY = "host";
	public static final String PORT_KEY = "port";
	public static final String USERNAME_KEY = "sh_username";
	public static final String PASSWORD_KEY = "sh_password";
	public static final String HOME_SSID_KEY = "sh_home_wifi";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

}