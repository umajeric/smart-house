package si.majeric.smarthouse.notification.impl;

import java.io.IOException;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.model.Switch;

/**
 *
 * @author Uros Majeric
 *
 */
public class PushNotificationTest extends TestCase {
	static final Logger logger = LoggerFactory.getLogger(PushNotificationTest.class);

	public void testTrigger() throws UnknownHostException, IOException {
		PushNotification notification = new PushNotification();
		Switch swtch = new Switch();
		swtch.setName("test");
		notification.trigger(swtch);
	}

}
