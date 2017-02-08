package si.majeric.smarthouse.alarm;

import java.net.InetAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.push.AbstractPush;

/**
 * 
 * @author Uros Majeric
 *
 */
public class KnownHostsChecker implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(KnownHostsChecker.class);
	Alarm _alarm;

	{
		_alarm = Alarm.getInstance();
	}

	@Override
	public void run() {

		// if no known hosts were defined then do not start the checker
		List<String> knownIPs = Environment.getKnownIPs();
		if (knownIPs != null && !knownIPs.isEmpty()) {
			logger.info("Know host checker is now running...");
			// loop forever
			while (true) {
				try {
					if (!_alarm.isSnoozed()) {
						boolean doWeHaveReachable = checkForKnownHosts(knownIPs);
						if (!doWeHaveReachable && _alarm.isOff()) {
							_alarm.turnOn();
							logger.info("Know host checker: Alarm is ON");
						} else if (doWeHaveReachable && _alarm.isOn()) {
							_alarm.turnOff();
							logger.info("Know host checker: Alarm is OFF");
							
						}
					}
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		logger.info("Know host checker is stopped...");
	}

	public boolean checkForKnownHosts(List<String> knownIPs) {
		if (knownIPs != null && !knownIPs.isEmpty()) {
			for (String ip : knownIPs) {
				try {
					boolean reachable = InetAddress.getByName(ip).isReachable(5000);
					if (reachable)
						return true;
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
			return false;
		}
		return true;
	}
}
