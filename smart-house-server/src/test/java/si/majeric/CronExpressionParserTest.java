package si.majeric;

import junit.framework.TestCase;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.cron.CronExpressionParser;

/**
 *
 * @author Uros Majeric
 *
 */
public class CronExpressionParserTest extends TestCase {
	static final Logger logger = LoggerFactory.getLogger(CronExpressionParserTest.class);

	public void testParser() {
		CronExpressionParser parser = CronExpressionParser.getInstance();

		CronExpression cronExpression = parser.getCronExpression(null);
		assertEquals(null, cronExpression);

		cronExpression = parser.getCronExpression("");
		assertEquals(null, cronExpression);

		cronExpression = parser.getCronExpression("SUNSET");
		assertNotNull(cronExpression);

		cronExpression = parser.getCronExpression("SUNRISE");
		assertNotNull(cronExpression);

		cronExpression = parser.getCronExpression("SUNSET+1800");
		assertNotNull(cronExpression);
		
		cronExpression = parser.getCronExpression("SUNSET+1801");
		assertNotNull(cronExpression);
		
		cronExpression = parser.getCronExpression("SUNSET+600");
		assertNotNull(cronExpression);

		cronExpression = parser.getCronExpression("SUNRISE-1800");
		assertNotNull(cronExpression);
	}
}
