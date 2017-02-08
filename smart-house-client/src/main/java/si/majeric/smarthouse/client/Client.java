package si.majeric.smarthouse.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import si.majeric.smarthouse.alarm.Alarm;
import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.dao.SmartHouseDaoFactory;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.House;
import si.majeric.smarthouse.model.HouseAccess;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.tpt.Request;
import si.majeric.smarthouse.tpt.Request.RequestType;

/**
 *
 * @author uros (Oct 11, 2013)
 *
 */
public abstract class Client {
	private final SmartHouseDaoFactory _daoFactory;
	private House _house;
	private HouseAccess _selectedAccess;
	private final Map<String, PinState> _currentStates = new HashMap<String, PinState>();
	private final List<String> _serverMessages = new ArrayList<String>();

	public Client(SmartHouseDaoFactory daoFactory) {
		_daoFactory = daoFactory;
	}

	public void init() {
		_house = initHouse();
		if (_house != null && _house.getConfiguration() == null) {
			if (_house.getAccesses() != null && !_house.getAccesses().isEmpty()) {
				_selectedAccess = _house.getAccesses().get(0);
			}
			final ConfigurationDao configurationDao = _daoFactory.getConfigurationDao();
			final Configuration found = configurationDao.findFor(_house);
			_house.setConfiguration(found);
		}
	}

	protected House initHouse() {
		final String host = getHost();
		final int port = getPort();
		final String username = getUsername();
		final String password = getPassword();
		if (host != null && !"".equals(host.trim())) {
			final House house = new House();
			final List<HouseAccess> accesses = house.getAccesses();

			/* Add primary access network */
			HouseAccess houseAccess = new HouseAccess();
			houseAccess.setHost(host);
			houseAccess.setPort(port);
			houseAccess.setUsername(username);
			houseAccess.setPassword(password);
			houseAccess.setName("LAN");
			accesses.add(houseAccess);

			return house;
		}
		return null;
	}

	protected abstract String getHost();

	protected abstract int getPort();

	protected abstract String getUsername();

	protected abstract String getPassword();

	public House getHouse() {
		return _house;
	}

	public void downloadHouseConfig() throws Exception {
		final URL url = new URL("http", _selectedAccess.getHost(), _selectedAccess.getPort(), "");
		final Object object = new HttpClientUtil().sendRequest(url.toURI(), new Request(RequestType.GetConfiguration),
				_selectedAccess.getUsername(), _selectedAccess.getPassword());
		if (object instanceof Configuration) {
			_house.setConfiguration((Configuration) object);
			saveConfiguration();
		}

	}

	public synchronized void saveConfiguration() {
		final ConfigurationDao configurationDao = _daoFactory.getConfigurationDao();
		if (configurationDao != null) {
			configurationDao.save(_house.getConfiguration());
		}
	}

	public void downloadMessages() throws Exception {
		final URL url = new URL("http", _selectedAccess.getHost(), _selectedAccess.getPort(), "");
		final Object object = new HttpClientUtil().sendRequest(url.toURI(), new Request(RequestType.GetMessages),
				_selectedAccess.getUsername(), _selectedAccess.getPassword());
		if (object instanceof List) {
			_serverMessages.clear();
			for (Object item : (List<?>) object) {
				if (item instanceof String) {
					_serverMessages.add(String.valueOf(item));
				}
			}
		}
	}

	public void downloadCurrentStates() throws Exception {
		final URL url = new URL("http", _selectedAccess.getHost(), _selectedAccess.getPort(), "");
		final Object object = new HttpClientUtil().sendRequest(url.toURI(), new Request(RequestType.GetStates),
				_selectedAccess.getUsername(), _selectedAccess.getPassword());
		if (object instanceof Map) {
			_currentStates.clear();
			for (Entry<?, ?> item : ((Map<?, ?>) object).entrySet()) {
				if (item.getKey() instanceof String && item.getValue() instanceof PinState) {
					_currentStates.put((String) item.getKey(), (PinState) item.getValue());
				}
			}
		}
	}

	public void getImageResource() throws Exception {
		final URL url = new URL("http", _selectedAccess.getHost(), _selectedAccess.getPort(), "");
		final Object object = new HttpClientUtil().sendRequest(url.toURI(), new Request(RequestType.GetImageResource),
				_selectedAccess.getUsername(), _selectedAccess.getPassword());
		if (object instanceof String) {
			// TODO convert object to byte array and return it
		}
	}

	public Alarm.State getAlarmState() throws Exception {
		final URL url = new URL("http", _selectedAccess.getHost(), _selectedAccess.getPort(), "");
		final Object object = new HttpClientUtil().sendRequest(url.toURI(), new Request(RequestType.GetAlarmState),
				_selectedAccess.getUsername(), _selectedAccess.getPassword());
		if (object != null) {
			final Alarm.State state = Alarm.State.valueOf(object.toString());
			return state;
		}
		return null;
	}

	public void invokeStateChange(final TriggerConfig triggerConfig, final ResponseListener listener) {
		invokeStateChange(triggerConfig, listener, true);
	}

	public void invokeStateChange(final TriggerConfig triggerConfig, final ResponseListener listener, boolean async) {
		final Request request = new Request(RequestType.Invoke);
		request.setTrigger(triggerConfig);
		this.invokeRequest(request, listener, async);
	}

	public void invokeRequest(final Request request, final ResponseListener listener) {
		invokeRequest(request, listener, true);
	}

	public void invokeRequest(final Request request, final ResponseListener listener, boolean async) {
		Runnable r = new Runnable() {
			@Override
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					final URL url = new URL("http", _selectedAccess.getHost(), _selectedAccess.getPort(), "");

					final Object object = new HttpClientUtil().sendRequest(url.toURI(), request, _selectedAccess.getUsername(),
							_selectedAccess.getPassword());
					if (object instanceof Map && !((Map<?, ?>) object).isEmpty() && listener != null) {
						listener.stateChanged((Map<String, PinState>) object);
					}
				} catch (Exception e) {
					if (listener != null) {
						listener.errorChangingState(e);
					}
				}
			}
		};
		if (async) {
			final Thread thread = new Thread(r);
			thread.setDaemon(true);
			thread.start();
		} else {
			r.run();
		}
	}

	public void updateStates(Configuration config) {
		/* TODO Get the states from the server */
	}

	public List<String> getServerMessages() {
		return _serverMessages;
	}

	public Map<String, PinState> getCurrentStates() {
		return _currentStates;
	}

	public HouseAccess getSelectedAccess() {
		return _selectedAccess;
	}

	public void setSelectedAccess(HouseAccess selectedAccess) {
		_selectedAccess = selectedAccess;
	}

}
