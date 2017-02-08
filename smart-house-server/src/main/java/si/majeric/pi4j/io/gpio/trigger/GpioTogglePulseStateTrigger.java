package si.majeric.pi4j.io.gpio.trigger;


/**
 *
 * @author Uros Majeric
 */
import java.util.List;
import java.util.concurrent.Future;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

public class GpioTogglePulseStateTrigger extends OutputTargetedGpioTrigger {

	private final long milliseconds;
	private Future<?> _lastPulse;

	public GpioTogglePulseStateTrigger(GpioPinDigitalOutput targetPin, long milliseconds) {
		super(targetPin);
		this.milliseconds = milliseconds;
	}

	public GpioTogglePulseStateTrigger(PinState state, GpioPinDigitalOutput targetPin, long milliseconds) {
		super(state, targetPin);
		this.milliseconds = milliseconds;
	}

	public GpioTogglePulseStateTrigger(PinState[] states, GpioPinDigitalOutput targetPin, long milliseconds) {
		super(states, targetPin);
		this.milliseconds = milliseconds;
	}

	public GpioTogglePulseStateTrigger(List<PinState> states, GpioPinDigitalOutput targetPin, long milliseconds) {
		super(states, targetPin);
		this.milliseconds = milliseconds;
	}

	@Override
	public void invoke(GpioPin pin, PinState state) {
		super.invoke(pin, state);
		if (targetPin != null) {
			// If current state is HIGH and this trigger was invoked (pulse was started) and it was not executed yet - then set the pin state to LOW and cancel the execution
			if (PinState.HIGH.equals(targetPin.getState()) && (_lastPulse != null && !_lastPulse.isDone())) {
				_lastPulse.cancel(false);
				targetPin.setState(PinState.LOW);
			} else {
				_lastPulse = targetPin.pulse(milliseconds);
			}
		}
	}
}
