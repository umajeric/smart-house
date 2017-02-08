package si.majeric.pi4j.io.gpio.trigger;

/**
 *
 * @author Uros Majeric
 */

import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.camera.API;
import si.majeric.smarthouse.push.AbstractPush;

public class GpioSoundTrigger extends OutputTargetedGpioTrigger {
	static final Logger logger = LoggerFactory.getLogger(GpioSoundTrigger.class);

	private final String triggerId;
	private final String soundFileName;

	public GpioSoundTrigger(GpioPinDigitalOutput targetPin, String triggerId, String soundFileName) {
		super(targetPin);
		this.triggerId = triggerId;
		this.soundFileName = soundFileName;
	}

	public GpioSoundTrigger(PinState state, GpioPinDigitalOutput targetPin, String triggerId, String soundFileName) {
		super(state, targetPin);
		this.triggerId = triggerId;
		this.soundFileName = soundFileName;
	}

	public GpioSoundTrigger(PinState[] states, GpioPinDigitalOutput targetPin, String triggerId, String soundFileName) {
		super(states, targetPin);
		this.triggerId = triggerId;
		this.soundFileName = soundFileName;
	}

	public GpioSoundTrigger(List<PinState> states, GpioPinDigitalOutput targetPin, String triggerId, String soundFileName) {
		super(states, targetPin);
		this.triggerId = triggerId;
		this.soundFileName = soundFileName;
	}

	@Override
	public void invoke(GpioPin pin, PinState state) {
		super.invoke(pin, state);
		logger.info("Invoking sound trigger for sound file: " + this.soundFileName);
		// if pin is null then the trigger was invoked from the smart client
		if (pin == null) {
			if (targetPin != null) {
				targetPin.pulse(500);
			}
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						final File absoluteFile = new File(soundFileName).getAbsoluteFile();
						InputStream resourceSoundFile = Environment.class.getClassLoader().getResourceAsStream(soundFileName);
						AudioInputStream audioInputStream;
						InputStream bufferedIn;
						if (absoluteFile.exists()) {
							bufferedIn = new BufferedInputStream(AudioSystem.getAudioInputStream(absoluteFile));
						} else if (resourceSoundFile != null) {
							bufferedIn = new BufferedInputStream(AudioSystem.getAudioInputStream(resourceSoundFile));
						} else {
							resourceSoundFile = Environment.class.getClassLoader().getResourceAsStream("wav/recording_not_found.wav");
							bufferedIn = new BufferedInputStream(resourceSoundFile);
						}
						audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
						Clip clip = AudioSystem.getClip();
						clip.open(audioInputStream);
						clip.start();
						Thread.sleep(clip.getMicrosecondLength() / 1000);
					} catch(Exception ex) {
						logger.error(ex.getLocalizedMessage(), ex);
					}
				}
			}).start();
		}
	}
}
