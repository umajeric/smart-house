package si.majeric.smarthouse.dao;

import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.House;

public interface ConfigurationDao extends GenericDao<Configuration> {
	public static final String DEFAULT_CONFIG_ID = "DEFAULT-CONFIG-ID";
	
	Configuration loadDefault();

	Configuration findFor(House house);
}
