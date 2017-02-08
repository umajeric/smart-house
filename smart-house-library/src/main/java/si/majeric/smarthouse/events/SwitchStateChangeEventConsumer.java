package si.majeric.smarthouse.events;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.alarm.Alarm;
import si.majeric.smarthouse.cron.CronTriggerScheduler;
import si.majeric.smarthouse.dao.SwitchDao;
import si.majeric.smarthouse.model.Address.Pin;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.Switch.NotificationType;
import si.majeric.smarthouse.notification.AbstractNotification;
import si.majeric.smarthouse.stats.Databox;

/**
 * Class that saves the new state on each save.
 * 
 * @author Uros Majeric
 * 
 */
public class SwitchStateChangeEventConsumer implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(SwitchStateChangeEventConsumer.class);

	private final SwitchDao _switchDao;
	private Map<NotificationType, AbstractNotification> notifications = new HashMap<Switch.NotificationType, AbstractNotification>();

	private Databox databox;

	{
		try {
			final Map<String, String> databoxConfig = Environment.getPropertyMap("databox");
			if (Boolean.valueOf(databoxConfig.get("enabled"))) {
				if (databoxConfig.get("token") == null || databoxConfig.get("token").isEmpty()) {
					logger.error("Please set 'databox.token' in config file.");
				}
				this.databox = new Databox(databoxConfig.get("token"));

				if (databoxConfig.get("host") != null && !databoxConfig.get("host").isEmpty()) {
					this.databox.setHost(databoxConfig.get("host"));
				}
			}
		} catch (Exception e) {
			databox = null;
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public SwitchStateChangeEventConsumer(SwitchDao switchDao) {
		_switchDao = switchDao;
	}

	public void registerNotification(NotificationType notificationType, AbstractNotification notification) {
		notifications.put(notificationType, notification);
	}

	@Override
	public void run() {
		Queue<SwitchStateChangeEvent> items = SwitchStateChangeEventListener.instance().getItems();
		logger.info("Starting saver deamon for items {} ...", items);
		while (true) {
			try {
				if (items.size() > 0) {
					SwitchStateChangeEvent event = items.poll();
					final Switch swtch = _switchDao.findFor(event.getProviderAddress(), Pin.valueOf(event.getPin()));
					if (swtch != null) {
						// if previous state was high and new state is low then we can calculate how long was the switch on
						if (PinState.HIGH.equals(swtch.getState()) && PinState.LOW.equals(event.getNewState())
								&& swtch.getModified() != null) {
							Long turnedOnSeconds = (System.currentTimeMillis() - swtch.getModified().getTime()) / 1000;
							if (databox != null) {
								Map<String,Object> test = new HashMap<String, Object>(){{put("switch", swtch.getHumanReadableId());}};
								databox.send("switch_on_seconds", turnedOnSeconds.doubleValue(), new Date(), test);
							}
						}

						// update switch only if state has changed
						if (swtch.getState() != event.getNewState()) {
							swtch.setState(event.getNewState());
							_switchDao.save(swtch);
						}

						// trigger push only on switch turning on
						if (PinState.HIGH.equals(event.getNewState())) {
							if (Alarm.getInstance().isOn() && notifications.containsKey(NotificationType.PUSH)) {
								// if switch was triggered by cron job then do not send the notification
								boolean wasCronTriggered = CronTriggerScheduler.wasTriggeredInDefaultTimeframe(swtch);
								if (!wasCronTriggered) {
									notifications.get(NotificationType.PUSH).trigger(swtch);
								} else {
									logger.info("Not sending the push as trigger was probably caused by cron.");
								}
							}
						}

						// trigger any notifications set for this switch
						// if (swtch.getNotificationType() != null && notifications.containsKey(swtch.getNotificationType())) {
						// notifications.get(swtch.getNotificationType()).trigger(swtch);
						// }
					}

				} else {
					Thread.sleep(100);
				}
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}

	}
}