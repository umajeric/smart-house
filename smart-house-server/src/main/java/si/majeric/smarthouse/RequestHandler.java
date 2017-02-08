package si.majeric.smarthouse;

import org.h2.store.fs.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import si.majeric.smarthouse.alarm.Alarm;
import si.majeric.smarthouse.logging.SHMessageEncoder.Messages;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.tpt.Request;
import si.majeric.smarthouse.tpt.Response;

public class RequestHandler {
	static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
	private final SmartHouse _smartHouse;

	public RequestHandler(SmartHouse smartHouse) {
		_smartHouse = smartHouse;
	}

	public Response handleRequest(Request request) {
		Response response = new Response();

		switch (request.getType()) {
			case GetConfiguration:
				Configuration configuration = _smartHouse.getConfiguration();
				response.setObject(configuration);
				response.getStatus().setSucceeded(true);
				break;
			case GetStates:
				response.setObject(_smartHouse.getProvisionedPinStates());
				response.getStatus().setSucceeded(true);
				break;
			case GetVersion:
				configuration = _smartHouse.getConfiguration();
				response.setObject(configuration.getVersion());
				response.getStatus().setSucceeded(true);
				break;
			case GetMessages:
				response.getStatus().setSucceeded(true);
				response.setObject(Messages.getInstance().getMessages());
				break;
			case Invoke:
				try {
					final Switch aSwitch = request.getSwitch();
					final TriggerConfig triggerConfig = request.getTriggerConfig();
					Map<String, PinState> pinStates = null;
					if (aSwitch != null) {
						if (triggerConfig != null) {
							aSwitch.setTriggers(new ArrayList<TriggerConfig>());
							aSwitch.getTriggers().add(triggerConfig);
						}
						pinStates = _smartHouse.invokeSwitch(aSwitch);
					} else if (triggerConfig != null) {
						pinStates = _smartHouse.invokeTrigger(triggerConfig);
					}
					if (pinStates != null && !pinStates.isEmpty()) {
						response.setObject(pinStates);
						response.getStatus().setSucceeded(true);
					} else {
						response.getStatus().setMessage("Switch or trigger has to be configured");
						response.getStatus().setSucceeded(false);
					}
				} catch (Exception e) {
					response.getStatus().setMessage(e.getMessage());
					response.getStatus().setSucceeded(false);
				}
				break;
			case GetAlarmState:
				response.setObject(Alarm.getInstance().isOn() ? Alarm.State.ON.toString() : Alarm.State.OFF.toString());
				response.getStatus().setSucceeded(true);
				break;
			case AlarmOn:
				Integer minutes = getSnoozeMinutesFromRequest(request);
				Alarm.getInstance().turnOn(minutes);
				response.getStatus().setSucceeded(true);
				break;
			case AlarmOff:
				minutes = getSnoozeMinutesFromRequest(request);
				Alarm.getInstance().turnOff(minutes);
				response.getStatus().setSucceeded(true);
				break;
			case GetImageResource:
				final Map<String, String> properties = request.getProperties();
				String image = "/tmp/doorbell.jpg";
				if (properties != null && properties.containsKey("image")) {
					image = properties.get("image");
				}
				try {
					byte[] encoded = Files.readAllBytes(Paths.get(image));
					response.setObject(new String(encoded, "UTF-8"));
					response.getStatus().setSucceeded(true);
				} catch (IOException e) {
					logger.error(e.getLocalizedMessage(), e);
					response.getStatus().setSucceeded(false);
					response.getStatus().setMessage(e.getLocalizedMessage());
				}
				break;
			default:
				response.getStatus().setSucceeded(false);
				response.getStatus().setMessage("Wrong request type");
		}
		return response;
	}

	private Integer getSnoozeMinutesFromRequest(Request request) {
		Integer minutes = null;
		Map<String, String> properties = request.getProperties();
		if (properties != null && properties.containsKey(Request.SNOOZE_MINUTES)) {
			minutes = Integer.valueOf(properties.get(Request.SNOOZE_MINUTES));
		}
		return minutes;
	}
}
