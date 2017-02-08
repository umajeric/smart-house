package si.majeric.smarthouse.pi.gpio.trigger;

import java.util.List;

import si.majeric.pi4j.io.gpio.trigger.GpioBlinkStateTrigger;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

/**
 * Trigger that is blinking only for a duration provided.
 * 
 * @author uros (Nov 24, 2013)
 * 
 */
public class GpioBlinkDurationStateTrigger extends GpioBlinkStateTrigger {
	private final long delay;
	private final long duration;

	public GpioBlinkDurationStateTrigger(List<PinState> states, GpioPinDigitalOutput targetPin, long delay, long duration) {
		super(states, targetPin, delay);
		this.delay = delay;
		this.duration = duration;
	}

	@Override
	public void invoke(GpioPin pin, PinState state) {
		if (getTargetPin() != null) {
			getTargetPin().blink(delay, duration);
		}
	}

}
