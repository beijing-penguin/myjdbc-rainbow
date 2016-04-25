package org.dc.jdbc.core.sqlhandler;

import org.dc.jdbc.config.JDBCConfig;
import org.dc.jdbc.entity.SqlEntity;

public class PrintSqlLogHandler extends SQLHandler{
	private static PrintSqlLogHandler sqlHandler = new PrintSqlLogHandler();
	public static PrintSqlLogHandler getInstance(){
		return sqlHandler;
	}
	/**
	 * 处理方法，调用此方法处理请求
	 */
	@Override
	public SqlEntity handleRequest(String sqlOrID,Object[] params) throws Exception{
		//通过责任链得到之前的责任人处理好的SqlEntity
		SqlEntity sqlEntity = super.getSuccessor().handleRequest(sqlOrID,params);
		if(JDBCConfig.isPrintSqlLog){
			super.printSqlLog(sqlEntity.getSql(), sqlEntity.getParams());
		}
		return sqlEntity;
	}
}
