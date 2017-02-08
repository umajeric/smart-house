package si.majeric.smarthouse.excel.dao;

import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.dao.HouseDao;
import si.majeric.smarthouse.dao.SmartHouseDaoFactory;
import si.majeric.smarthouse.dao.SwitchDao;

/**
 *
 * @author Uros Majeric
 *
 */
public class ExcelSHDaoFactory implements SmartHouseDaoFactory {
	@Override
	public HouseDao getHouseDao() {
		throw new UnsupportedOperationException("House is not yet Excel persistent. Implement necessary classes to make it work.");
	}

	@Override
	public ConfigurationDao getConfigurationDao() {
		return new ExcelConfigurationDao();
	}

	@Override
	public SwitchDao getSwitchDao() {
		throw new UnsupportedOperationException("Switch is not yet Excel persistent. Implement necessary classes to make it work.");
	}
}
