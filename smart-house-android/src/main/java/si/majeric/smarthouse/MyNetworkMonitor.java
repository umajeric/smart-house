package si.majeric.smarthouse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.widget.Toast;

import si.majeric.smarthouse.alarm.Alarm;

/**
 * Created by Uros Majeric on 11/08/15.
 */
public class MyNetworkMonitor extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null && WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
			NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (ConnectivityManager.TYPE_WIFI == netInfo.getType()) {
				boolean connected = netInfo.isConnected();

				if (connected) {
					WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					WifiInfo info = wifiManager.getConnectionInfo();
					String ssid = info.getSSID();
					if (ssid != null) {
						if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
							ssid = ssid.substring(1, ssid.length() - 1);
						}

						SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
						String homeSSID = prefs.getString(Preferences.HOME_SSID_KEY, null);

						if (ssid.equals(homeSSID)) {
							IntentReceiver.triggerAlarmNotification(context, "You are now at home", Alarm.State.OFF, true);
						}
					}
				}
			}
		}
	}
}