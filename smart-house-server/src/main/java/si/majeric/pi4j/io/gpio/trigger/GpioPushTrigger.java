package si.majeric.pi4j.io.gpio.trigger;

/**
 *
 * @author Uros Majeric
 */
import java.io.IOException;
import java.util.List;

import si.majeric.smarthouse.camera.API;
import si.majeric.smarthouse.push.AbstractPush;

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GpioPushTrigger extends OutputTargetedGpioTrigger {
	static final Logger logger = LoggerFactory.getLogger(GpioPushTrigger.class);

	private final String triggerId;
	private final String pushText;

	public GpioPushTrigger(GpioPinDigitalOutput targetPin, String triggerId, String pushText) {
		super(targetPin);
		this.triggerId = triggerId;
		this.pushText = pushText;
	}

	public GpioPushTrigger(PinState state, GpioPinDigitalOutput targetPin, String triggerId, String pushText) {
		super(state, targetPin);
		this.triggerId = triggerId;
		this.pushText = pushText;
	}

	public GpioPushTrigger(PinState[] states, GpioPinDigitalOutput targetPin, String triggerId, String pushText) {
		super(states, targetPin);
		this.triggerId = triggerId;
		this.pushText = pushText;
	}

	public GpioPushTrigger(List<PinState> states, GpioPinDigitalOutput targetPin, String triggerId, String pushText) {
		super(states, targetPin);
		this.triggerId = triggerId;
		this.pushText = pushText;
	}

	@Override
	public void invoke(GpioPin pin, PinState state) {
		super.invoke(pin, state);
		// if pin is null then the trigger was invoked from the smart client
		if (pin == null) {
			if (targetPin != null) {
				targetPin.pulse(500);
			}
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					final String oiPath = "/tmp/doorbell.jpg";
					AbstractPush.getDefaultImpl().sendPush("{\"data\": \"" + pushText + "\", \"type\":\"PushTrigger\", \"triggerId\":\"" + triggerId + "\"}");
					API.instance().sendTestEmail();
					try {
						API.instance().saveSnapImage(oiPath);
					} catch (IOException e) {
						logger.error(e.getLocalizedMessage(), e);
					}
				}
			}).start();
		}
	}
}
