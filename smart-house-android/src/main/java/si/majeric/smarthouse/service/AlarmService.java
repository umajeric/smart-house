package si.majeric.smarthouse.service;

import java.util.Map;

import si.majeric.smarthouse.SmartHouseApplication;
import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.client.ResponseListener;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.tpt.Request;
import si.majeric.smarthouse.tpt.Request.RequestType;
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
public class AlarmService extends IntentService implements ResponseListener {
	public static final int NOTIFICATION_ID = 002;
	public static final String ACTION_TURN_ON = "si.majeric.smarthouse.ACTION_TURN_ON";
	public static final String ACTION_TURN_OFF = "si.majeric.smarthouse.ACTION_TURN_OFF";
	public static final String ACTION_UNLOCK = "si.majeric.smarthouse.ACTION_UNLOCK";
	public static final String ACTION_OK = "si.majeric.smarthouse.alarm.ACTION_OK";

	private Handler mHandler;

	public AlarmService() {
		// The super call is required. The background thread that IntentService starts is labeled with the string argument you pass.
		super("si.majeric.smarthouse.alarm");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		String action = intent.getAction();
		Client client = ((SmartHouseApplication) getApplication()).getClient();
		if (action.equals(AlarmService.ACTION_TURN_OFF)) {
			final Request request = new Request(RequestType.AlarmOff);
			request.addProperty(Request.SNOOZE_MINUTES, String.valueOf(30));
			client.invokeRequest(request, this);
		}
		if (action.equals(AlarmService.ACTION_TURN_ON)) {
			final Request request = new Request(RequestType.AlarmOn);
			request.addProperty(Request.SNOOZE_MINUTES, String.valueOf(30));
			client.invokeRequest(request, this);
		} else if (action.equals(AlarmService.ACTION_UNLOCK)) {
			// TODO be smarter here to get the trigger ID
			client.invokeStateChange(new TriggerConfig("VhodVrataOdpri_vrata"), this);
		} else if (action.equals(AlarmService.ACTION_OK)) {
		}
		nm.cancel(AlarmService.NOTIFICATION_ID);
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
