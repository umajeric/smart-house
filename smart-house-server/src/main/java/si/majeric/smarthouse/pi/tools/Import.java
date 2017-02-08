package si.majeric.smarthouse.pi.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.dao.DBSHDaoFactory;
import si.majeric.smarthouse.dao.SmartHouseDaoFactory;
import si.majeric.smarthouse.dao.SwitchDao;
import si.majeric.smarthouse.excel.dao.ExcelSHDaoFactory;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.xstream.dao.SmartHouseConfigReadError;
import si.majeric.smarthouse.xstream.dao.XStreamSHDaoFactory;

/**
 *
 * @author uros
 *
 */
public class Import {
	private static final Logger logger = LoggerFactory.getLogger(Import.class);

	private final DBSHDaoFactory _persistenceDaoFactory;

	public Import(DBSHDaoFactory persistenceDaoFactory) {
		_persistenceDaoFactory = persistenceDaoFactory;
	}

	/**
	 * TODO enable importing configurations with different IDs
	 */
	public final void doImport(String fileName) throws SmartHouseConfigReadError {
		final SmartHouseDaoFactory xstreamDaoFactory;
		if (fileName != null && fileName.endsWith(".xlsx")) {
			xstreamDaoFactory = new ExcelSHDaoFactory();
		} else {
			xstreamDaoFactory = new XStreamSHDaoFactory();
		}
		final ConfigurationDao configurationDao = xstreamDaoFactory.getConfigurationDao();
		Configuration configuration = configurationDao.findById(fileName);

		if (configuration != null) {
			if (configuration.getId() == null) {
				configuration.setId(ConfigurationDao.DEFAULT_CONFIG_ID);
			}

			SwitchDao switchDao = _persistenceDaoFactory.getSwitchDao();
			/* preveri na posameznem switchu, če že imamo kakšno stanje za njega in ga nastavi preden ga shranimo */
			for (Floor floor : configuration.getFloors()) {
				for (Room room : floor.getRooms()) {
					for (Switch swtch : room.getSwitches()) {
						PinState currentState = switchDao.getStateFor(swtch);
						swtch.setState(currentState);
					}
				}
			}
			ConfigurationDao dbConfDao = _persistenceDaoFactory.getConfigurationDao();
			dbConfDao.save(configuration);

			logger.info("Import succeeded!");
		} else {
			logger.info("Configuration could not be read.");
		}
	}
}
