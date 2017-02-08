package si.majeric.smarthouse.wear.dao;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.House;
import si.majeric.smarthouse.xstream.XStreamSupport;

public class PreferencesConfigurationDao implements ConfigurationDao {
	private static final Logger logger = LoggerFactory.getLogger(PreferencesConfigurationDao.class);

	private static final String CONFIG_KEY = "sh.config";
	private final Context _context;

	public PreferencesConfigurationDao(Context context) {
		_context = context;
	}

	@Override
	public Configuration loadDefault() {
		return findById(null);
	}

	@Override
	public Configuration findFor(House house) {
		return findById(house.getId());
	}

	@Override
	public Configuration findById(String id) {
		String configKey = CONFIG_KEY;
		try {
			if (configKey != null && id != null) {
				configKey += "-" + id;
			}
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
			final String configXml = sharedPrefs.getString(configKey, null);
			if (configXml != null) {
				return new XStreamSupport().deserialize(configXml);
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	@Override
	public Configuration save(Configuration conf) {
		try {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_context);
			SharedPreferences.Editor prefEditor = settings.edit();
			final String serialized = new XStreamSupport().serialize(conf, true);
			prefEditor.putString(CONFIG_KEY, serialized);
			prefEditor.apply();
			return conf;
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	@Override
	public long count() {
		return 0;
	}

	@Override
	public List<Configuration> findAll() {
		return null;
	}

	@Override
	public int remove(Configuration entity) {
		return 0;
	}

}
