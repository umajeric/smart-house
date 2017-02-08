package si.majeric.smarthouse.pi.gpio;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.pi4j.io.gpio.trigger.GpioBlinkStopStateTrigger;
import si.majeric.pi4j.io.gpio.trigger.GpioInverseSyncStateTrigger;
import si.majeric.pi4j.io.gpio.trigger.GpioPulseStateTrigger;
import si.majeric.pi4j.io.gpio.trigger.GpioPushTrigger;
import si.majeric.pi4j.io.gpio.trigger.GpioSetStateTrigger;
import si.majeric.pi4j.io.gpio.trigger.GpioSoundTrigger;
import si.majeric.pi4j.io.gpio.trigger.GpioSyncStateTrigger;
import si.majeric.pi4j.io.gpio.trigger.GpioTogglePulseStateTrigger;
import si.majeric.pi4j.io.gpio.trigger.GpioToggleStateTrigger;
import si.majeric.pi4j.io.gpio.trigger.OutputTargetedGpioTrigger;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.model.TriggerConfig.GpioTriggerType;
import si.majeric.smarthouse.pi.gpio.exception.GpioTriggerCreationException;
import si.majeric.smarthouse.pi.gpio.trigger.GpioBlinkDurationStateTrigger;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

public class GpioTriggerFactory {
	private static final Logger logger = LoggerFactory.getLogger(GpioTriggerFactory.class);
	private static final PinState[] TRIGGER_STATES = { PinState.LOW };// PinState.allStates();
	public static final List<PinState> TRIGGER_STATES_LIST = Arrays.asList(TRIGGER_STATES);

	public static OutputTargetedGpioTrigger createTriggerFor(TriggerConfig triggerConfig, GpioPinDigitalOutput targetPin)
			throws GpioTriggerCreationException {
		if (triggerConfig == null || triggerConfig.getType() == null || targetPin == null) {
			throw new IllegalArgumentException("PinConfig.type cannot be null!");
		}

		final List<PinState> states = TRIGGER_STATES_LIST;
		switch (triggerConfig.getType()) {
			case BLINK:
				if (triggerConfig.getDelay() == null) {
					throw new GpioTriggerCreationException("delay", GpioTriggerType.BLINK, targetPin);
				}
				final long duration;
				if (triggerConfig.getDuration() != null) {
					duration = triggerConfig.getDuration();
				} else {
					logger.info("'duration' config was not specified for ");
					duration = Long.MAX_VALUE;
				}
				return new GpioBlinkDurationStateTrigger(states, targetPin, triggerConfig.getDelay(), duration);
			case BLINK_STOP:
				return new GpioBlinkStopStateTrigger((GpioPin) null, states, targetPin);
			case INVERSE_SYNC:
				return new GpioInverseSyncStateTrigger(PinState.allStates(), targetPin);
			case PULSE:
				if (triggerConfig.getDuration() == null) {
					throw new GpioTriggerCreationException("duration", GpioTriggerType.PULSE, targetPin);
				}
				return new GpioPulseStateTrigger(states, targetPin, triggerConfig.getDuration());
			case TPULSE:
				if (triggerConfig.getDuration() == null) {
					throw new GpioTriggerCreationException("duration", GpioTriggerType.TPULSE, targetPin);
				}
				return new GpioTogglePulseStateTrigger(states, targetPin, triggerConfig.getDuration());
			case PUSH:
				return new GpioPushTrigger(states, targetPin, triggerConfig.getId(), triggerConfig.getName());
			case SET:
				if (triggerConfig.getNewState() == null) {
					throw new GpioTriggerCreationException("newState", GpioTriggerType.SET, targetPin);
				}
				PinState newState = PinState.valueOf(triggerConfig.getNewState().toString());
				return new GpioSetStateTrigger(states, targetPin, newState);
			case SYNC:
				return new GpioSyncStateTrigger(PinState.allStates(), targetPin);
			case TOGGLE:
				return new GpioToggleStateTrigger((GpioPin) null, states, targetPin);
			case SOUND:
				if (triggerConfig.getExtra() == null) {
					throw new GpioTriggerCreationException("extra", GpioTriggerType.SOUND, targetPin);
				}
				return new GpioSoundTrigger(targetPin, triggerConfig.getId(), triggerConfig.getExtra());
			default:
				throw new IllegalArgumentException("Unknown GpioTriggerState!");
		}
	}
}
