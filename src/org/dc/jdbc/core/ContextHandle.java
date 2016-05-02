package org.dc.jdbc.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.core.base.JdbcSuper;
import org.dc.jdbc.core.inter.InitHandler;
import org.dc.jdbc.core.inter.TypeFactory;
import org.dc.jdbc.core.sqlhandler.SQLHandler;
import org.dc.jdbc.entity.SqlEntity;

public class ContextHandle {
	private static final Log jdbclog = LogFactory.getLog(ContextHandle.class);
	private volatile SQLHandler firsthandle = null;
	public  void registerSQLHandle(SQLHandler handle){
			handle.setSuccessor(firsthandle);
			firsthandle = handle;
	}
	public  void registerInit(InitHandler...initHandlers){
		for (int i = 0; i < initHandlers.length; i++) {
			try {
				initHandlers[i].init();
			} catch (Exception e) {
				jdbclog.error("SQL initialization exception",e);
			}
		}
	}
	public SqlEntity handleRequest(String sqlOrID, Object[] params) throws Exception {
		return firsthandle.handleRequest(sqlOrID, params);
	}
	public void registerTypeChange(TypeFactory typeFactory) {
		JdbcSuper.typeFactory = typeFactory;
	}
}
