package si.majeric.smarthouse.tpt;

import java.io.Serializable;

public class Response implements Serializable {
	private static final long serialVersionUID = 1L;

	private ResponseStatus _status = new ResponseStatus();
	private Request _request;
	private Object object;

	/**
	 * Needed by XStream.
	 */
	public Response() {
	}

	public ResponseStatus getStatus() {
		return _status;
	}

	public void setStatus(ResponseStatus status) {
		_status = status;
	}

	public Request getRequest() {
		return _request;
	}

	public void setRequest(Request request) {
		_request = request;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
}
