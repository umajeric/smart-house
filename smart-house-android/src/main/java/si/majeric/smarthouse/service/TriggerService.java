package si.majeric.smarthouse.service;

import java.util.Map;

import si.majeric.smarthouse.SmartHouseApplication;
import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.client.ResponseListener;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.TriggerConfig;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

/**
 * 
 * @author Uros Majeric
 *
 */
public class TriggerService extends IntentService implements ResponseListener {
	public static final int NOTIFICATION_ID = 001;
	public static final String ACTION_TRIGGER = "si.majeric.smarthouse.ACTION_SNOOZE";
	public static final String ACTION_DISMISS = "si.majeric.smarthouse.ACTION_DISMISS";

	private Handler mHandler;

	public TriggerService() {
		// The super call is required. The background thread that IntentService starts is labeled with the string argument you pass.
		super("si.majeric.smarthouse");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String triggerId = intent.getStringExtra(SmartHouseApplication.TRIGGER_ID_KEY);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		String action = intent.getAction();
		if (action.equals(TriggerService.ACTION_TRIGGER)) {
			nm.cancel(TriggerService.NOTIFICATION_ID);
			TriggerConfig triggerConfig = new TriggerConfig();
			triggerConfig.setId(triggerId);
			Client client = ((SmartHouseApplication) getApplication()).getClient();
			client.invokeStateChange(triggerConfig, this);
		} else if (action.equals(TriggerService.ACTION_DISMISS)) {
			nm.cancel(TriggerService.NOTIFICATION_ID);
		}
		stopSelf();
	}

	@Override
	public void stateChanged(Map<String, PinState> swtch) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void errorChangingState(final Throwable t) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "Error has occured: " + t.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}
}
