package si.majeric.smarthouse.pi;

import static si.majeric.smarthouse.model.Address.PIN_NAME_PREFIX;
import static si.majeric.smarthouse.pi.gpio.GpioTriggerFactory.TRIGGER_STATES_LIST;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.databox.currency.Currency;
import si.majeric.pi4j.io.gpio.trigger.OutputTargetedGpioTrigger;
import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.SmartHouse;
import si.majeric.smarthouse.alarm.Alarm;
import si.majeric.smarthouse.alarm.KnownHostsChecker;
import si.majeric.smarthouse.alarm.MotionDetectionSnapWatch;
import si.majeric.smarthouse.alarm.listeners.CameraEmailAlarmListener;
import si.majeric.smarthouse.alarm.listeners.PushNotificationAlarmListener;
import si.majeric.smarthouse.camera.API;
import si.majeric.smarthouse.cron.CronTriggerScheduler;
import si.majeric.smarthouse.dao.ConfigurationDao;
import si.majeric.smarthouse.dao.SmartHouseDaoFactory;
import si.majeric.smarthouse.events.SwitchStateChangeEventConsumer;
import si.majeric.smarthouse.exception.TriggerNotConfiguredException;
import si.majeric.smarthouse.model.Address;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.Switch.NotificationType;
import si.majeric.smarthouse.model.TriggerConfig;
import si.majeric.smarthouse.notification.impl.PushNotification;
import si.majeric.smarthouse.notification.impl.SMSNotification;
import si.majeric.smarthouse.pi.gpio.ExtendedMCP23017GpioProvider;
import si.majeric.smarthouse.pi.gpio.GPIOProviderMultiton;
import si.majeric.smarthouse.pi.gpio.GpioTriggerFactory;

import com.pi4j.gpio.extension.mcp.MCP23017Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.system.SystemInfo;

/**
 *
 * uros@m05:~/Dropbox/workspace/smart-house$ mvn clean install -Dmaven.test.skip=true -o -pl smart-house-server && scp
 * smart-house-server/target/smart-house-server-1.0.0-SNAPSHOT.jar pi@192.168.1.210:smart-house/smart-house-server-1.0.0.jar
 *
 * scp smart-house-server/src/test/resources/smart-house-model.xml pi@192.168.1.210:smart-house/smart-house-model.xml
 *
 * @author uros (Oct 19, 2013)
 *
 */
public class PiSmartHouse implements SmartHouse {
	static final Logger logger = LoggerFactory.getLogger(PiSmartHouse.class);

	private Configuration _configuration;
	/**
	 * Map that contains all triggers. We use it to search for correct trigger when invoking it.
	 */
	private final Map<String, OutputTargetedGpioTrigger> _triggerMap = new HashMap<String, OutputTargetedGpioTrigger>();

	private final SmartHouseDaoFactory _persistenceDaoFactory;

	public PiSmartHouse(SmartHouseDaoFactory daoFactory) {
		_persistenceDaoFactory = daoFactory;
	}

