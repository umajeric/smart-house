package si.majeric.smarthouse.alarm.listeners;

import si.majeric.smarthouse.alarm.Alarm;
import si.majeric.smarthouse.push.AbstractPush;

/**
 * Created by uros on 16/07/15.
 */
public class PushNotificationAlarmListener implements Alarm.AlarmChangeListener {

	@Override
	public void alarmTurnedOn() {
		AbstractPush.getDefaultImpl().sendPush("{\"data\": \"Alarm vklopljen\", \"collapse_key\": \"alarm\", \"type\":\"Alarm\", \"state\":\"ON\"}");
	}

	@Override
	public void alarmTurnedOff() {
		AbstractPush.getDefaultImpl().sendPush("{\"data\": \"Alarm izklopljen\", \"collapse_key\": \"alarm\", \"type\":\"Alarm\", \"state\":\"OFF\"}");
	}
}
