package si.majeric.smarthouse.event;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.dao.DBSHDaoFactory;
import si.majeric.smarthouse.events.SwitchStateChangeEventListener;
import si.majeric.smarthouse.events.SwitchStateChangeEventConsumer;
import si.majeric.smarthouse.model.PinState;

public class SwitchStateSaverTest extends TestCase {
	static final Logger logger = LoggerFactory.getLogger(SwitchStateSaverTest.class);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testSwitchUpdate() {
		try {
			DBSHDaoFactory daoFactory = new DBSHDaoFactory();
			Thread switchStateSaverThread = new Thread(new SwitchStateChangeEventConsumer(daoFactory.getSwitchDao()));
			switchStateSaverThread.setDaemon(false);
			switchStateSaverThread.start();

			SwitchStateChangeEventListener.instance().notifyStateChanged(34, "A0", PinState.LOW);
			SwitchStateChangeEventListener.instance().notifyStateChanged(34, "A0", PinState.HIGH);

			logger.info("Test Switch Update finished...");
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
