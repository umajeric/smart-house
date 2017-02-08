package si.majeric.smarthouse.push;

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
import java.util.Map.Entry;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import si.majeric.smarthouse.Environment;

/**
 * @author Uros Majeric
 */
public class OneSignal extends AbstractPush {
	private static final String API_ENDPOINT = "https://onesignal.com/api/v1/notifications";

	static final Logger logger = LoggerFactory.getLogger(OneSignal.class);

	private Map<String, String> _config = new HashMap<String, String>();

	{
		_config = Environment.getPropertyMap("onesignal");
	}

	public OneSignal() {
		this(null);
	}

	public OneSignal(Map<String, String> config) {
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
		if (_config.get("app_id") == null || _config.get("app_id").isEmpty() ||
			_config.get("api_key") == null || _config.get("api_key").isEmpty()) {
			logger.error("Please set 'onesignal.app_id' AND 'onesignal.api_key' in config file.");
			return;
		}
		String jsonRespone = "";
		HttpsURLConnection con = null;
		OutputStream os = null;
		try {
			URL url = new URL(API_ENDPOINT);
			con = (HttpsURLConnection) url.openConnection();
			con.setUseCaches(false);
			con.setDoOutput(true);
			con.setDoInput(true);

			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			con.setRequestProperty("Authorization", "Basic " + _config.get("api_key"));
			con.setRequestMethod("POST");

			String strJsonBody = "{"
					+ "\"app_id\": \"" + _config.get("app_id") + "\","
					+ "\"included_segments\": [\"All\"],"
					+ "\"data\": " + text + ","
					+ "\"contents\": {\"en\": \"English Message\"},"
					+ "\"myKey\": \"my test key\""
					+ "}";

			System.out.println("strJsonBody:\n" + strJsonBody);

			byte[] sendBytes = strJsonBody.getBytes("UTF-8");
			con.setFixedLengthStreamingMode(sendBytes.length);

			os = con.getOutputStream();
			os.write(sendBytes);

			int httpResponse = con.getResponseCode();
			System.out.println("httpResponse: " + httpResponse);

			if (httpResponse >= HttpURLConnection.HTTP_OK
					&& httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
				Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
				jsonRespone = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
				scanner.close();
			} else {
				Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
				jsonRespone = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
				scanner.close();
			}
			System.out.println("jsonRespone:\n" + jsonRespone);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		} finally {
			if (con != null) {
				con.disconnect();
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

}
