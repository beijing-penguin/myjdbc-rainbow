package org.dc.jdbc.utils;

public class JdbcReqParam {
	
	private String sql;
	
	public static JdbcReqParam build() {
		return new JdbcReqParam();
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
	
}
