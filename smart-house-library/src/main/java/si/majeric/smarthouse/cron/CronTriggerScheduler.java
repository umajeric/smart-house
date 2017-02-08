package si.majeric.smarthouse.cron;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.HashMap;
import java.util.Map;

import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.SmartHouse;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;

/**
 *
 * @author uros (Nov 10, 2013)
 *
 */
public class CronTriggerScheduler {
	private static final Logger logger = LoggerFactory.getLogger(CronTriggerScheduler.class);
	private static final String TRIGGER_CONFIG_KEY = "TRIGGER_CONF";
	private static final String SWITCH_KEY = "SWITCH";
	private static final String SMART_HOUSE_KEY = "SMART_HOUSE";

	private static final Long DEFAULT_TRIGGER_TIMEFRAME_MS = 1000 * 60L; // 60 seconds
	private static final Map<Switch, Long> _switchLastTrigger = new HashMap<Switch, Long>();

	public static void schedule(SmartHouse smartHouse, Switch swtch, TriggerConfig triggerConf) {
		if (triggerConf == null || triggerConf.getCron() == null) {
			return;
		}
		CronExpressionParser parser = CronExpressionParser.getInstance();
		CronExpression cronExpression = parser.getCronExpression(triggerConf.getCron().getExpression());

		if (cronExpression == null) {
			return;
		}

		try {
			JobKey myJobKey = new JobKey("JOB-" + triggerConf.getId());
			JobDataMap newJobDataMap = new JobDataMap();
			newJobDataMap.put(SMART_HOUSE_KEY, smartHouse);
			newJobDataMap.put(SWITCH_KEY, swtch);
			newJobDataMap.put(TRIGGER_CONFIG_KEY, triggerConf);
			JobDetail job = newJob(TriggerConfigJob.class).withIdentity(myJobKey).usingJobData(newJobDataMap).build();

			CronTrigger trigger = newTrigger() //
					.withIdentity(triggerConf.getId(), "DEFAULT_SH_GROUP") //
					.withSchedule(cronSchedule(cronExpression)) //
					.forJob(myJobKey) //
					.build();

			logger.info("Cron scheduled for {} at '{}'", triggerConf, cronExpression.getCronExpression());
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.scheduleJob(job, trigger);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public static final class TriggerConfigJob implements Job {
		@Override
		public void execute(JobExecutionContext context) throws JobExecutionException {
			try {
				final SmartHouse smartHouse = (SmartHouse) context.getJobDetail().getJobDataMap().get(SMART_HOUSE_KEY);
				if (smartHouse != null) {
					final Switch swtch = (Switch) context.getJobDetail().getJobDataMap().get(SWITCH_KEY);
					_switchLastTrigger.put(swtch, System.currentTimeMillis());

					final TriggerConfig tc = (TriggerConfig) context.getJobDetail().getJobDataMap().get(TRIGGER_CONFIG_KEY);
					smartHouse.invokeTrigger(tc);
				}
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
	}

	/**
	 *
	 * @param swtch
	 * @param start
	 * @param end
	 *
	 * @return true if switch was triggered in last {@code timeframe} milliseconds
	 */
	public static synchronized boolean wasTriggeredInTimeframeBetween(Switch swtch, Long start, Long end) {
		if (swtch != null) {
			final Long lastTrigger = _switchLastTrigger.get(swtch);
			final long ctms = System.currentTimeMillis();
			if (lastTrigger != null && lastTrigger < (ctms - start) && lastTrigger > (ctms - end)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param swtch
	 * @param timeframe
	 * 
	 * @return true if switch was triggered in last {@code timeframe} milliseconds
	 */
	public static synchronized boolean wasTriggeredInLastTimeframe(Switch swtch, Long timeframe) {
		if (swtch != null) {
			final Long lastTrigger = _switchLastTrigger.get(swtch);
			final long ctms = System.currentTimeMillis();
			if (lastTrigger != null && lastTrigger > (ctms - timeframe)) {
				return true;
			}
		}
		return false;
	}

	public static synchronized boolean wasTriggeredInDefaultTimeframe(Switch swtch) {
		return wasTriggeredInLastTimeframe(swtch, DEFAULT_TRIGGER_TIMEFRAME_MS);
	}

	/**
	 * Method marks switch as triggered expectedly - meaning alarm notification should not be sent for such trigger
	 *
	 * @param swtch
     */
	public static synchronized void notifySwitchTriggeredExpectedly(Switch swtch) {
		_switchLastTrigger.put(swtch, System.currentTimeMillis());
	}
}
