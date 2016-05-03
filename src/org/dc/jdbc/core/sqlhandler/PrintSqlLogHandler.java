package org.dc.jdbc.core.sqlhandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.entity.SqlEntity;

public class PrintSqlLogHandler extends SQLHandler{
	private static final Log jdbclog = LogFactory.getLog(PrintSqlLogHandler.class);

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

		StringBuilder sbsql = new StringBuilder(sqlEntity.getSql());
		Object[] my_params = sqlEntity.getParams();
		if(my_params!=null && my_params.length>0){
			int index = 0;
			int i = 0;
			while((i = sbsql.indexOf("?"))!=-1){
				Object value = my_params[index];
				if(value!=null && value.getClass().isAssignableFrom(String.class)){
					sbsql.replace(i, i+1, "\""+value+"\"");
				}else{
					sbsql.replace(i, i+1, String.valueOf(value));
				}
				index++;
			}
		}
		jdbclog.info(sbsql.toString());
		return sqlEntity;
	}
}
