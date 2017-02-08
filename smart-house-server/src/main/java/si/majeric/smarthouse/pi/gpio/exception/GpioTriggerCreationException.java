package si.majeric.smarthouse.pi.gpio.exception;

import si.majeric.smarthouse.model.TriggerConfig.GpioTriggerType;

import com.pi4j.io.gpio.GpioPinDigitalOutput;

public class GpioTriggerCreationException extends Exception {
	private static final long serialVersionUID = 1L;

	public GpioTriggerCreationException(String field, GpioTriggerType type, GpioPinDigitalOutput targetPin) {
		super("'" + field + "' config required for " + type.toString() + ". PIN '" + targetPin.getName() + "' will not be initialized.");
	}

}
