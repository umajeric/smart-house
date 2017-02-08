package si.majeric.smarthouse;

import si.majeric.android.AbstractTask;
import si.majeric.android.TaskCallbacks;
import si.majeric.android.TaskCallbacksProvider;
import si.majeric.smarthouse.client.Client;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

/**
 * This Fragment manages a single background task and retains itself across configuration changes.
 * 
 * @author uros
 * 
 */
public class MessagesFragment extends Fragment implements TaskCallbacksProvider<String> {
	private TaskCallbacks<String> _callbacks;

	/**
	 * Hold a reference to the parent Activity so we can report the task's current progress and results. The Android framework will pass us a reference to the
	 * newly created Activity after each configuration change.
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			_callbacks = (TaskCallbacks<String>) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.getClass().getSimpleName() + " must implement " + TaskCallbacks.class.getSimpleName());
		}
	}

	@Override
	public TaskCallbacks<String> getTaskCallbacks() {
		return _callbacks;
	}

	/**
	 * This method will only be called once when the retained Fragment is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retain this fragment across configuration changes.
		setRetainInstance(true);

		startMessagesDownload();
	}

	public void startMessagesDownload() {
		// Create and execute the background task.
		AbstractTask<String> mTask = new AbstractTask<String>(this) {
			@Override
			protected String doInBackground(String... ignore) {
				try {
					Client client = ((SmartHouseApplication) getActivity().getApplication()).getClient();
					client.downloadMessages();
				} catch (Exception e) {
					String message = e.getMessage();
					return message;
				}
				return null;
			}
		};
		mTask.execute();
	}

	/**
	 * Set the callback to null so we don't accidentally leak the Activity instance.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		_callbacks = null;
	}
}