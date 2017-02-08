package si.majeric.smarthouse.pi.gpio;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CBus;

public class GPIOProviderMultiton {
	private static final Map<Integer, GPIOProviderMultiton> instances = new HashMap<Integer, GPIOProviderMultiton>();
	private final ExtendedMCP23017GpioProvider _provider;

	private GPIOProviderMultiton(int address) throws IOException {
		_provider = new ExtendedMCP23017GpioProvider(I2CBus.BUS_1, address);
	}

	public static synchronized GPIOProviderMultiton getInstance(Integer address) throws IOException {
		// Our "per key" singleton
		GPIOProviderMultiton instance = instances.get(address);

		if (instance == null) {
			// Lazily create instance
			instance = new GPIOProviderMultiton(address);

			// Add it to map
			instances.put(address, instance);
		}

		return instance;
	}

	public ExtendedMCP23017GpioProvider getProvider() {
		return _provider;
	}

	private GpioPinDigital getProvisionedDigitalPin(final Pin pin) {
		final Collection<GpioPin> provisionedPins = GpioFactory.getInstance().getProvisionedPins();
		if (_provider != null && pin != null) {
			for (GpioPin gpioPin : provisionedPins) {
				if (gpioPin != null && pin.getAddress() == gpioPin.getPin().getAddress() && pin.getName() != null //
						&& pin.getName().equals(gpioPin.getPin().getName()) && _provider.equals(gpioPin.getProvider()) && gpioPin instanceof GpioPinDigital) {
					return (GpioPinDigital) gpioPin;
				}
			}
		}
		return null;
	}

	public GpioPinDigitalOutput getProvisionedDigitalOutput(final Pin pin) {
		GpioPinDigital gpioPin = getProvisionedDigitalPin(pin);
		if (gpioPin != null && gpioPin instanceof GpioPinDigital) {
			return (GpioPinDigitalOutput) gpioPin;
		}
		return null;
	}

	public GpioPinDigitalInput getProvisionedDigitalInput(final Pin pin) {
		GpioPinDigital gpioPin = getProvisionedDigitalPin(pin);
		if (gpioPin != null && gpioPin instanceof GpioPinDigital) {
			return (GpioPinDigitalInput) gpioPin;
		}
		return null;
	}
}