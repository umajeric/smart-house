package si.majeric.smarthouse.events;

import si.majeric.smarthouse.model.PinState;

/**
 * 
 * @author Uros Majeric
 * 
 */
public class SwitchStateChangeEvent {
	private final Integer _providerAddress;
	private final String _pin;
	private final PinState _newState;

	public SwitchStateChangeEvent(Integer providerAddress, String pin, PinState newState) {
		_providerAddress = providerAddress;
		_pin = pin;
		_newState = newState;
	}

	public Integer getProviderAddress() {
		return _providerAddress;
	}

	public String getPin() {
		return _pin;
	}

	public PinState getNewState() {
		return _newState;
	}

	@Override
	public String toString() {
		return SwitchStateChangeEvent.class.getSimpleName() + "(" + _providerAddress + ";" + _pin + "=" + _newState + ")";
	}
}