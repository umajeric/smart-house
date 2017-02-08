package si.majeric.smarthouse.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import si.majeric.smarthouse.Environment;

/**
 * 
 * @author Uros Majeric
 *
 */
public class ZeroPush extends AbstractPush {
	private static final String ZEROPUSH_BROADCAST_API_ENDPOINT = "https://zeropush.pushwoosh.com/broadcast";

	static final Logger logger = LoggerFactory.getLogger(ZeroPush.class);

	private Map<String, String> _config = new HashMap<String, String>();

	{
		_config = Environment.getPropertyMap("zeropush");
	}

	public ZeroPush() {
		this(null);
	}

	public ZeroPush(Map<String, String> config) {
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
		if (_config.get("auth_token") == null || _config.get("auth_token").isEmpty()) {
			logger.error("Please set 'zeropush.auth_token' in config file.");
			return;
		}
		String rawData = "";
		HttpsURLConnection conn = null;
		OutputStream os = null;
		try {
			for (Entry<String, String> config : _config.entrySet()) {
				rawData += config.getKey() + "=" + URLEncoder.encode(config.getValue(), "UTF-8") + "&";

			}
			rawData += "data=" + URLEncoder.encode(text, "UTF-8");

			String type = "application/x-www-form-urlencoded";
			String encodedData = rawData;
			URL url = new URL(ZEROPUSH_BROADCAST_API_ENDPOINT);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", type);
			// conn.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setConnectTimeout(5000);

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(encodedData);
			wr.flush();
			wr.close();
			// os = conn.getOutputStream();
			// os.write(encodedData.getBytes());

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuffer input = new StringBuffer();
			String in;
			while ((in = br.readLine()) != null) {
				input.append(in);
			}
			br.close();
			logger.info(input.toString());
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		} finally {
			if (conn != null) {
				conn.disconnect();
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
