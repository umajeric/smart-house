package si.majeric.smarthouse.dao;

/**
 * 
 * @author uros
 *
 */
public interface SmartHouseDaoFactory {
	HouseDao getHouseDao();

	ConfigurationDao getConfigurationDao();

	SwitchDao getSwitchDao();
}
