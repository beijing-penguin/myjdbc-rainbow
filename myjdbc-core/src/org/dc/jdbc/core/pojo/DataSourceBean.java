package org.dc.jdbc.core.pojo;

import javax.sql.DataSource;

public class DataSourceBean {
	private DataSource dataSource;
	private boolean isUsed;
	
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public boolean isUsed() {
		return isUsed;
	}
	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}
}
