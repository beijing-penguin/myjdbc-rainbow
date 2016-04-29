package org.dc.jdbc.core;

import org.dc.jdbc.core.base.JdbcSuper;
import org.dc.jdbc.core.inter.InitHandler;
import org.dc.jdbc.core.inter.TypeFactory;
import org.dc.jdbc.core.sqlhandler.SQLHandler;
import org.dc.jdbc.entity.SqlEntity;

public class ContextHandle {
	private SQLHandler firsthandle = null;
	/*public  SQLHandler registerSQLHandle(SQLHandler...handles){
		int len = handles.length;
		for (int i = len; i > 0; i--) {
			if(i>1){
				SQLHandler handle_one = handles[i-1];
				SQLHandler handle_two = handles[i-2];
				handle_one.setSuccessor(handle_two);
			}
		}
		firsthandle = handles[handles.length-1];
		return firsthandle;
	}*/
	public  void registerSQLHandle(SQLHandler handle){
			handle.setSuccessor(firsthandle);
			firsthandle = handle;
	}
	public  void registerInit(InitHandler...initHandlers){
		for (int i = 0; i < initHandlers.length; i++) {
			try {
				initHandlers[i].init();
			} catch (Exception e) {
				e.printStackTrace();
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
