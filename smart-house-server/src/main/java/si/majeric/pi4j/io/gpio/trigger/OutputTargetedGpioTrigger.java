package si.majeric.pi4j.io.gpio.trigger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.trigger.GpioTriggerBase;

public abstract class OutputTargetedGpioTrigger extends GpioTriggerBase {
	private static final Logger logger = LoggerFactory.getLogger(OutputTargetedGpioTrigger.class);

	protected final GpioPinDigitalOutput targetPin;

	public OutputTargetedGpioTrigger(GpioPinDigitalOutput targetPin) {
		super();
		this.targetPin = targetPin;
	}

	public OutputTargetedGpioTrigger(PinState state, GpioPinDigitalOutput targetPin) {
		super(state);
		this.targetPin = targetPin;
	}

	public OutputTargetedGpioTrigger(PinState[] states, GpioPinDigitalOutput targetPin) {
		super(states);
		this.targetPin = targetPin;
	}

	public OutputTargetedGpioTrigger(List<PinState> states, GpioPinDigitalOutput targetPin) {
		super(states);
		this.targetPin = targetPin;
	}

	public GpioPinDigitalOutput getTargetPin() {
		return targetPin;
	}

	@Override
	public void invoke(GpioPin pin, PinState state) {
		String input = "smart client only";
		if (pin != null) {
			input = pin.getProvider() + "/" + pin.getPin().getName();
		}
		logger.info("Trigger invoked on " + input + " pin for target pin " + targetPin.getProvider() + "/" + targetPin.getPin());
	}
}
