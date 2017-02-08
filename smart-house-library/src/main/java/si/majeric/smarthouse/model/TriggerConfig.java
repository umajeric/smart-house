package si.majeric.smarthouse.model;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "trigger_config")
public class TriggerConfig extends EntityBase {
	private static final long serialVersionUID = 1L;

	public static final List<PinState> ALL_STATES = Arrays.asList(PinState.allStates());

	public enum GpioTriggerType {
		BLINK, BLINK_STOP, INVERSE_SYNC, PULSE, TPULSE, SET, SYNC, TOGGLE, PUSH, SOUND, /* Callback */;
	}

	private String id;
	private String name;
	private boolean isDefault;
	private Address address;
	// private String targetPin; /* commented out for now as trigger config is always inside some switch */
	private GpioTriggerType type;
	private Long delay;
	private Long duration;
	private PinState newState;
	private String extra;
	private Cron cron;

	public TriggerConfig() {
	}

	public TriggerConfig(String id) {
		super(id);
	}

	/**
	 * Overriden for persistence
	 */
	@Id
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "trg_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "is_default")
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Embedded
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Column(name = "trigger_type")
	public GpioTriggerType getType() {
		return type;
	}

	public void setType(GpioTriggerType type) {
		this.type = type;
	}

	@Column(name = "delay")
	public Long getDelay() {
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	@Column(name = "duration")
	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	@Column(name = "new_state")
	@Enumerated(EnumType.STRING)
	public PinState getNewState() {
		return newState;
	}

	public void setNewState(PinState newState) {
		this.newState = newState;
	}

	@Column(name = "extra")
	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	@Embedded
	public Cron getCron() {
		return cron;
	}

	public void setCron(Cron cron) {
		this.cron = cron;
	}

	@Override
	public String toString() {
		return TriggerConfig.class.getSimpleName() + "(" + getId() + (address != null ? "; " + address : "") //
				+ (cron != null ? ("; " + cron) : "") + ")";
	}

	@Transient
	public String getHumanReadableId() {
		if (id == null) {
			return null;
		}
		return id.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"),
				" ");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityBase other = (EntityBase) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		return true;
	}
}