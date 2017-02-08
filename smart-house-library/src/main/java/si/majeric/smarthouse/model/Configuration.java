package si.majeric.smarthouse.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "configuration")
public class Configuration extends EntityBase {
	private static final long serialVersionUID = 1L;

	private String name;
	private Long version;
	private List<Floor> floors = new ArrayList<Floor>();
	private Map<String, String> properties = new HashMap<String, String>();

	public Configuration() {
	}
	
	/**
	 * Overriden for persistence
	 */
	@Id
	public String getId() {
		return super.getId();
	}

	@Column(name = "conf_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return the version of the configuration for this house.
	 */
	@Column(name = "conf_version")
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "configuration")
	public List<Floor> getFloors() {
		return floors;
	}

	public void setFloors(List<Floor> floors) {
		this.floors = floors;
	}
	
	@Transient
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return Configuration.class.getSimpleName() + "(" + name + (version != null ? ";" + version : "") + ")";
	}
}
