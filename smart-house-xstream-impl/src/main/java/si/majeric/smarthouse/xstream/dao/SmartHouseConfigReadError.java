package si.majeric.smarthouse.xstream.dao;

public class SmartHouseConfigReadError extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SmartHouseConfigReadError(Exception e) {
		super(e);
	}

}
