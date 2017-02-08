package si.majeric.smarthouse.wear.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import si.majeric.android.ui.TriToggleButton;
import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.wear.MainActivity;
import si.majeric.smarthouse.wear.R;
import si.majeric.smarthouse.wear.SmartHouseApplication;

/**
 * Created by Uros Majeric on 11/01/16.
 */
public class ConfigFragment extends Fragment {
	private static final Logger logger = LoggerFactory.getLogger(ConfigFragment.class);
	public interface ConfigLoadedListener {
		void configLoaded();
	}

	private ConfigLoadedListener _configLoadedListener;

	public ConfigFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.round_config, container, false);

		Button downloadConfigButton = (Button) rootView.findViewById(R.id.downloadConfigButton);
		downloadConfigButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				new Thread() {
					@Override
					public void run() {
						try {
							final Client client = ((SmartHouseApplication) getActivity().getApplication()).getClient();
							client.downloadHouseConfig();
							if (_configLoadedListener != null) {
								_configLoadedListener.configLoaded();
							}
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(getActivity(), "Config loaded", Toast.LENGTH_LONG).show();
								}
							});
						} catch (Exception e) {
							logger.error(e.getLocalizedMessage(), e);
						}
					}
				}.start();
			}
		});
		return rootView;
	}

	public void setConfigLoadedListener(ConfigLoadedListener configLoadedListener) {
		_configLoadedListener = configLoadedListener;
	}
}
