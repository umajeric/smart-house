package si.majeric.smarthouse.service;

import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Map;

import si.majeric.smarthouse.SmartHouseApplication;
import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.client.ResponseListener;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.TriggerConfig;

/**
 * Created by Uros Majeric on 12/01/16.
 */
public class WearListenerService extends WearableListenerService implements ResponseListener {

	private Handler mHandler;

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		if ("/state/change".equals(messageEvent.getPath())) {
			final String triggerId = new String(messageEvent.getData());
			TriggerConfig triggerConfig = new TriggerConfig();
			triggerConfig.setId(triggerId);
			Client client = ((SmartHouseApplication) getApplication()).getClient();
			client.invokeStateChange(triggerConfig, this);
		}
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