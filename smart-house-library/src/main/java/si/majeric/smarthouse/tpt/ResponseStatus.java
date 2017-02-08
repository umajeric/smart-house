package si.majeric.smarthouse.tpt;

import java.io.Serializable;

public class ResponseStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean _succeeded = false;
	private String _message;

	/**
	 * Needed by XStream.
	 */
	public ResponseStatus() {
	}

	public boolean isSucceeded() {
		return _succeeded;
	}

	public void setSucceeded(boolean succeeded) {
		_succeeded = succeeded;
	}

	public String getMessage() {
		return _message;
	}

	public void setMessage(String message) {
		_message = message;
	}

}
