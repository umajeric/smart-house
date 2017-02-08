package si.majeric.smarthouse.push;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import si.majeric.smarthouse.Environment;

/**
 * @author Uros Majeric
 */
public class PushWoosh extends AbstractPush {
	public static final String PUSHWOOSH_SERVICE_BASE_URL = "https://cp.pushwoosh.com/json/1.3/";

	static final Logger logger = LoggerFactory.getLogger(PushWoosh.class);

	private Map<String, String> _config = new HashMap<String, String>();

	{
		_config = Environment.getPropertyMap("pushwoosh");
	}

	public PushWoosh() {
		this(null);
	}

	public PushWoosh(Map<String, String> config) {
		if (config != null) {
			_config.putAll(config);
		}
	}

	@Override
	public void sendPush(String text) {
		sendPush(text, null);
	}

	@Override
	public void sendPush(String text, Map<String, String> data) {
		if (_config.get("auth_token") == null || _config.get("auth_token").isEmpty()
				|| _config.get("app_code") == null || _config.get("app_code").isEmpty()) {
			logger.error("Please set 'pushwoosh.auth_token' AND 'pushwoosh.app_code' in config file.");
			return;
		}

		try {
			JSONObject params = new JSONObject(text);

			final JSONObject jsonObject = new JSONObject().put("send_date", "now")
					.put("content", "SmartHouse")
					.put("android_root_params", params);

			if (data != null) {
				final JSONObject object = new JSONObject();
				for (Map.Entry<String, String> entry : data.entrySet()) {
					object.put(entry.getKey(), entry.getValue());
				}
				jsonObject.put("data", new JSONArray().put(object));
			}
			JSONArray notificationsArray = new JSONArray().put(jsonObject);

			JSONObject requestObject = new JSONObject()
					.put("application", _config.get("app_code"))
					.put("auth", _config.get("auth_token"))
					.put("notifications", notificationsArray);

			JSONObject mainRequest = new JSONObject().put("request", requestObject);


			String method = "createMessage";
			URL url = new URL(PUSHWOOSH_SERVICE_BASE_URL + method);
			JSONObject response = SendServerRequest.sendJSONRequest(url, mainRequest.toString());
			logger.info(response.toString());
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}

	}

	static class SendServerRequest {
		static JSONObject sendJSONRequest(URL url, String request) throws Exception {
			HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setDoInput(true);
				connection.setDoOutput(true);

				DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
				writer.write(request.getBytes("UTF-8"));
				writer.flush();
				writer.close();

				return parseResponse(connection);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}

		static JSONObject parseResponse(HttpURLConnection connection) throws IOException, JSONException {
			String line;
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				response.append(line).append('\r');
			}
			reader.close();

			return new JSONObject(response.toString());
		}
	}
}
