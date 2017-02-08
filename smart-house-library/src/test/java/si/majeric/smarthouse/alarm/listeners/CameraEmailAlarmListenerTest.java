package si.majeric.smarthouse.alarm.listeners;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by uros on 17/07/15.
 */
public class CameraEmailAlarmListenerTest extends TestCase {
	private Map<String, String> _testConfig = new HashMap<String, String>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_testConfig.put("host", "");
		_testConfig.put("username", "");
		_testConfig.put("password", "");
	}

	public void testAlarmTurnedOn() throws Exception {
//		new CameraEmailAlarmListener(_testConfig).alarmTurnedOn();
	}

	public void testAlarmTurnedOff() throws Exception {
//		new CameraEmailAlarmListener(_testConfig).alarmTurnedOff();
	}
}