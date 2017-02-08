package si.majeric.smarthouse.model;

import java.util.EnumSet;

public enum PinState {

	LOW(0, "LOW"), HIGH(1, "HIGH");

	private final int value;
	private final String name;

	private PinState(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public boolean isHigh() {
		return (this == HIGH);
	}

	public boolean isLow() {
		return (this == LOW);
	}

	public int getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public static PinState getState(int state) {
		for (PinState item : PinState.values()) {
			if (item.getValue() == state) {
				return item;
			}
		}
		return null;
	}

	public static PinState getInverseState(PinState state) {
		return (state == HIGH ? LOW : HIGH);
	}

	public static PinState getState(boolean state) {
		if (state == true) {
			return PinState.HIGH;
		} else {
			return PinState.LOW;
		}
	}

	public static PinState[] allStates() {
		return PinState.values();
	}

	public static EnumSet<PinState> all() {
		return EnumSet.of(PinState.HIGH, PinState.LOW);
	}
}
