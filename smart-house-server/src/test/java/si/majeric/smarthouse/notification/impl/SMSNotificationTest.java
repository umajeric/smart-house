package si.majeric.smarthouse.notification.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Uros Majeric
 *
 */
public class SMSNotificationTest extends TestCase {
	static final Logger logger = LoggerFactory.getLogger(SMSNotificationTest.class);

	public void testTrigger() throws UnknownHostException, IOException {
//		checkHosts("192.168.1");
		boolean reachable = InetAddress.getByName("192.168.1.100").isReachable(1000);
		// SMSNotification smsNotification = new SMSNotification(null);
		// Switch swtch = new Switch();
		// swtch.setName("test");
		// smsNotification.trigger(swtch);
	}

	private void checkHosts(String subnet) throws UnknownHostException, IOException {
		int timeout = 1000;
		for (int i = 1; i < 254; i++) {
			String host = subnet + "." + i;
			if (InetAddress.getByName(host).isReachable(timeout)) {
				System.out.println(host + " is reachable");
			}
		}
	}

}
