package si.majeric.smarthouse.tpt;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;

public class Request implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String ALARM_STATE = "alarmState";
	public static final String SNOOZE_MINUTES = "snoozeMinutes";

	public static enum RequestType {
		/**
		 * Loads entire configuration from the server
		 */
		GetConfiguration,
		/**
		 * Gets the current states for all switches
		 */
		GetStates,
		/**
		 * Gets the current configuration version (lightweight alternative of GetConfiguration)
		 */
		GetVersion,
		/**
		 * Get log messages from the server
		 */
		GetMessages,
		/**
		 * Invokes the trigger event.
		 */
		Invoke,
		/**
		 * Turn alarm on
		 */
		AlarmOn,
		/**
		 * Turn alarm off
		 */
		AlarmOff,
		/**
		 * Get alarm state
		 */
		GetAlarmState,
		/**
		 * Get Image Resource
		 */
		GetImageResource
	}

	/**
	 * Needed by XStream.
	 */
	public Request() {
	}

	public Request(RequestType type) {
		_type = type;
	}

	private RequestType _type;
	private TriggerConfig _triggerConfig;
	private Switch _switch;
	private Map<String, String> _properties = new HashMap<String, String>();

	public RequestType getType() {
		return _type;
	}

	public void setType(RequestType type) {
		_type = type;
	}

	public TriggerConfig getTriggerConfig() {
		return _triggerConfig;
	}

	public void setTrigger(TriggerConfig triggerConfig) {
		_triggerConfig = triggerConfig;
	}

	public Switch getSwitch() {
		return _switch;
	}

	public void setSwitch(Switch swtch) {
		_switch = swtch;
	}

	public Map<String, String> getProperties() {
		return _properties;
	}

	public void setProperties(Map<String, String> properties) {
		_properties = properties;
	}
	
	public void addProperty(String key, String value) {
		_properties.put(key, value);
	}
}
