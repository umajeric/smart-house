package si.majeric.smarthouse.alarm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Uros Majeric
 */
public class Alarm {
	static final Logger logger = LoggerFactory.getLogger(Alarm.class);

	public enum State {
		ON, OFF;
	}

	public interface AlarmChangeListener {
		void alarmTurnedOn();

		void alarmTurnedOff();
	}

	private static Alarm INSTANCE = null;
	private static SimpleDateFormat _sdf;

	private State _state = State.OFF;
	private Long _snoozedTill = null;

	private List<AlarmChangeListener> _listeners = new ArrayList<>();

	static {
		_sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
	}

	private Alarm() {
	}

	public static Alarm getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Alarm();
		}
		return INSTANCE;
	}

	public synchronized boolean isOn() {
		return _state == State.ON;
	}

	public synchronized boolean isOff() {
		return _state == State.OFF;
	}

	public synchronized void turnOn() {
		this.turnOn(null);
	}

	public synchronized void turnOn(Integer timeoutMinutes) {
		_state = State.ON;
		snooze(timeoutMinutes);
		for (AlarmChangeListener listener : _listeners) {
			listener.alarmTurnedOn();
		}
	}

	public synchronized void turnOff() {
		this.turnOff(null);
	}

	public synchronized void turnOff(Integer snoozeMinutes) {
		_state = State.OFF;
		snooze(snoozeMinutes);
		for (AlarmChangeListener listener : _listeners) {
			listener.alarmTurnedOff();
		}
	}

	public void snooze(Integer snoozeMinutes) {
		if (snoozeMinutes != null) {
			_snoozedTill = System.currentTimeMillis() + snoozeMinutes * 60 * 1000;
		} else {
			_snoozedTill = null;
		}
	}

	public boolean isSnoozed() {
		boolean snoozed = _snoozedTill != null && _snoozedTill < System.currentTimeMillis();
		if (_snoozedTill != null) {
			logger.info("isSnoozed: " + snoozed + (_snoozedTill != null ? ", till: " + _sdf.format(new Date(_snoozedTill)) : ""));
		}
		return snoozed;
	}

	public void addChangeListener(AlarmChangeListener listener) {
		_listeners.add(listener);
	}

}
