package si.majeric.smarthouse.cron;

import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.Environment;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

/**
 *
 * @author Uros Majeric
 *
 */
public class CronExpressionParser {
	static final Logger logger = LoggerFactory.getLogger(CronExpressionParser.class);

	private static Pattern NUMBER_EXTRACT_PATTERN = Pattern.compile("\\d+");
	private static CronExpressionParser INSTANCE;
	private final SunriseSunsetCalculator _calculator;

	private CronExpressionParser() {
		Location location = new Location(Environment.getLatitude(), Environment.getLongitude());
		_calculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());
	}

	public static CronExpressionParser getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CronExpressionParser();
		}
		return INSTANCE;
	}

	public CronExpression getCronExpression(String expression) {
		if (expression != null) {
			try {
				expression = expression.toLowerCase();
				Calendar calendar = null;
				if (expression.contains("sunrise")) {
					calendar = _calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance(TimeZone.getDefault()));
				} else if (expression.contains("sunset")) {
					calendar = _calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance(TimeZone.getDefault()));
				}
				if (calendar != null) {
					Matcher m = NUMBER_EXTRACT_PATTERN.matcher(expression);
					if (m.find()) {
						Integer amount = Integer.valueOf(m.group());
						if (expression.contains("+")) {
							calendar.add(Calendar.SECOND, amount);
						} else if (expression.contains("-")) {
							calendar.add(Calendar.SECOND, -amount);
						}
					}
					// 0 30 21 * * ?
					expression = calendar.get(Calendar.SECOND) + " " + calendar.get(Calendar.MINUTE) + " "
							+ calendar.get(Calendar.HOUR_OF_DAY) + " * * ?";
				}
				if (CronExpression.isValidExpression(expression)) {
					return new CronExpression(expression);
				}
			} catch (ParseException e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}

		return null;
	}
}
