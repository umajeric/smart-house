package si.majeric.smarthouse;

import java.io.IOException;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.alarm.KnownHostsChecker;

/**
 *
 * @author Uros Majeric
 *
 */
public class KnownHostsCheckerTest extends TestCase {
	static final Logger logger = LoggerFactory.getLogger(KnownHostsCheckerTest.class);

	public void testTrigger() throws UnknownHostException, IOException {
		KnownHostsChecker checker = new KnownHostsChecker();
//		 checker.run();
	}

}
