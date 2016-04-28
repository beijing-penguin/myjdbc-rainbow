package org.dc.jdbc.core.sqlhandler;

import org.dc.jdbc.entity.SqlEntity;

public class PrintSqlLogHandler extends SQLHandler{
	private static final PrintSqlLogHandler oper = new PrintSqlLogHandler();
	public static PrintSqlLogHandler getInstance(){
		return oper;
	}
	/**
	 * 处理方法，调用此方法处理请求
	 */
	@Override
	public SqlEntity handleRequest(String sqlOrID,Object[] params) throws Exception{
		//通过责任链得到之前的责任人处理好的SqlEntity
		SqlEntity sqlEntity = super.getSuccessor().handleRequest(sqlOrID,params);

		super.printSqlLog(sqlEntity.getSql(), sqlEntity.getParams());
		return sqlEntity;
	}
}
