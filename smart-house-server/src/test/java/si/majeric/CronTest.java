package si.majeric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.SmartHouse;
import si.majeric.smarthouse.cron.CronTriggerScheduler;
import si.majeric.smarthouse.exception.TriggerNotConfiguredException;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.Cron;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;

/**
 * http://www.quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/tutorial-lesson-06
 *
 * @author uros (Nov 4, 2013)
 *
 */
public class CronTest extends TestCase {
	private static final Logger logger = LoggerFactory.getLogger(CronTest.class);

	@Override
	public void setUp() {
	}

	public void testCronExpression() {
		try {
			final TestSmartHouse testSmartHouse = new TestSmartHouse();
			for (int i = 1; i < 4; i++) {
				/* create new trigger */
				TriggerConfig triggerConf = new TriggerConfig();
				triggerConf.setId("tigger-" + i);

				/* create a new cron for the trigger */
				final Cron cron = new Cron();
				cron.setExpression("/" + (i * 2) + " * * * * ?");
				triggerConf.setCron(cron);

				/* schedule a new trigger */
				CronTriggerScheduler.schedule(testSmartHouse, null, triggerConf);
			}

			// and start it off
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();

			/* sleep for twenty seconds to allow trigger to be invoked */
			Thread.sleep(20000);

			scheduler.shutdown();
		} catch (SchedulerException se) {
			logger.error(se.getLocalizedMessage(), se);
		} catch (InterruptedException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public static class TestSmartHouse implements SmartHouse {
		private static final Logger logger = LoggerFactory.getLogger(TestSmartHouse.class);

		@Override
		public Map<String, PinState> invokeTrigger(TriggerConfig triggerConfig) throws TriggerNotConfiguredException, IOException {
			logger.info("Simulating trigger invoke: " + triggerConfig);
			return new HashMap<>();
		}

		@Override
		public Map<String, PinState> invokeSwitch(Switch aSwitch) {
			return new HashMap<>();
		}

		@Override
		public void init() {
		}

		@Override
		public Configuration getConfiguration() {
			return null;
		}

		@Override
		public List<Switch> getProvisionedSwitches() {
			return new ArrayList<Switch>();
		}

		@Override
		public Map<String, PinState> getProvisionedPinStates() {
			return new HashMap<String, PinState>();
		}

		@Override
		public PinState getProvisionedPinState(Switch swtch) {
			return null;
		}
	};
}
