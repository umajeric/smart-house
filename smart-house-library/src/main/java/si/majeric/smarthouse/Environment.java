package si.majeric.smarthouse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Environment {
	private static final Logger logger = LoggerFactory.getLogger(Environment.class);
	public static Properties prop = new Properties();

	static {
		try {
			final InputStream properties = Environment.class.getClassLoader().getResourceAsStream("config.properties");
			if (properties != null) {
				prop.load(properties);
				try {
					prop.load(new FileInputStream("/etc/smart-house"));
				} catch (IOException ex) {
				/* ignore if there is no override conf file - use defualts */
				}
			}
		} catch (IOException ex) {
			logger.error(ex.getLocalizedMessage(), ex);
		}
	}

	public static String getBindAddress() {
		return prop.getProperty("bind-address");
	}

	public static Integer getBindPort() {
		try {
			return Integer.valueOf(prop.getProperty("bind-port"));
		} catch (NumberFormatException e) {
			return 8000;
		}
	}

	public static String getConfigModelFile() {
		return prop.getProperty("config-model-file");
	}

	public static String getUsername() {
		return prop.getProperty("username");
	}

	public static String getLatitude() {
		String latitude = prop.getProperty("latitude");
		if (latitude == null || latitude.isEmpty()) {
			latitude = "46.5500"; // latitude for Maribor (if not set)
		}
		return latitude;
	}

	public static String getLongitude() {
		String longitude = prop.getProperty("longitude");
		if (longitude == null || longitude.isEmpty()) {
			longitude = "15.6500"; // longitude for Maribor (if not set)
		}
		return longitude;
	}

	public static String getPassword() {
		return prop.getProperty("password");
	}
	
	public static List<String> getKnownIPs() {
		String[] ips = prop.getProperty("known-ip-list", "").split(",");
		return Arrays.asList(ips);
	}

	public static int getLogMessagesSize() {
		try {
			return Integer.valueOf(prop.getProperty("log-messages-size"));
		} catch (NumberFormatException e) {
			return 100;
		}
	}

	public static String getProperty(String property) {
		return prop.getProperty(property);
	}

	/**
	 * @param prefix
	 *
	 * @return Method returns all properties that starts with some prefix and dot (.)
	 */
	public static Map<String, String> getPropertyMap(String prefix) {
		return getPropertyMap(prefix, false);
	}

	/**
	 * @param prefix
	 * @param retainPrefix
	 * 
	 * @return Method returns all properties that starts with some prefix and dot (.)
	 */
	public static Map<String, String> getPropertyMap(String prefix, boolean retainPrefix) {
		if (prefix == null) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		for (Entry<Object, Object> property : prop.entrySet()) {
			String key = property.getKey().toString();
			if (key.startsWith(prefix + ".")) {
				if (!retainPrefix) {
					key = key.replaceFirst(prefix + ".", "");
				}
				map.put(key, property.getValue().toString());
			}
		}
		return map;
	}
}