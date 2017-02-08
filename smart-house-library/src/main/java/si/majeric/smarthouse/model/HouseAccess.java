package si.majeric.smarthouse.model;

import java.io.Serializable;

public class HouseAccess implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String host;
	private Integer port;
	private String username;
	private String password;

	public HouseAccess() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return HouseAccess.class.getSimpleName() + "(name: " + name + ", host: " + host + ":" + port + ")";
	}
}
