package si.majeric.smarthouse.notification.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.notification.AbstractNotification;
import si.majeric.smarthouse.push.AbstractPush;

/**
 * 
 * @author Uros Majeric
 *
 */
public class PushNotification extends AbstractNotification {
	static final Logger logger = LoggerFactory.getLogger(PushNotification.class);

	public PushNotification() {
	}

	@Override
	public void trigger(Switch swtch) {
		AbstractPush.getDefaultImpl().sendPush("{\"data\": \"" + swtch.getHumanReadableId() + " triggered\", \"type\":\"AlarmTrigger\"}");
	}
}
