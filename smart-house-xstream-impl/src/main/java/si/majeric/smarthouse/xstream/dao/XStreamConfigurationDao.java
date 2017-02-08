package si.majeric.smarthouse.xstream.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.House;
import si.majeric.smarthouse.xstream.XStreamSupport;

public class XStreamConfigurationDao implements ConfigurationDao {
	private static final Logger logger = LoggerFactory.getLogger(XStreamConfigurationDao.class);

	public XStreamConfigurationDao() {
	}

	@Override
	public Configuration loadDefault() {
		return findById(Environment.getConfigModelFile());
	}

	@Override
	public Configuration findFor(House house) {
		return findById(house != null ? house.getId() : null);
	}

	@Override
	public Configuration findById(String fileName) {
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);
			final Configuration deserialized = new XStreamSupport().deserialize(is);
			return deserialized;
		} catch (Exception e) {
			throw new SmartHouseConfigReadError(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	@Override
	public Configuration save(Configuration conf) {
		try {
			final String confXml = new XStreamSupport().serialize(conf);
			logger.info(confXml);
			return conf;
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return null;
	}

	@Override
	public int remove(Configuration entity) {
		return 0;
	}

	@Override
	public long count() {
		return 1;
	}

	@Override
	public List<Configuration> findAll() {
		ArrayList<Configuration> list = new ArrayList<Configuration>();
		list.add(findById(Environment.getConfigModelFile()));
		return list;
	}

}
