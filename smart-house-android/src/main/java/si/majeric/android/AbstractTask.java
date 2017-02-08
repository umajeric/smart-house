package si.majeric.android;

import android.os.AsyncTask;

/**
 * A dummy task that performs some (dumb) background work and proxies progress updates and results back to the Activity.
 * 
 * Note that we need to check if the callbacks are null in each method in case they are invoked after the Activity's and Fragment's onDestroy() method have
 * been called.
 */
public abstract class AbstractTask<T> extends AsyncTask<T, Integer, T> {
	private final TaskCallbacksProvider<T> _cbProvider;

	public AbstractTask(TaskCallbacksProvider<T> cbProvider) {
		_cbProvider = cbProvider;
	}

	@Override
	protected void onPreExecute() {
		if (_cbProvider.getTaskCallbacks() != null) {
			_cbProvider.getTaskCallbacks().onPreExecute();
		}
	}

	/**
	 * Note that we do NOT call the callback object's methods directly from the background thread, as this could result in a race condition.
	 */
	@Override
	protected abstract T doInBackground(T... ignore);

	@Override
	protected void onProgressUpdate(Integer... percent) {
		if (_cbProvider.getTaskCallbacks() != null) {
			_cbProvider.getTaskCallbacks().onProgressUpdate(percent[0]);
		}
	}

	@Override
	protected void onCancelled() {
		if (_cbProvider.getTaskCallbacks() != null) {
			_cbProvider.getTaskCallbacks().onCancelled();
		}
	}

	@Override
	protected void onPostExecute(T result) {
		if (_cbProvider.getTaskCallbacks() != null) {
			_cbProvider.getTaskCallbacks().onPostExecute(result);
		}
	}
}