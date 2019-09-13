package org.dc.jdbc.core.sqlhandler;

import org.dc.jdbc.sqlparse.Lexer;
import org.dc.jdbc.sqlparse.Token;
import java.util.logging.Logger;

public class PrintSqlLogHandler {
	private static  Logger LOG =Logger.getLogger("PrintSqlLogHandler");
	/**
	 * 处理方法，调用此方法处理请求
	 */
	public static void handleRequest(String doSql, Object[] params) throws Exception {
		StringBuilder sbsql = new StringBuilder(doSql);
		if (params.length > 0) {
			Lexer lexer = new Lexer(sbsql.toString());
			int index = 0;
			int lastCharLen = 0;
			while (true) {
				lexer.nextToken();
				Token tok = lexer.token();
				if (tok == Token.EOF) {
					break;
				}
				int curpos = lexer.pos();
				if (tok == Token.QUES) {

					Object value = params[index];
					if (value != null && value instanceof String) {
						value = "\"" + value + "\"";
						sbsql.replace(curpos + lastCharLen - 1, curpos + lastCharLen, String.valueOf(value));
					} else {
						sbsql.replace(curpos + lastCharLen - 1, curpos + lastCharLen, String.valueOf(value));
					}
					lastCharLen = lastCharLen + String.valueOf(value).length() - 1;
					index++;

				}
			}
		}
		LOG.info(sbsql.toString());
	}
}
