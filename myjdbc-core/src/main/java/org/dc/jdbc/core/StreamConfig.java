package org.dc.jdbc.core;

import javax.sql.DataSource;

import org.dc.jdbc.core.pojo.DBType;


public class StreamConfig {

	private DataSource dataSource;

	private DBType dbtype;

	private boolean transaction;
	
	private boolean readOnly;
	
	
	public boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public DBType getDbtype() throws Exception {
		return dbtype;
	}

	public void setDbtype(DBType dbtype) {
		this.dbtype = dbtype;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public boolean getTransaction() {
		return transaction;
	}

	public void setTransaction(boolean transaction) {
		this.transaction = transaction;
	}
	
}
