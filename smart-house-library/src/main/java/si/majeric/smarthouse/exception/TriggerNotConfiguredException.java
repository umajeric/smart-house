package si.majeric.smarthouse.exception;

public class TriggerNotConfiguredException extends Exception {
	private static final long serialVersionUID = 1L;

	public TriggerNotConfiguredException(String triggerId) {
		super("Trigger with id '" + triggerId + "' not configured.");
	}

}
