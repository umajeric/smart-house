package si.majeric.smarthouse;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import si.majeric.CronTest;
import si.majeric.smarthouse.alarm.MotionDetectionSnapWatch;

/**
 *
 * @author Uros Majeric
 *
 */
public class MotionDetectionSnapWatchTest extends TestCase {
	static final Logger logger = LoggerFactory.getLogger(MotionDetectionSnapWatchTest.class);

	public void testTrigger() throws IOException {
		final String motionDetectionPath = Environment.getProperty("motion.detection.path");
		if (motionDetectionPath != null) {
			Path folder = Paths.get(motionDetectionPath);
			SmartHouse sh = new CronTest.TestSmartHouse();
			MotionDetectionSnapWatch watcher = new MotionDetectionSnapWatch(sh, folder);
			watcher.run();
		}
	}

}
