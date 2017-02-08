package si.majeric.smarthouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.android.ui.TriToggleButton;
import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.client.ResponseListener;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ExpandableRoomsListAdapter extends BaseExpandableListAdapter implements ResponseListener {
	static final Logger logger = LoggerFactory.getLogger(ExpandableRoomsListAdapter.class);

	private Activity _activity;
	private ArrayList<Room> _groups;
	private ArrayList<ArrayList<Switch>> _children;
	private Client _client;

	public ExpandableRoomsListAdapter(Activity context, ArrayList<Room> groups, ArrayList<ArrayList<Switch>> children, Client client) {
		_activity = context;
		_groups = groups;
		_children = children;
		_client = client;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return _children.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		final Switch swtch = (Switch) getChild(groupPosition, childPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.exp_list_child_layout, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.tvChild);
		tv.setText("   " + swtch.getName());

		Button upButton = (Button) convertView.findViewById(R.id.upButton);
		Button downButton = (Button) convertView.findViewById(R.id.downButton);
		TriToggleButton sw = (TriToggleButton) convertView.findViewById(R.id.triToggleButton);

		final OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (swtch.getTriggers() == null || swtch.getTriggers().isEmpty()) {
					Toast.makeText(_activity, "No triggers set on " + swtch.getName(), Toast.LENGTH_LONG).show();
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
					showTriggerSelectionDialog(triggers);
				}
			}
		};

		/* 4. Add on long click trigger selection dialog */
		final OnLongClickListener onLongClickListener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				showTriggerSelectionDialog(swtch.getTriggers());
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

	@Override
	public int getChildrenCount(int groupPosition) {
		return _children.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return _groups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return _groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Room room = (Room) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.exp_list_group_layout, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
		tv.setText(room.getName());
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	private void invokeTrigger(final TriggerConfig triggerConfig) {
		_client.invokeStateChange(triggerConfig, this);
	}

	private void showTriggerSelectionDialog(final List<TriggerConfig> triggers) {
		if (triggers != null && !triggers.isEmpty()) {
			List<String> itemsList = new ArrayList<String>();
			for (TriggerConfig tc : triggers) {
				itemsList.add(tc.getName() != null ? tc.getName() : tc.getId());
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
			// builder.setTitle("Pick a color");
			builder.setItems(itemsList.toArray(new String[] {}), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					invokeTrigger(triggers.get(item));

				}
			});
			builder.setTitle("Select trigger");
			builder.setInverseBackgroundForced(true);
			builder.setNegativeButton("Close", null);
			AlertDialog dialog = builder.create();
			dialog.show();
		} else {
			Toast.makeText(_activity, "No triggers to show...", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void stateChanged(final Map<String, PinState> statesMap) {
		MainActivity.updateConfigWithStates(_client.getHouse().getConfiguration(), statesMap);
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (statesMap != null) {
					ExpandableRoomsListAdapter.this.notifyDataSetChanged();
					logger.debug("Returned states {}", statesMap);
				}
			}
		});
	}

	@Override
	public void errorChangingState(final Throwable t) {
		_activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (t instanceof TimeoutException) {
					Toast.makeText(_activity.getApplicationContext(), "Waiting to long to get the response.", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(_activity.getApplicationContext(), "An error uccured: " + t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}