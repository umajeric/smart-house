package si.majeric.android;

/**
 * 
 * Callback interface through which the fragment will report the task's progress and results back to the Activity.
 * 
 * @author uros
 */
public interface TaskCallbacks<T> {
	void onPreExecute();

	void onProgressUpdate(int percent);

	void onCancelled();

	void onPostExecute(T result);
}
