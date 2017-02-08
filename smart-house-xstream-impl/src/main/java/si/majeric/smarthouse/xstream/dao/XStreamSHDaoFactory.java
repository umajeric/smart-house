package si.majeric.smarthouse.xstream.dao;

import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.dao.HouseDao;
import si.majeric.smarthouse.dao.SmartHouseDaoFactory;
import si.majeric.smarthouse.dao.SwitchDao;

public class XStreamSHDaoFactory implements SmartHouseDaoFactory {
	public HouseDao getHouseDao() {
		throw new UnsupportedOperationException("House is not yet XStream persistent. Implement necessary classes to make it work.");
	}

	@Override
	public ConfigurationDao getConfigurationDao() {
		return new XStreamConfigurationDao();
	}

	@Override
	public SwitchDao getSwitchDao() {
		throw new UnsupportedOperationException("Switch is not yet XStream persistent. Implement necessary classes to make it work.");
	}
}
