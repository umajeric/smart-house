package si.majeric.smarthouse.wear;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.client.ResponseListener;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.wear.fragment.ConfigFragment;
import si.majeric.smarthouse.wear.fragment.FloorSectionFragment;

public class MainActivity extends Activity implements ResponseListener {
	private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
	private static final Long CONNECTION_TIME_OUT_MS = 100l;

	private Client _client;


	private DotsPageIndicator mPageIndicator;
	private GridViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
		_client = ((SmartHouseApplication) getApplication()).getClient();

		stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
			@Override
			public void onLayoutInflated(WatchViewStub stub) {
				// Get UI references
				mPageIndicator = (DotsPageIndicator) stub.findViewById(R.id.page_indicator);
				mViewPager = (GridViewPager) stub.findViewById(R.id.pager);

				// Assigns an adapter to provide the content for this pager
				final FloorGridPagerAdapter floorGridPagerAdapter = new FloorGridPagerAdapter(getFragmentManager(), _client);
				mViewPager.setAdapter(floorGridPagerAdapter);
				mPageIndicator.setPager(mViewPager);
			}
		});
	}

	@Override
	public void stateChanged(Map<String, PinState> swtch) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mViewPager.getAdapter().notifyDataSetChanged();
			}
		});

	}

	@Override
	public void errorChangingState(final Throwable t) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (t instanceof TimeoutException) {
					Toast.makeText(getApplicationContext(), "Waiting to long to get the response.", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "An error uccured: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
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
						mViewPager.getAdapter().notifyDataSetChanged();
//						mAppSectionsPagerAdapter.notifyDataSetChanged();
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
	protected void onStart() {
		super.onStart();
		_client = ((SmartHouseApplication) getApplication()).getClient();
		new Thread(downloadStates).start();
	}

	private final class FloorGridPagerAdapter extends FragmentGridPagerAdapter implements ConfigFragment.ConfigLoadedListener {
		private Client _client;
		private final Map<Integer, Fragment> _fragments = new HashMap<>();

		private FloorGridPagerAdapter(FragmentManager fm, Client client) {
			super(fm);
			_client = client;
		}

		@Override
		public Fragment getFragment(int row, int column) {
			Fragment fragment = _fragments.get(column);
			/* if fragment already exists return existing one */
			if (fragment != null) {
				return fragment;
			}
			if (column < getColumnCount(row) - 1) {
				fragment = new FloorSectionFragment();
				((FloorSectionFragment) fragment).setClient(_client);
			} else {
				fragment = new ConfigFragment();
				((ConfigFragment) fragment).setConfigLoadedListener(this);
			}
			Bundle args = new Bundle();
			args.putInt(FloorSectionFragment.ARG_SECTION_NUMBER, column + 1);
			fragment.setArguments(args);

			/* add a fragment to the map */
			_fragments.put(column, fragment);
			return fragment;
		}

		@Override
		public int getRowCount() {
			return 1;
		}

		@Override
		public int getColumnCount(int row) {
			int count = 0;
			if (_client.getHouse() != null) {
				final Configuration configuration = _client.getHouse().getConfiguration();
				if (configuration != null && configuration.getFloors() != null) {
					count = configuration.getFloors().size();
				}
			}
			count++;
			return count;
		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
//			for (Fragment f : _fragments.values()) {
//				f.getView().invalidate();
//			}
		}

		public void reset() {
			_fragments.clear();
		}

		@Override
		public void configLoaded() {
			reset();
			notifyDataSetChanged();
		}
	}

}
