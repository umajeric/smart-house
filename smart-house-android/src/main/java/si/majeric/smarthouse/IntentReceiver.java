package si.majeric.smarthouse;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.alarm.Alarm;
import si.majeric.smarthouse.alarm.Alarm.State;
import si.majeric.smarthouse.client.Client;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.service.AlarmService;
import si.majeric.smarthouse.service.TriggerService;

/**
 * TODO http://developer.android.com/training/notify-user/expanded.html
 *
 * @author Uros Majeric
 */
public class IntentReceiver extends BroadcastReceiver {
	private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

	@Override
	public void onReceive(Context context, Intent intent) {
		context = context.getApplicationContext();
		final Bundle extras = intent.getExtras();

		String data = extras.getString("data");
		String pushType = extras.getString("type");
		String stateString = extras.getString("state");
		String triggerId = extras.getString("triggerId");

		if (data == null || pushType == null) {
			try {
				final String custom = extras.getString("custom");
				JSONObject params = new JSONObject(custom).getJSONObject("a");
				data = params.getString("data");
				pushType = pushType == null && params.has("type") ? params.getString("type") : pushType;
				stateString = stateString == null && params.has("state") ? params.getString("state") : stateString;
				triggerId = triggerId == null && params.has("triggerId") ? params.getString("triggerId") : triggerId;
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage(), e);
				return;
			}
		}

		if (data != null && pushType != null) {
			if ("PushTrigger".equals(pushType)) {
				triggerInvokeNotification(context, data, triggerId);
			} else if ("Alarm".equals(pushType)) {
				Alarm.State state = null;
				if (stateString != null) {
					state = Alarm.State.valueOf(stateString);
				}
				IntentReceiver.triggerAlarmNotification(context, data, state);
			} else if ("AlarmTrigger".equals(pushType)) {
				triggerAlarmTriggerNotification(context, data);
			}
		} else {
			logger.error("Unknown push.");
		}
		abortBroadcast(); // do not propagate the broadcast to OneSignal broadcast receiver
	}

	private void triggerAlarmTriggerNotification(Context context, String data) {
		Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.intruder_alert);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context) //
				.setSmallIcon(R.drawable.ic_launcher) //
				.setContentTitle(data) //
				.setContentText("Alarm was triggered") //
				// .setSubText("Action required")
				.setDefaults(Notification.DEFAULT_ALL)//
				.setSound(alarmSound);

		// playSoundOnMuted(context);

		Intent resultIntent = new Intent(context, MainActivity.class);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = mBuilder.build();
		notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
		// notification.priority = Notification.PRIORITY_HIGH;
		mNotificationManager.notify(3, notification);
	}

	public static void triggerAlarmNotification(Context context, String data, State state) {
		IntentReceiver.triggerAlarmNotification(context, data, state, false);
	}

	public static void triggerAlarmNotification(Context context, String data, State state, boolean ongoing) {
		// Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Uri alarmSound = null;
		if (state != null && Alarm.State.OFF.equals(state)) {
			alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.door_entry_notification);
		} else {
			alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.fire_pager);
		}
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context) //
				.setSmallIcon(R.drawable.ic_launcher) //
				.setContentTitle(data) //
				.setContentText("Swipe down for more actions") //
				// .setSubText("Action required")
				.setDefaults(Notification.DEFAULT_ALL)//
				.setSound(alarmSound);

		// playSoundOnMuted(context);

		Intent resultIntent = new Intent(context, MainActivity.class);

		boolean alarmOn = state != null && Alarm.State.ON.equals(state);
		// Buttons in notifications
		Intent triggerIntent = new Intent(context, AlarmService.class);
		triggerIntent.setAction(alarmOn ? AlarmService.ACTION_TURN_OFF : AlarmService.ACTION_TURN_ON);
		PendingIntent piTrigger = PendingIntent.getService(context, 0, triggerIntent, 0);

		Intent dismissIntent = new Intent(context, AlarmService.class);
		dismissIntent.setAction(alarmOn ? AlarmService.ACTION_OK : AlarmService.ACTION_UNLOCK);
		PendingIntent piDismiss = PendingIntent.getService(context, 0, dismissIntent, 0);

		mBuilder.setStyle(new NotificationCompat.BigTextStyle()) //
				.addAction(0, alarmOn ? "OK" : "Open doors", piDismiss) //
				.addAction(0, "Snooze", piTrigger)
				.setOngoing(alarmOn || ongoing);
		// END Buttons

		// TODO move this to the server (KnownHostsChecker)
//		if (!alarmOn) {
//			Client client = ((SmartHouseApplication) context.getApplicationContext()).getClient();
//			client.invokeStateChange(new TriggerConfig("VhodLucVklopi_za_3_minute"), null);
//		}

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = mBuilder.build();
		notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
		// notification.priority = Notification.PRIORITY_HIGH;
		mNotificationManager.notify(AlarmService.NOTIFICATION_ID, notification);
	}

	private void triggerInvokeNotification(Context context, String data, String triggerId) {
		// Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.store_door_chime);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context) //
				.setSmallIcon(R.drawable.ic_launcher) //
				.setContentTitle(data) //
				.setContentText("Action required") //
				// .setSubText("Action required")
				.setDefaults(Notification.DEFAULT_ALL)//
				.setSound(alarmSound);

		// playSoundOnMuted(context);

		Intent resultIntent = new Intent(context, MainActivity.class);
		if (triggerId != null && !"".equals(triggerId)) {
			resultIntent.putExtra(SmartHouseApplication.TRIGGER_ID_KEY, triggerId);
			resultIntent.putExtra(SmartHouseApplication.TRIGGER_TITLE_KEY, data);
		}

		// Buttons in notifications
		Intent triggerIntent = new Intent(context, TriggerService.class);
		triggerIntent.putExtra(SmartHouseApplication.TRIGGER_ID_KEY, triggerId);
		triggerIntent.setAction(TriggerService.ACTION_TRIGGER);
		PendingIntent piTrigger = PendingIntent.getService(context, 0, triggerIntent, 0);

		Intent dismissIntent = new Intent(context, TriggerService.class);
		dismissIntent.setAction(TriggerService.ACTION_DISMISS);
		PendingIntent piDismiss = PendingIntent.getService(context, 0, dismissIntent, 0);

		mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("What would you like to do?")) //
				.addAction(0, "Cancel", piDismiss) //
				.addAction(0, "Trigger", piTrigger);
		// END Buttons

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(resultIntent);

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = mBuilder.build();
		notification.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
		// notification.priority = Notification.PRIORITY_HIGH;
		mNotificationManager.notify(TriggerService.NOTIFICATION_ID, notification);
	}

	@SuppressWarnings("unused")
	private void playSoundOnMuted(Context context) {
		try {
			Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

			final MediaPlayer mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(context, notification);
			// mMediaPlayer = MediaPlayer.create(context, notification);
			// final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

			mMediaPlayer.prepare();
			mMediaPlayer.setLooping(false);
			mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
				public void onPrepared(MediaPlayer arg0) {
					mMediaPlayer.seekTo(0);
					mMediaPlayer.start();

				}
			});
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
}