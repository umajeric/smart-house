package si.majeric.smarthouse.wear;

import android.app.Application;

import si.majeric.smarthouse.client.Client;

/**
 * Created by Uros Majeric on 26/12/15.
 */
public class SmartHouseApplication extends Application {
	private Client _client;

	@Override
	public void onCreate() {
		super.onCreate();

		_client = new AndroidClient(this);
		_client.init();
	}

	public Client getClient() {
		return _client;
	}
}
