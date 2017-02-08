package si.majeric.smarthouse.push;

import java.util.Map;

/**
 * 
 * @author Uros Majeric
 *
 */
public abstract class AbstractPush {
	private static AbstractPush _pushImpl;
	
	public static AbstractPush getDefaultImpl() {
		if (_pushImpl == null) {
			_pushImpl = new OneSignal();
		}
		return _pushImpl;
	}
	
	public abstract void sendPush(String text, Map<String, String> data);

	public abstract void sendPush(String text);
}