	@Override
	public void init() {
		final ConfigurationDao configurationDao = _persistenceDaoFactory.getConfigurationDao();
		_configuration = configurationDao.loadDefault();

		/* Start the saver daemon thread */
		SwitchStateChangeEventConsumer changeEventConsumer = new SwitchStateChangeEventConsumer(_persistenceDaoFactory.getSwitchDao());
		// changeEventConsumer.registerNotification(NotificationType.SMS, new SMSNotification());
		changeEventConsumer.registerNotification(NotificationType.PUSH, new PushNotification());
		Thread switchStateSaverThread = new Thread(changeEventConsumer);
		switchStateSaverThread.setDaemon(true);
		switchStateSaverThread.start();

		final Map<String, String> cam1Conf = Environment.getPropertyMap("cam1");
		if (Boolean.valueOf(cam1Conf.get("enabled"))) {
			API cameraAPI = API.instance().init(cam1Conf);
			Alarm.getInstance().addChangeListener(new PushNotificationAlarmListener());
			Alarm.getInstance().addChangeListener(new CameraEmailAlarmListener(cameraAPI));
		}

		Thread knownHostsCheckerThread = new Thread(new KnownHostsChecker());
		knownHostsCheckerThread.setDaemon(true);
		knownHostsCheckerThread.start();

		final String motionDetectionPath = Environment.getProperty("motion.detection.path");
		if (motionDetectionPath != null) {
			Path folder = Paths.get(motionDetectionPath);
			Thread watcherThread = new Thread(new MotionDetectionSnapWatch(this, folder));
			watcherThread.setDaemon(true);
			watcherThread.start();
		}

		(new Thread(new Currency())).start(); // TODO move this to scheduler

		if (_configuration == null) {
			final String importCommand = "curl -X POST http://{SERVER_HOST}:{SEVER_PORT}/import{?fileName=/file/path}";
			logger.error("No configuration in Database. Please run: \n" + importCommand);
			return;
		}
		try {
			/* Raspberry is always ARM architecture */
			if ("arm".equals(SystemInfo.getOsArch())) {
				/* clear all possible currently running jobs */
				Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
				scheduler.clear();

				removeAllTriggers();

				logger.info("Initializing SMART-HOUSE for configuration: {}", _configuration);
				initPins();

				// new Thread(statesLogger).start();

				/* when pins are initialized start the scheduler for cron jobs */
				if (!scheduler.isStarted()) {
					scheduler.start();
				}
			} else {
				logger.error("Cannot initialize SMART-HOUSE for this server.");
			}
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public Configuration getConfiguration() {
		return _configuration;
	}

	private void initPins() {
		logger.info("Initializing pins...");
		final GpioController gpio = GpioFactory.getInstance();

		/* build configuration for all outputs */
		if (_configuration != null) {
			final List<Floor> floors = _configuration.getFloors();
			for (Floor floor : floors) {
				final List<Room> rooms = floor.getRooms();
				for (Room room : rooms) {
					final List<Switch> switches = room.getSwitches();
					/* iterate all switches and configure them */
					for (Switch swch : switches) {
						try {
							if (swch != null && swch.getAddress() != null && swch.getAddress().getProviderAddress() > -1) {
								logger.info("Getting GPIO provider for : " + swch.getAddress().getProviderAddress() + ", will create PIN: "
										+ swch.getAddress().getPin());
								GPIOProviderMultiton gpioProviderInstance = GPIOProviderMultiton.getInstance(swch.getAddress()
										.getProviderAddress());
								final ExtendedMCP23017GpioProvider provider = gpioProviderInstance.getProvider();

								final Pin pin = PinFactory.createPinFor(swch.getAddress().getPin());

								/* now we are able to determine the GPIO pin */
								GpioPinDigitalOutput gpioPin = gpioProviderInstance.getProvisionedDigitalOutput(pin);
								if (gpioPin == null) {
									PinState state = PinState.LOW;
									if (swch.getState() != null) {
										state = PinState.valueOf(swch.getState().toString());
									}
									logger.info("gpioPin {} ------> initial state: {}", gpioPin, state);
									gpioPin = gpio.provisionDigitalOutputPin(provider, pin, swch.getId(), state);
								}
								if (gpioPin != null) {
									/* lastly configure triggers for outputs */
									createTriggersFor(gpioPin, swch);
								} else {
									logger.warn("Couldn't provide the GPIO Pin for " + swch);
								}
							}
						} catch (Exception e) {
							logger.error("ERROR on switch: " + swch, e);
							// logger.error(e.getLocalizedMessage(), e);
						}
					}
				}
			}
		}
	}

	protected void removeAllTriggers() {
		final GpioController gpio = GpioFactory.getInstance();
		gpio.removeAllTriggers();
		_triggerMap.clear();
	}

	/**
	 * Method builds the GpioTriggers for each TriggerConfig and sets the trigger for targetPin.
	 *
	 * @param targetPin
	 * @param swtch
	 */
	private void createTriggersFor(GpioPinDigitalOutput targetPin, final Switch swtch) {
		if (targetPin != null && swtch != null && swtch.getTriggers() != null) {
			for (TriggerConfig trg : swtch.getTriggers()) {
				try {
					if (trg != null) {
						logger.info("Creating trigger for {}", trg);
						final OutputTargetedGpioTrigger trigger = GpioTriggerFactory.createTriggerFor(trg, targetPin);
						logger.info("Trigger created for {} - {}", targetPin, targetPin.getState());

						if (trg.getAddress() != null) {
							logger.info("Will get input for {}", trg.getAddress());
							GpioPinDigitalInput input = getInputForAddress(trg.getAddress());
							if (input == null) {
								logger.warn("Input was not initialized for {} with address {}", trg, trg.getAddress());
								// continue; // uncoment if we won't allow UI only triggers
							} else {
								logger.info("Adding trigger for input: {}", input);
								input.addTrigger(trigger);
							}
						}

						/* Schedule triggers if they are configured for TriggerConfig */

						// hack (do not schedule rollers for now) - temporary solution
						CronTriggerScheduler.schedule(PiSmartHouse.this, swtch, trg);

						if (_triggerMap.containsKey(trg.getId())) {
							throw new RuntimeException("Multiple triggers with same ID: " + trg);
						}

						/* Put the trigger in the map - so it can be triggered from various sources */
						_triggerMap.put(trg.getId(), trigger);
						logger.debug("Added " + trg.getId() + " for " + trigger + " to trigger map.");
					}
				} catch (Exception e) {
					logger.error("ERROR on trigger (not initialized): " + trg, e);
					// logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	private GpioPinDigitalInput getInputForAddress(final Address address) throws IOException {
		if (address == null || address.getProviderAddress() < 0) {
			return null;
		}
		final GpioController gpio = GpioFactory.getInstance();
		final GPIOProviderMultiton inputProviderInstance = GPIOProviderMultiton.getInstance(address.getProviderAddress());
		final ExtendedMCP23017GpioProvider inputProvider = inputProviderInstance.getProvider();

		final Pin inputPin = PinFactory.createPinFor(address.getPin());

		GpioPinDigitalInput input = inputProviderInstance.getProvisionedDigitalInput(inputPin);
		if (input == null) {
			input = gpio.provisionDigitalInputPin(inputProvider, inputPin, PinPullResistance.PULL_UP);
		}
		return input;
	}

	private static class PinFactory {
		private static Pin createPinFor(si.majeric.smarthouse.model.Address.Pin address) {
			if (address != null) {
				for (Pin p : MCP23017Pin.ALL) {
					if (p.getName().equals(PIN_NAME_PREFIX + address.toString())) {
						return p;
					}
				}
			}
			return null;
		}
	}

	@Override
	public Map<String, si.majeric.smarthouse.model.PinState> invokeSwitch(Switch aSwitch) {
		if (aSwitch != null && aSwitch.getId() != null) {

			TriggerConfig providedTrigger = null;
			if (!aSwitch.getTriggers().isEmpty() && null != aSwitch.getTriggers().get(0)) {
				providedTrigger = aSwitch.getTriggers().get(0);
			}
			for (Floor floor : _configuration.getFloors()) {
				for (Room room : floor.getRooms()) {
					for (Switch swtch : room.getSwitches()) {
						if (aSwitch.getId().equals(swtch.getId())) {
							if (swtch.getTriggers() != null && !swtch.getTriggers().isEmpty()) {

								TriggerConfig trigger = swtch.getTriggers().get(0);
								// if trigger is provided then search for one matching the same type
								if (providedTrigger != null && providedTrigger.getType() != null) {
									for (TriggerConfig trg : swtch.getTriggers()) {
										if (trg.getType() == providedTrigger.getType()) {
											trigger = trg;
											break;
										}
									}
								}
								try {
									return invokeTrigger(trigger);
								} catch (Exception e) {
									logger.error(e.getLocalizedMessage(), e);
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public Map<String, si.majeric.smarthouse.model.PinState> invokeTrigger(TriggerConfig triggerConfig)
			throws TriggerNotConfiguredException, IOException {
		logger.info("invokeTrigger: " + triggerConfig);
		HashMap<String, si.majeric.smarthouse.model.PinState> map = new HashMap<>();
		if (triggerConfig == null || triggerConfig.getId() == null) {
			final IllegalArgumentException iae = new IllegalArgumentException("Trigger Config or its id not provided: " + triggerConfig);
			logger.error(iae.getLocalizedMessage(), iae);
			throw iae;
		}

		/* if we don't have any trigger states then do nothing */
		if (TRIGGER_STATES_LIST != null && !TRIGGER_STATES_LIST.isEmpty()) {
			/* find the trigger (and saver trigger) in the map */
			final OutputTargetedGpioTrigger trigger = _triggerMap.get(triggerConfig.getId());
			if (trigger == null) {
				logger.error("TriggerNotConfiguredException: " + triggerConfig.getId());
				throw new TriggerNotConfiguredException(triggerConfig.getId());
			}

			// input is null for client only configured switches
			GpioPinDigitalInput input = getInputForAddress(triggerConfig.getAddress());

			PinState pinState = TRIGGER_STATES_LIST.get(0);
			trigger.invoke(input, pinState);

			GpioPinDigitalOutput targetPin = trigger.getTargetPin();
			PinState state = targetPin.getState();

			si.majeric.smarthouse.model.PinState st = si.majeric.smarthouse.model.PinState.valueOf(state.toString());
			logger.info("Returning '{}' with state '{}'", targetPin.getName(), st);
			map.put(targetPin.getName(), st);
		}

		/* log the states of the outputs */
		// new Thread(statesLogger).start();

		/* TODO get the list of switches with states for triggerConfig */
		return map;
	}

	@Override
	public synchronized List<Switch> getProvisionedSwitches() {
		return getProvisionedSwitches(null);
	}


	public synchronized List<Switch> getProvisionedSwitches(String switchId) {
		List<Switch> allStates = new ArrayList<>();
		final List<Floor> floors = _configuration.getFloors();
		if (floors != null) {
			for (Floor floor : floors) {
				final List<Room> rooms = floor.getRooms();
				if (rooms != null) {
					for (Room room : rooms) {
						final List<Switch> switches = room.getSwitches();
						if (switches != null) {
							/* iterate all switches and configure them */
							for (Switch swch : switches) {
								if (swch != null && swch.getAddress() != null) {
									try {
										GPIOProviderMultiton gpioProviderInstance = GPIOProviderMultiton.getInstance(swch.getAddress()
												                                                                             .getProviderAddress());
										final Pin pin = PinFactory.createPinFor(swch.getAddress().getPin());

										/* now we are able to determine the GPIO pin */
										GpioPinDigitalOutput gpioPin = gpioProviderInstance.getProvisionedDigitalOutput(pin);
										// final GpioController gpio = GpioFactory.getInstance();

										if (gpioPin != null) {
											Switch sw = new Switch();
											sw.setState(si.majeric.smarthouse.model.PinState.valueOf(gpioPin.getState().toString()));
											sw.setId(swch.getId());
											sw.setTriggers(null);
											sw.setAddress(swch.getAddress());
											allStates.add(sw);

											if (switchId != null && switchId == swch.getId()) {
												return allStates;
											}
										} else {
											logger.info("PIN for address " + swch.getAddress() + " should be provided but is null.");
										}
									} catch (Exception e) {
										logger.error(e.getLocalizedMessage(), e);
									}
								}
							}
						}
					}
				}
			}
		} else {
			logger.error("No floors defined.");
		}

		return allStates;
	}

	@Override
	public Map<String, si.majeric.smarthouse.model.PinState> getProvisionedPinStates() {
		logger.info("Getting states...");
		/*
		 * TODO we should use in-memory _configuration instead of retrieving from DB here, but the states are not changed properly yet in in-memory
		 * configuration.
		 */
		Configuration config = _persistenceDaoFactory.getConfigurationDao().loadDefault();
		HashMap<String, si.majeric.smarthouse.model.PinState> states = new HashMap<String, si.majeric.smarthouse.model.PinState>();
		for (Floor floor : config.getFloors()) {
			for (Room room : floor.getRooms()) {
				for (Switch swtch : room.getSwitches()) {
					if (swtch != null && swtch.getId() != null) {
						states.put(swtch.getId(), swtch.getState());
//						logger.info("Putting '{}' with state '{}'", swtch.getId(), swtch.getState());
					}
				}
			}
		}
		return states;
	}

	@Override
	public si.majeric.smarthouse.model.PinState getProvisionedPinState(Switch swtch) {
		logger.info("Getting state for: " + swtch.getId());
		final si.majeric.smarthouse.model.PinState state = _persistenceDaoFactory.getSwitchDao().getStateFor(swtch);
		return state;
	}

	private final Runnable statesLogger = new Runnable() {
		@Override
		public void run() {
			try {
				/* just for test - remove that */
				Thread.sleep(500);
				List<Switch> allStates = getProvisionedSwitches();
				StringBuilder pinStates = new StringBuilder();
				for (Switch swtch : allStates) {
					pinStates.append(swtch).append(": ").append(swtch.getState()).append("\n");
				}
				logger.info(" All available PIN states: \n{}", pinStates);
			} catch (InterruptedException e) {
				logger.error(e.getLocalizedMessage(), e);
			} catch (Exception e) {
				logger.error(e.getLocalizedMessage(), e);
			}
		}
	};

}
