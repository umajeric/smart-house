package si.majeric.smarthouse.wear;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.client.ResponseListener;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.wear.dao.PreferencesSHDaoFactory;

public class AndroidClient extends Client implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
	private static final Logger logger = LoggerFactory.getLogger(AndroidClient.class);
	private static final Long CONNECTION_TIME_OUT_MS = 100l;

	private final Context _context;
	private Exception _exception; /* WARNING not thread safe */
	private GoogleApiClient _gApiClient;

	public AndroidClient(Context context) {
		super(new PreferencesSHDaoFactory(context));
		_context = context;
	}

	@Override
	public void init() {
		super.init();
		initApiClient();
	}

	private void initApiClient() {
		_gApiClient = new GoogleApiClient.Builder(_context)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Wearable.API)
				.build();
		_gApiClient.connect();
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

	@Override
	public void downloadHouseConfig() throws Exception {
		super.downloadHouseConfig();
	}

	public void invokeStateChange(final TriggerConfig triggerConfig, final ResponseListener listener) {
		if (_gApiClient.isConnected()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(_gApiClient).await();
					for (Node node : nodes.getNodes()) {
						MessageApi.SendMessageResult result =
								Wearable.MessageApi.sendMessage(_gApiClient, node.getId(), "/state/change", triggerConfig.getId().getBytes()).await();
						if (!result.getStatus().isSuccess()) {
							listener.errorChangingState(new RuntimeException(result.getStatus().getStatusMessage()));
						} else {
							listener.stateChanged(null);
						}
						return;
					}
					// if no node was found try to send over wifi
					// if not connected try invoking state change over wifi
					AndroidClient.super.invokeStateChange(triggerConfig, listener);
				}
			}).start();

		} else {
			// if not connected try invoking state change over wifi
			super.invokeStateChange(triggerConfig, listener);
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		Toast.makeText(_context, "onConnected: " + connectionHint, Toast.LENGTH_LONG).show();
		logger.info("onConnected: " + connectionHint);
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Toast.makeText(_context, "onConnectionSuspended: " + cause, Toast.LENGTH_LONG).show();
		logger.info("onConnectionSuspended: " + cause);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Toast.makeText(_context, "onConnectionFailed: " + result, Toast.LENGTH_LONG).show();
		logger.info("onConnectionFailed: " + result);
	}
}
