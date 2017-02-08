package si.majeric.smarthouse.dao.mock;

import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.dao.HouseDao;
import si.majeric.smarthouse.dao.SmartHouseDaoFactory;
import si.majeric.smarthouse.dao.SwitchDao;

public class MockSHDaoFactory implements SmartHouseDaoFactory {
	@Override
	public HouseDao getHouseDao() {
		return new MockHouseDao();
	}

	@Override
	public ConfigurationDao getConfigurationDao() {
		return null;
	}

	@Override
	public SwitchDao getSwitchDao() {
		return null;
	}
}
