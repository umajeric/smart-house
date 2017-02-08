package si.majeric.smarthouse.alarm.listeners;

import com.thoughtworks.xstream.core.util.Base64Encoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import si.majeric.smarthouse.alarm.Alarm;
import si.majeric.smarthouse.camera.API;
import si.majeric.smarthouse.push.AbstractPush;

/**
 * Created by uros on 16/07/15.
 */
public class CameraEmailAlarmListener implements Alarm.AlarmChangeListener {
	static final Logger logger = LoggerFactory.getLogger(CameraEmailAlarmListener.class);

	private final API _api;

	public CameraEmailAlarmListener(API api) {
		_api = api;
	}

	@Override
	public void alarmTurnedOn() {
		_api.turnOnAlarm();
	}

	@Override
	public void alarmTurnedOff() {
		_api.turnOffAlarm();
	}

}
