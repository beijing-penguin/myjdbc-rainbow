package org.dc.jdbc.entity;

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

public class SqlEntity implements Serializable{
	private static final long serialVersionUID = -7425527965344197867L;
	private String sql;
    private Object[] params;
    private Set<String> tables;
    private boolean transaction;
    private boolean readOnly;
    private Map<DataSource,Connection> dataSourceMap = new HashMap<DataSource,Connection>();
    
    
	public Map<DataSource, Connection> getDataSourceMap() {
		return dataSourceMap;
	}
	public void setDataSourceMap(Map<DataSource, Connection> dataSourceMap) {
		this.dataSourceMap = dataSourceMap;
	}
	public boolean getTransaction() {
		return transaction;
	}
	public void setTransaction(boolean transaction) {
		this.transaction = transaction;
	}
	public String getSql() {
        return sql;
    }
    public void setSql(String sql) {
        this.sql = sql;
    }
    public Object[] getParams() {
        return params;
    }
    public void setParams(Object[] params) {
        this.params = params;
    }
	public boolean getReadOnly() {
		return readOnly;
	}
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	public Set<String> getTables() {
		return tables;
	}
	public void setTables(Set<String> tables) {
		this.tables = tables;
	}
}
