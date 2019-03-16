package org.dc.jdbc.core.pojo;

import java.util.List;

public class DoSql {
	
	private String sql;
    private List<Object> paramList;
    private SqlType sqlType;
    
	public SqlType getSqlType() {
		return sqlType;
	}
	public void setSqlType(SqlType sqlType) {
		this.sqlType = sqlType;
	}
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public List<Object> getParamList() {
		return paramList;
	}
	public void setParamList(List<Object> paramList) {
		this.paramList = paramList;
	}
    
    
}
