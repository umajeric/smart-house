package si.majeric;

import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;
import si.majeric.smarthouse.Environment;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

public class SolarCalculator extends TestCase {
	public void test() {
		// TimeZone timeZone = TimeZone.getTimeZone("Europe/Ljubljana");
		Location location = new Location(Environment.getLatitude(), Environment.getLongitude());
		SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

		String officialSunriseForDate = calculator.getOfficialSunriseForDate(Calendar.getInstance(TimeZone.getDefault()));
		System.out.println("officialSunriseForDate: " + officialSunriseForDate);

		String officialSunsetForDate = calculator.getOfficialSunsetForDate(Calendar.getInstance(TimeZone.getDefault()));
		System.out.println("officialSunsetForDate: " + officialSunsetForDate);
	}
}
