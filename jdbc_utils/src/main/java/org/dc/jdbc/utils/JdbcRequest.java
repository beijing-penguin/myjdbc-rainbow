package org.dc.jdbc.utils;

import java.sql.Connection;

public class JdbcRequest {
	
	private Connection connection;
	private String sql;
	/**
	 * 存在占位符的sql
	 */
	private String phSql;
	private Object sqlParam;
	private Class<?> returnCls;
	
	public static JdbcRequest build() {
		return new JdbcRequest();
	}
	
	public void doReq() {
		
	}
	
	public JdbcRequest send(JdbcReqParam jdbcReqParam) {
		return null;
	}
	
	public Connection getConnection() {
		return connection;
	}

	public JdbcRequest setConnection(Connection connection) {
		this.connection = connection;
		return this;
	}

	public String getSql() {
		return sql;
	}

	public JdbcRequest setSql(String sql) {
		this.sql = sql;
		return this;
	}

	public Object getSqlParam() {
		return sqlParam;
	}

	public JdbcRequest setSqlParam(Object sqlParam) {
		this.sqlParam = sqlParam;
		return this;
	}

	public String getPhSql() {
		return phSql;
	}

	public JdbcRequest setPhSql(String phSql) {
		this.phSql = phSql;
		return this;
	}

    public Class<?> getReturnCls() {
        return returnCls;
    }

    public JdbcRequest setReturnCls(Class<?> returnCls) {
        this.returnCls = returnCls;
        return this;
    }
	
}
