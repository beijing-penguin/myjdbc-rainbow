package com.yanxiu.jdbc.core.sqlhandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.yanxiu.jdbc.sqlparse.Lexer;
import com.yanxiu.jdbc.sqlparse.Token;


public class PrintSqlLogHandler{
	private static final Log LOG = LogFactory.getLog(PrintSqlLogHandler.class);

	private static final PrintSqlLogHandler oper = new PrintSqlLogHandler();
	public static PrintSqlLogHandler getInstance(){
		return oper;
	}
	/**
	 * 处理方法，调用此方法处理请求
	 */
	public void handleRequest(String doSql,Object[] params) throws Exception{
		try{
			StringBuilder sbsql = new StringBuilder(doSql);
			if(params.length>0){
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

						Object value = params[index];
						if(value!=null && value instanceof String){
							value = "\""+value+"\"";
							sbsql.replace(curpos+lastCharLen-1, curpos+lastCharLen, String.valueOf(value));
						}else{
							sbsql.replace(curpos+lastCharLen-1, curpos+lastCharLen, String.valueOf(value));
						}
						lastCharLen = lastCharLen+String.valueOf(value).length()-1;
						index++;

					}
				}
			}
			LOG.info(sbsql.toString());
		}catch(Exception e){
			LOG.error("Log print error!",e);
		}
	}
}
