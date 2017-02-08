package si.majeric.smarthouse.dao;

public class DBSHDaoFactory implements SmartHouseDaoFactory {
	public HouseDao getHouseDao() {
		throw new UnsupportedOperationException("House is not yet DB persistent. Implement necessary classes to make it work.");
	}

	@Override
	public ConfigurationDao getConfigurationDao() {
		return new ConfigurationDBDao();
	}

	@Override
	public SwitchDao getSwitchDao() {
		return new SwitchDBDao();
	}
}
