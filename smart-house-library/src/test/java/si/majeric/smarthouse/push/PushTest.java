package si.majeric.smarthouse.push;

import junit.framework.TestCase;

import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Uros Majeric
 */
public class PushTest extends TestCase {
	static final Logger logger = LoggerFactory.getLogger(PushTest.class);

	public void testTrigger() {
//		Map<String, String> config = new HashMap<>();
//		config.put("auth_token","gcmprod_Bd4WJckF43MKGCUwKmeg");
//		config.put("collapse_key","push");
//		config.put("delay_while_idle","false");
//		config.put("time_to_live","40320");
//
//		final ZeroPush zeroPush = new ZeroPush(config);
//		zeroPush.sendPush("{\"data\": \"" + "Kuhinja luc" + " triggered\", \"type\": \"AlarmTrigger\"}");
	}

	public void testTrigger2() throws JSONException, MalformedURLException {
//		Map<String, String> config = new HashMap<>();
//		config.put("auth_token", "ozSIIpoZitb3bTKClYYK8XkBaCFkBZpvYwdFGBgcaTtplcc7773xTQ4LrYm7n3jddF25lhbRzFcV3QUqCIhi");
//		config.put("app_code", "GCMPROD_1V2SPQ3VJYJAHZYHVS9B");
//
//		final PushWoosh zeroPush = new PushWoosh(config);
//		zeroPush.sendPush("{\"data\": \"" + "Kuhinja luc" + " triggered\", \"type\": \"AlarmTrigger\"}");
	}

	public void testTrigger3() throws JSONException, MalformedURLException {
		Map<String, String> config = new HashMap<>();
		config.put("app_id", "a52cde32-60e4-4a05-8bdd-22f90b8d979a");
		config.put("api_key", "NjkyNWEwZDUtNTNiZC00ZDViLWFiN2MtNzYyNzk2M2RiZWY2");

		final OneSignal push = new OneSignal(config);
		push.sendPush("{\"data\": \"" + "Kuhinja luc triggered\", \"type\": \"AlarmTrigger\"}");
	}


}
