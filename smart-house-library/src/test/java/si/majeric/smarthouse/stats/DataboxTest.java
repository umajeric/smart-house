package si.majeric.smarthouse.stats;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.Environment;

/**
 *
 * @author Uros Majeric
 *
 */
public class DataboxTest extends TestCase {
	static final Logger logger = LoggerFactory.getLogger(DataboxTest.class);

	public void testTrigger() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

		final Map<String, String> config = Environment.getPropertyMap("databox");
		if (config.get("token") == null || config.get("token").isEmpty()) {
			logger.error("Please set 'databox.token' in config file.");
		}

		Databox databox = new Databox(config.get("token"));
		try {
			databox.send("Kitchen light", 2d, sdf.parse("2017-02-06 00:00:00"));

			Map<String,Object> test = new HashMap<String, Object>(){{put("switch", "mm");}};
			databox.send("test", 2d, new Date(), test);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

}
