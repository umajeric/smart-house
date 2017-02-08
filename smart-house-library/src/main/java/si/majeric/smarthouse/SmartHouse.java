package si.majeric.smarthouse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import si.majeric.smarthouse.exception.TriggerNotConfiguredException;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;

/**
 * 
 * @author uros (Oct 19, 2013)
 * 
 */
public interface SmartHouse {

	Configuration getConfiguration();

	void init();

	/**
	 * One trigger can be configured on many switches. We have to return all new states for all those switches.
	 *
	 * @param triggerConfig
	 * @return switches for {@code triggerConfig} with new states.
	 * @throws TriggerNotConfiguredException
	 * @throws IOException
	 */
	Map<String, PinState> invokeTrigger(TriggerConfig triggerConfig) throws TriggerNotConfiguredException, IOException;

	/**
	 *
	 * @param aSwitch
	 *
	 * @return
     */
	Map<String,PinState> invokeSwitch(Switch aSwitch);

	/**
	 *
	 * @return All provisioned switches with current states
	 */
	@Deprecated
	List<Switch> getProvisionedSwitches();

	Map<String, PinState> getProvisionedPinStates();

	si.majeric.smarthouse.model.PinState getProvisionedPinState(Switch swtch);
}
