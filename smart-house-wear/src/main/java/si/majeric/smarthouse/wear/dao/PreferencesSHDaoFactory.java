package si.majeric.smarthouse.wear.dao;

import android.content.Context;

import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.dao.HouseDao;
import si.majeric.smarthouse.dao.SmartHouseDaoFactory;
import si.majeric.smarthouse.dao.SwitchDao;

public class PreferencesSHDaoFactory implements SmartHouseDaoFactory {
	private final Context _context;

	public PreferencesSHDaoFactory(Context context) {
		_context = context;
	}

	@Override
	public HouseDao getHouseDao() {
		throw new UnsupportedOperationException("House is not yet preferences persistent. Implement necessary classes to make it work.");
	}

	@Override
	public ConfigurationDao getConfigurationDao() {
		return new PreferencesConfigurationDao(_context);
	}

	@Override
	public SwitchDao getSwitchDao() {
		throw new UnsupportedOperationException("Switch is not yet preferences persistent. Implement necessary classes to make it work.");
	}
}
