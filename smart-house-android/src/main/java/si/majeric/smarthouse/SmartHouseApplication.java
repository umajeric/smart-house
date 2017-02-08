package si.majeric.smarthouse;

import si.majeric.smarthouse.client.Client;
import android.app.Application;

import com.onesignal.OneSignal;

public class SmartHouseApplication extends Application {
	public static final String TRIGGER_ID_KEY = "TRIGGER_ID";
	public static final String TRIGGER_TITLE_KEY = "TRIGGER_TITLE";
	
	private Client _client;

	@Override
	public void onCreate() {
		super.onCreate();
		_client = new AndroidClient(this);
		_client.init();
		OneSignal.startInit(this).init();
		// PushLink.start(this, R.drawable.ic_launcher, "e0bi23hfkku791el", AndroidDeviceID.getDeviceID(this));
		//
		// PushLink.addMetadata("Brand", Build.BRAND);
		// PushLink.addMetadata("Model", Build.MODEL);
		// PushLink.addMetadata("OS Version", Build.VERSION.RELEASE);
		// PushLink.addMetadata("Logged in user", "Test user");
	}

	public Client getClient() {
		return _client;
	}
}
