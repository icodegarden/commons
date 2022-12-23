package io.github.icodegarden.commons.shardingsphere.properties;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DataSourceProperties {
	
	private String name;
	private String jdbcUrl;
	private String username;
	private String password;
	/**
	 * 
	 */
	private Integer minimumIdle;
	private Long idleTimeout;
	private Integer maximumPoolSize;
	private Long maxLifetime;
	private Long connectionTimeout;
	private String connectionTestQuery;
	private Long keepaliveTime;
	private Long validationTimeout;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
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

	public Integer getMinimumIdle() {
		return minimumIdle;
	}

	public void setMinimumIdle(Integer minimumIdle) {
		this.minimumIdle = minimumIdle;
	}

	public Long getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(Long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public Integer getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public void setMaximumPoolSize(Integer maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public Long getMaxLifetime() {
		return maxLifetime;
	}

	public void setMaxLifetime(Long maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	public Long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public String getConnectionTestQuery() {
		return connectionTestQuery;
	}

	public void setConnectionTestQuery(String connectionTestQuery) {
		this.connectionTestQuery = connectionTestQuery;
	}

	public Long getKeepaliveTime() {
		return keepaliveTime;
	}

	public void setKeepaliveTime(Long keepaliveTime) {
		this.keepaliveTime = keepaliveTime;
	}

	public Long getValidationTimeout() {
		return validationTimeout;
	}

	public void setValidationTimeout(Long validationTimeout) {
		this.validationTimeout = validationTimeout;
	}

	@Override
	public String toString() {
		return "Datasource [name=" + name + ", jdbcUrl=" + jdbcUrl + ", username=" + username + ", password=" + password
				+ ", minimumIdle=" + minimumIdle + ", idleTimeout=" + idleTimeout + ", maximumPoolSize="
				+ maximumPoolSize + ", maxLifetime=" + maxLifetime + ", connectionTimeout=" + connectionTimeout
				+ ", connectionTestQuery=" + connectionTestQuery + ", keepaliveTime=" + keepaliveTime
				+ ", validationTimeout=" + validationTimeout + "]";
	}

}