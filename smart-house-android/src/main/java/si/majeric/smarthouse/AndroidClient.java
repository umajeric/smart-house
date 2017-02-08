package si.majeric.smarthouse;

import si.majeric.smarthouse.android.dao.PreferencesSHDaoFactory;
import si.majeric.smarthouse.client.Client;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AndroidClient extends Client {
	private final Context _context;
	private Exception _exception; /* WARNING not thread safe */

	public AndroidClient(Context context) {
		super(new PreferencesSHDaoFactory(context));
		_context = context;
	}

	@Override
	protected String getHost() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
		return prefs.getString(Preferences.HOST_KEY, null);
	}

	@Override
	protected int getPort() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
		return prefs.getInt(Preferences.PORT_KEY, -1);
	}

	@Override
	protected String getUsername() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
		return prefs.getString(Preferences.USERNAME_KEY, null);
	}

	@Override
	protected String getPassword() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
		return prefs.getString(Preferences.PASSWORD_KEY, null);
	}

}