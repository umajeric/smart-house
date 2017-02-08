package si.majeric.smarthouse.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class Address {
	public static final String PIN_NAME_PREFIX = "GPIO ";

	public static enum Pin {
		A0, A1, A2, A3, A4, A5, A6, A7, B0, B1, B2, B3, B4, B5, B6, B7;
	}

	private int providerAddress = -1;
	private Pin pin;

	@Column(name = "provider_address")
	public int getProviderAddress() {
		return providerAddress;
	}

	public void setProviderAddress(int providerAddress) {
		this.providerAddress = providerAddress;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "pin")
	public Pin getPin() {
		return pin;
	}

	public void setPin(Pin pin) {
		this.pin = pin;
	}

	@Override
	public String toString() {
		return Address.class.getSimpleName() + "<" + providerAddress + " " + pin + ">";
	}

}
