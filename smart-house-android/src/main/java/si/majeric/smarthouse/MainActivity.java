/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package si.majeric.smarthouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.android.AbstractTask;
import si.majeric.android.TaskCallbacks;
import si.majeric.android.TaskCallbacksProvider;
import si.majeric.smarthouse.alarm.Alarm;
import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.client.ResponseListener;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

/**
 * @author Uros Majeric
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener, TaskCallbacksProvider<String>, TaskCallbacks<String>,
		ResponseListener {
	private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
	private static final int RESULT_SETTINGS = 900;

	private Client _client;

	private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	private ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		_client = ((SmartHouseApplication) getApplication()).getClient();

		final Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String triggerId = extras.getString(SmartHouseApplication.TRIGGER_ID_KEY);
			String title = extras.getString(SmartHouseApplication.TRIGGER_TITLE_KEY);
			if (triggerId != null) {
				showTriggerDialog(triggerId, title);
			}
		}

		// Create the adapter that will return a fragment for each of the three primary sections
		// of the app.
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(), _client);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();

		// Specify that the Home/Up button should not be enabled, since there is no hierarchical parent.
		actionBar.setHomeButtonEnabled(false);

		// Specify that we will be displaying tabs in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD); // Uncomment actionBar.setSelectedNavigationItem if changed to TABS

		// Set up the ViewPager, attaching the adapter and setting up a listener for when the
		// user swipes between sections.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// When swiping between different app sections, select the corresponding tab.
				// We can also use ActionBar.Tab#select() to do this if we have a reference to the
				// Tab.
				// actionBar.setSelectedNavigationItem(position);
			}
		});

		reloadActionBar();

		if (_client.getHouse() == null) {
			startPreferencesActivity();
		} else {
			if (_client.getHouse().getConfiguration() == null) {
				new DownloadConfigAsyncTask(this).execute();
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		_client = ((SmartHouseApplication) getApplication()).getClient();
		new Thread(downloadStates).start();
		new Thread(getAlarmState).start();
	}

	private void showTriggerDialog(final String triggerId, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title).setMessage("What would you like to do?");
		builder.setPositiveButton("Trigger", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				getIntent().removeExtra(SmartHouseApplication.TRIGGER_ID_KEY);
				TriggerConfig triggerConfig = new TriggerConfig();
				triggerConfig.setId(triggerId);
				_client.invokeStateChange(triggerConfig, MainActivity.this);
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				getIntent().removeExtra(SmartHouseApplication.TRIGGER_ID_KEY);
			}
		});
		builder.create().show();
	}

	private void reloadActionBar() {
		// For each of the sections in the app, add a tab to the action bar.
		ActionBar actionBar = getActionBar();
		actionBar.removeAllTabs();
		for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by the adapter.
			// Also specify this Activity object, which implements the TabListener interface, as the
			// listener for when this tab is selected.
			actionBar.addTab(actionBar.newTab().setText(mAppSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

			case R.id.menu_settings:
				startPreferencesActivity();
				break;
			case R.id.menu_download_config:
				new DownloadConfigAsyncTask(this).execute();
				break;
			case R.id.menu_refresh_states:
				new Thread(getAlarmState).start();
				new Thread(downloadStates).start();
				break;
			case R.id.menu_download_messages:
				Intent i = new Intent(this, MessagesActivity.class);
				startActivity(i);
				break;
		}

		return true;
	}

	private void startPreferencesActivity() {
		Intent i = new Intent(this, Preferences.class);
		startActivityForResult(i, RESULT_SETTINGS);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case RESULT_SETTINGS:
				_client.init();
				break;
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary sections of the app.
	 */
	public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {
		Client _client;
		private final Map<Integer, FloorSectionFragment> _fragments = new HashMap<Integer, FloorSectionFragment>();

		public AppSectionsPagerAdapter(FragmentManager fm, Client client) {
			super(fm);
			_client = client;
		}

		@Override
		public Fragment getItem(int i) {
			FloorSectionFragment fragment = _fragments.get(i);
			/* if fragment already exists return existing one */
			if (fragment != null) {
				return fragment;
			}
			fragment = new FloorSectionFragment();
			Bundle args = new Bundle();
			args.putInt(FloorSectionFragment.ARG_SECTION_NUMBER, i + 1);
			fragment.setArguments(args);

			/* add a fragment to the map */
			_fragments.put(i, fragment);
			return fragment;
		}

		@Override
		public int getCount() {
			if (_client.getHouse() != null) {
				final Configuration configuration = _client.getHouse().getConfiguration();
				if (configuration != null && configuration.getFloors() != null) {
					return configuration.getFloors().size();
				}
			}
			return 0;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if (_client.getHouse() != null) {
				final Configuration configuration = _client.getHouse().getConfiguration();
				if (configuration != null && configuration.getFloors() != null) {
					return configuration.getFloors().get(position).getName();
				}
			}
			return "";
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
			for (FloorSectionFragment fragment : _fragments.values()) {
				if (fragment != null) {
					fragment.notifyDataSetChanged();
				}
			}
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply displays dummy text.
	 */
	public static class FloorSectionFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";
		private ExpandableListView _roomList;

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_section_floor, container, false);

			Client client = ((SmartHouseApplication) getActivity().getApplication()).getClient();

			Bundle args = getArguments();
			int floorIndex = args.getInt(ARG_SECTION_NUMBER);
			Floor floor = client.getHouse().getConfiguration().getFloors().get(floorIndex - 1);

			ArrayList<Room> rooms = new ArrayList<Room>(floor.getRooms());

			_roomList = (ExpandableListView) rootView.findViewById(R.id.roomListView);
			ArrayList<ArrayList<Switch>> switches = new ArrayList<ArrayList<Switch>>();
			for (Room room : rooms) {
				ArrayList<Switch> object = new ArrayList<Switch>(room.getSwitches());
				switches.add(object);
			}
			BaseExpandableListAdapter adapter = new ExpandableRoomsListAdapter(getActivity(), rooms, switches, client);
			_roomList.setAdapter(adapter);
			return rootView;
		}

		public void notifyDataSetChanged() {
			if (_roomList != null && _roomList.getExpandableListAdapter() != null
					&& _roomList.getExpandableListAdapter() instanceof BaseExpandableListAdapter) {
				BaseExpandableListAdapter adapter = (BaseExpandableListAdapter) _roomList.getExpandableListAdapter();
				adapter.notifyDataSetChanged();
			}
		}
	}

	private final class DownloadConfigAsyncTask extends AbstractTask<String> {

		public DownloadConfigAsyncTask(TaskCallbacksProvider<String> cbProvider) {
			super(cbProvider);
		}

		@Override
		protected String doInBackground(String... ignore) {
			try {
				_client.downloadHouseConfig();
			} catch (Exception e) {
				return e.getLocalizedMessage();
			}
			return null;
		}
	}

	Runnable downloadStates = new Runnable() {
		@Override
		public void run() {
			try {
				_client.downloadCurrentStates();
				updateConfigWithStates(_client.getHouse().getConfiguration(), _client.getCurrentStates());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mAppSectionsPagerAdapter.notifyDataSetChanged();
					}
				});

				_client.saveConfiguration();
			} catch (final Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						 Toast.makeText(getApplication(), "States not updated.", Toast.LENGTH_SHORT).show();
					}
				});
			}

		}
	};

	Runnable getAlarmState = new Runnable() {
		@Override
		public void run() {
			try {
				final Alarm.State alarmState = _client.getAlarmState();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO show alarm state somewhere
						Toast.makeText(getApplication(), "Alarm: " + alarmState.toString(), Toast.LENGTH_LONG).show();
					}
				});

				_client.saveConfiguration();
			} catch (final Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplication(), "Alarm state not updated: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
			}

		}
	};

	static ProgressDialog pd;

	@Override
	public void onPreExecute() {
		try {
			if (pd == null || !pd.isShowing()) {
				pd = new ProgressDialog(this);
				pd.setIndeterminate(true);
				pd.setMessage("Loading...");
				pd.show();
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void onPostExecute(String result) {
		try {
			if (pd != null && pd.isShowing()) {
				pd.dismiss();
			}

			if (result != null) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
				alertDialogBuilder.setTitle("Error has occured");

				// set dialog message
				alertDialogBuilder.setMessage(result).setNegativeButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			} else {
				_client.init();
				mAppSectionsPagerAdapter.notifyDataSetChanged();
				reloadActionBar();
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void onCancelled() {
	}

	@Override
	public void onProgressUpdate(int percent) {
	}

	@Override
	public TaskCallbacks<String> getTaskCallbacks() {
		return this;
	}

	public static void updateConfigWithStates(Configuration configuration, Map<String, PinState> currentStates) {
		if (currentStates != null) {
			List<Floor> floors = configuration.getFloors();
			for (Floor floor : floors) {
				for (Room room : floor.getRooms()) {
					for (Switch swtch : room.getSwitches()) {
						if (swtch != null && swtch.getId() != null && currentStates.containsKey(swtch.getId())) {
							PinState pinState = currentStates.get(swtch.getId());
							if (pinState != null) {
								swtch.setState(pinState);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void stateChanged(Map<String, PinState> swtch) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void errorChangingState(final Throwable t) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "Error has occured: " + t.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}
}
