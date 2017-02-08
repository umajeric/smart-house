package si.majeric.smarthouse.dao;

import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.House;

public class ConfigurationDBDao extends GenericDBDAO<Configuration> implements ConfigurationDao {

	public ConfigurationDBDao() {
		super(Configuration.class);
	}

	@Override
	public Configuration loadDefault() {
		return findById(DEFAULT_CONFIG_ID);
	}

	@Override
	public Configuration findFor(House house) {
		throw new UnsupportedOperationException("Not yet implemented.");
	}

}
