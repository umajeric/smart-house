package si.majeric.smarthouse.dao;

import si.majeric.smarthouse.events.SwitchStateChangeEvent;
import si.majeric.smarthouse.model.Address.Pin;
import si.majeric.smarthouse.model.PinState;
import si.majeric.smarthouse.model.Switch;

public interface SwitchDao extends GenericDao<Switch> {

	/**
	 * 
	 * @param swch
	 * @return state for the switch if it was saved in the DB already. PinSate.LOW otherwise.
	 */
	PinState getStateFor(Switch swch);

	int save(SwitchStateChangeEvent event);

	/**
	 * 
	 * @param providerAddress
	 * @param pin
	 * 
	 * @return
	 */
	Switch findFor(Integer providerAddress, Pin pin);
}
