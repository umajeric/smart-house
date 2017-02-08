package si.majeric.android;

/**
 * 
 * Callback interface through which the fragment will report the task's progress and results back to the Activity.
 * 
 * @author uros
 */
public interface TaskCallbacksProvider<T> {
	TaskCallbacks<T> getTaskCallbacks();
}
