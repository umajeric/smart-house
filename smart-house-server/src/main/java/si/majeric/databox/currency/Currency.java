package si.majeric.databox.currency;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.stats.Databox;

/**
 *
 */
public class Currency implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(Currency.class);

	// essential URL structure is built using constants
	public static final String ACCESS_KEY = "8830d4b17b4a526a9d8135c5d1d17c76";
	public static final String BASE_URL = "http://apilayer.net/api/";
	public static final String ENDPOINT = "live";

	Databox _databox;

	{
		try {
			final Map<String, String> databoxConfig = Environment.getPropertyMap("databox");
			if (Boolean.valueOf(databoxConfig.get("enabled"))) {
				if (databoxConfig.get("token") == null || databoxConfig.get("token").isEmpty()) {
					logger.error("Please set 'databox.token' in config file.");
				}
				_databox = new Databox(databoxConfig.get("token"));

				if (databoxConfig.get("host") != null && !databoxConfig.get("host").isEmpty()) {
					_databox.setHost(databoxConfig.get("host"));
				}
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void run() {
		while (true) {
			sendLiveRequest();
			try {
				Thread.sleep(1000 * 60 * 60);
			} catch (InterruptedException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
	}

	// sendLiveRequest() function is created to request and retrieve the data
	public void sendLiveRequest() {

		// The following line initializes the HttpGet Object with the URL in order to send a request
		try {

			// the following line converts the JSON Response to an equivalent Java Object
			final String liveRequestData = getLiveRequestData();
			if (liveRequestData != null) {
				JSONObject exchangeRates = new JSONObject(liveRequestData);
				final double usd2EurRate = exchangeRates.getJSONObject("quotes").getDouble("USDEUR");

				final Databox.KPI kpi = new Databox.KPI().setKey("usd_2_eur").setValue(usd2EurRate);
//				Date timeStampDate = new Date((long) (exchangeRates.getLong("timestamp") * 1000));
//				kpi.setDate(timeStampDate);
				_databox.send(Arrays.asList(kpi));
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public String getLiveRequestData() {
		String host = BASE_URL + ENDPOINT + "?access_key=" + ACCESS_KEY + "&currencies=EUR";

		if (host == null || host.isEmpty()) {
			logger.error("Please set the host");
			return null;
		}
		HttpURLConnection conn = null;
		OutputStream os = null;
		try {

			if (!host.startsWith("http")) {
				host = "http://" + host;
			}
			URL url = new URL(host);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			String type = "application/json";
			conn.setRequestProperty("Content-Type", type);
			conn.setRequestProperty("User-Agent", "SmartHouse");

			InputStream inputStream;
			if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
				inputStream = conn.getInputStream();
			} else {
				inputStream = conn.getErrorStream();
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			StringBuffer input = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null) {
				input.append(line);
			}
			in.close();
			return input.toString();
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
		return null;
	}

	public static void main(String[] args) throws IOException {
		(new Thread(new Currency())).start();
	}
}