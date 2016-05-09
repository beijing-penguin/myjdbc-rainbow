package org.dc.jdbc.core.sqlhandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.entity.SqlEntity;

import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.Token;

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
		Lexer lexer = new Lexer(sbsql.toString());
		int index = 0;
		int lastCharLen = 0;
		while(true){
			lexer.nextToken();
			Token tok = lexer.token();
			if (tok == Token.EOF) {
				break;
			}
			int curpos = lexer.pos();
			if(tok == Token.QUES){
				Object value = my_params[index];
				if(value!=null && value instanceof String){
					sbsql.replace(curpos+lastCharLen-1, curpos+lastCharLen, "\""+value+"\"");
				}else{
					sbsql.replace(curpos+lastCharLen-1, curpos+lastCharLen, String.valueOf(value));
				}
				lastCharLen = lastCharLen+value.toString().length()+1;
				index++;
			}
		}
		jdbclog.info(sbsql.toString());
		/*if(my_params!=null && my_params.length>0){
			int index = 0;
			int i = 0;
			// 修复params中出现"?"字符时数组越界的错误
			while((i = sbsql.indexOf("?", i))!=-1){
				Object value = my_params[index];
				// String不可继承
				if(value!=null && value instanceof String){
					sbsql.replace(i, i+1, "\""+value+"\"");
					i += value.toString().length() + 2;
				}else{
					sbsql.replace(i, i+1, String.valueOf(value));
					i += String.valueOf(value).length();
				}
				index++;
			}
		}
		jdbclog.info(sbsql.toString());*/
		return sqlEntity;
	}
}
