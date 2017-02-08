package si.majeric.smarthouse.client;

import java.util.Map;

import si.majeric.smarthouse.model.PinState;

/**
 * 
 * @author uros
 * 
 */
public interface ResponseListener {
	void stateChanged(Map<String, PinState> swtch);

	void errorChangingState(Throwable t);
}
