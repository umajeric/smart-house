package si.majeric.smarthouse.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

@Entity
@Table(name = "switch")
public class Switch extends EntityBase {
	private static final long serialVersionUID = 1L;

	public static enum SwitchType {
		SWITCH, UP, DOWN/* , INPUT, ANALOG; */
	}

	public static enum NotificationType {
		ALL, SMS, PUSH, FAILOVER_PUSH, EMAIL
	}

	private String id;
	private String name;
	private String sequence;
	private Address address;
	private PinState state;
	private SwitchType type;
	@XStreamOmitField
	private Date modified;
	@XStreamOmitField
	private NotificationType notificationType;
	private List<TriggerConfig> triggers = new ArrayList<>();

	public Switch() {
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

	@Column(name = "switch_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "switch_sequence")
	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	@Embedded
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "switch_type")
	public SwitchType getType() {
		return type;
	}

	public void setType(SwitchType type) {
		this.type = type;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modified")
	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "switch")
	public List<TriggerConfig> getTriggers() {
		return triggers;
	}

	public void setTriggers(List<TriggerConfig> triggers) {
		this.triggers = triggers;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "current_state")
	public PinState getState() {
		return state;
	}

	public void setState(PinState state) {
		this.state = state;
	}

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type")
	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	@PrePersist
	@PreUpdate
	public void prePersist() {
		this.modified = new Date();
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
		} else if (!id.equals(other.getId())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return Switch.class.getSimpleName() + "(" + getId() + (name != null ? ";" + name : "") + (address != null ? "; " + address : "")
				       + (state != null ? " " + state : "") + ")";
	}
}
