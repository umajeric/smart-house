package si.majeric.smarthouse.events;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.model.PinState;

/**
 * Singleton class that listens for state changes on providers registered for changes.
 *
 * @author Uros Majeric
 *
 */
public class SwitchStateChangeEventListener {
	private static final Logger logger = LoggerFactory.getLogger(SwitchStateChangeEventListener.class);

	private static SwitchStateChangeEventListener INSTANCE;
	private final Queue<SwitchStateChangeEvent> _items = new ConcurrentLinkedQueue<SwitchStateChangeEvent>();

	private SwitchStateChangeEventListener() {
	}

	public static SwitchStateChangeEventListener instance() {
		if (INSTANCE == null) {
			INSTANCE = new SwitchStateChangeEventListener();
		}
		return INSTANCE;
	}

	/**
	 *
	 * @param providerAddress
	 *            Provider address for which the state was changed.
	 * @param name
	 *            Name of the PIN for which the state was changed (e.g.: A0, B3, etc.).
	 * @param state
	 * @return
	 */
	public boolean notifyStateChanged(int providerAddress, String name, PinState state) {
		SwitchStateChangeEvent event = new SwitchStateChangeEvent(providerAddress, name, state);
		// logger.info("notifyStateChanged... adding event {} to {}", event, _items);
		return _items.add(event);
	}

	public Queue<SwitchStateChangeEvent> getItems() {
		return _items;
	}
}