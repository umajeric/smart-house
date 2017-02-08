package si.majeric.smarthouse.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;

//@Entity
//@Table(name = "address")
public class House extends EntityBase {
	private static final long serialVersionUID = 1L;

	private List<HouseAccess> accesses = new ArrayList<HouseAccess>();
	private Configuration configuration;

	public House() {
	}

	public List<HouseAccess> getAccesses() {
		return accesses;
	}

	public void setAccesses(List<HouseAccess> access) {
		this.accesses = access;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String toString() {
		return House.class.getSimpleName() + "(access: " + accesses + ")";
	}
}
