package org.dc.jdbc.core.entity;

import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

public class DataSourceBean {
	private DataSource dataSource;
	private boolean isUsed;
	private AtomicInteger failCount;
	
	
	public AtomicInteger getFailCount() {
		return failCount;
	}
	public void setFailCount(AtomicInteger failCount) {
		this.failCount = failCount;
	}
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
