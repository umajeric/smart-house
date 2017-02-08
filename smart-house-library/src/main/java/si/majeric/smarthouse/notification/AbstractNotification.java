package si.majeric.smarthouse.notification;

import si.majeric.smarthouse.model.Switch;

/**
 * 
 * @author Uros Majeric
 *
 */
public abstract class AbstractNotification {

	/**
	 * 
	 * @param swtch
	 */
	abstract public void trigger(Switch swtch);
}
