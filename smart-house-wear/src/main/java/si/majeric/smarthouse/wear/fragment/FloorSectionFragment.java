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
public class FloorSectionFragment extends Fragment {

	public static final String ARG_SECTION_NUMBER = "section_number";
	private Client _client;

	public FloorSectionFragment() {
	}

	public void setClient(Client client) {
		this._client = client;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_section_floor, container, false);

		final ViewGroup roomListLayout = (ViewGroup) rootView.findViewById(R.id.roomListLayout);
		Bundle args = getArguments();
		int floorIndex = args.getInt(ARG_SECTION_NUMBER);
		final List<Floor> floors = _client.getHouse().getConfiguration().getFloors();
		if (floorIndex > floors.size()) {
			return rootView;
		}
		Floor floor = floors.get(floorIndex - 1);

		ArrayList<Room> rooms = new ArrayList<>(floor.getRooms());

		for (Room room : rooms) {
			final LinearLayout linearLayout = new LinearLayout(container.getContext());
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			final TextView textView = new TextView(container.getContext());
			textView.setText(room.getName());
			linearLayout.addView(textView);

			final LinearLayout switchesLayout = new LinearLayout(container.getContext());
			switchesLayout.setOrientation(LinearLayout.HORIZONTAL);

			for (Switch swtch : room.getSwitches()) {
				final View switchView = getChildView(container.getContext(), swtch);
				switchesLayout.addView(switchView);
			}
			linearLayout.addView(switchesLayout);

			roomListLayout.addView(linearLayout);
		}
		return rootView;
	}

	private void invokeTrigger(final TriggerConfig triggerConfig) {
		if (getActivity() instanceof MainActivity) {
			_client.invokeStateChange(triggerConfig, (MainActivity) getActivity());
		}
	}

//		public void notifyDataSetChanged() {
//			if (_roomList != null && _roomList.getExpandableListAdapter() != null
//					&& _roomList.getExpandableListAdapter() instanceof BaseExpandableListAdapter) {
//				BaseExpandableListAdapter adapter = (BaseExpandableListAdapter) _roomList.getExpandableListAdapter();
//				adapter.notifyDataSetChanged();
//			}
//		}

	public View getChildView(final Context context, final Switch swtch) {
		LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View convertView = infalInflater.inflate(R.layout.exp_list_child_layout, null);

		Button upButton = (Button) convertView.findViewById(R.id.upButton);
		Button downButton = (Button) convertView.findViewById(R.id.downButton);
		TriToggleButton sw = (TriToggleButton) convertView.findViewById(R.id.triToggleButton);

		final View.OnClickListener onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (swtch.getTriggers() == null || swtch.getTriggers().isEmpty()) {
					Toast.makeText(context, "No triggers set on " + swtch.getName(), Toast.LENGTH_LONG).show();
					return;
				}
				final List<TriggerConfig> triggers = swtch.getTriggers();
				if (triggers.size() == 1) {
				/* 1. If there is only one trigger invoke it */
					invokeTrigger(triggers.get(0));
				} else {
				/* 2. If there is more then one trigger and there is the default trigger then invoke this one */
					for (TriggerConfig tc : triggers) {
						if (tc != null && tc.isDefault()) {
							invokeTrigger(tc);
							return;
						}
					}
				/* 3. If there is more then one trigger and there is no default trigger then show trigger selection dialog */
//						showTriggerSelectionDialog(triggers);
				}
			}
		};

	/* 4. Add on long click trigger selection dialog */
		final View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
//					showTriggerSelectionDialog(swtch.getTriggers());
				return true;
			}
		};

		if (swtch.getType() != null) {
			switch (swtch.getType()) {
				case SWITCH:
					upButton.setVisibility(View.GONE);
					downButton.setVisibility(View.GONE);
					sw.setVisibility(View.VISIBLE);
					sw.setOnClickListener(onClickListener);
					if (swtch.getState() == null) {
						sw.setState(-1);
					} else if (swtch.getState() == PinState.HIGH) {
						sw.setState(1);
					} else if (swtch.getState() == PinState.LOW) {
						sw.setState(0);
					}

					sw.setOnLongClickListener(onLongClickListener);
					// tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.car, 0, 0, 0);
					break;
				case UP:
					upButton.setVisibility(View.VISIBLE);
					downButton.setVisibility(View.GONE);
					sw.setVisibility(View.GONE);

					upButton.setOnClickListener(onClickListener);
					upButton.setOnLongClickListener(onLongClickListener);
					break;
				case DOWN:
					upButton.setVisibility(View.GONE);
					downButton.setVisibility(View.VISIBLE);
					sw.setVisibility(View.GONE);

					downButton.setOnClickListener(onClickListener);
					downButton.setOnLongClickListener(onLongClickListener);
					// tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.car, 0, 0, 0);
					break;
				default:
			}
		} else {
			upButton.setVisibility(View.GONE);
			downButton.setVisibility(View.GONE);
			sw.setVisibility(View.GONE);
		}
		return convertView;
	}
}
