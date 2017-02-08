package si.majeric.smarthouse.notification.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.notification.AbstractNotification;

/**
 * 
 * @author Uros Majeric
 *
 */
public class SMSNotification extends AbstractNotification {
	private static final String SMSAPI_SEND_SMS_API_ENDPOINT = "https://www.smsapi.si/poslji-sms";
	static final Logger logger = LoggerFactory.getLogger(SMSNotification.class);

	private Map<String, String> _config = new HashMap<String, String>();

	{
		_config = Environment.getPropertyMap("smsapi");
	}

	public SMSNotification() {

	}

	@Override
	public void trigger(Switch swtch) {
		if (_config.get("un") == null || _config.get("un").isEmpty() //
				|| _config.get("ps") == null || _config.get("ps").isEmpty()) {
			logger.error("Please set 'smsapi.un' AND 'smsapi.ps' in config file.");
			return;
		}

		String rawData = "";
		HttpsURLConnection conn = null;
		OutputStream os = null;
		try {
			for (Entry<String, String> config : _config.entrySet()) {
				rawData += config.getKey() + "=" + URLEncoder.encode(config.getValue(), "UTF-8") + "&";
			}
			rawData += "m=" + URLEncoder.encode("Switch triggered " + swtch.getName(), "UTF-8");

			logger.info(rawData);

			String type = "application/x-www-form-urlencoded";
			String encodedData = rawData;
			URL url = new URL(SMSAPI_SEND_SMS_API_ENDPOINT);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", type);
			// conn.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
			conn.setDoOutput(true);
			conn.setDoInput(true);

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(encodedData);
			wr.flush();
			wr.close();
			// os = conn.getOutputStream();
			// os.write(encodedData.getBytes());

			print_https_cert(conn);

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

	private void print_https_cert(HttpsURLConnection con) {

		if (con != null) {

			try {

				System.out.println("Response Code : " + con.getResponseCode());
				System.out.println("Cipher Suite : " + con.getCipherSuite());
				System.out.println("\n");

				Certificate[] certs = con.getServerCertificates();
				for (Certificate cert : certs) {
					System.out.println("Cert Type : " + cert.getType());
					System.out.println("Cert Hash Code : " + cert.hashCode());
					System.out.println("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
					System.out.println("Cert Public Key Format : " + cert.getPublicKey().getFormat());
					System.out.println("\n");
				}

			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}
