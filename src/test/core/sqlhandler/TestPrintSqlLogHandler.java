package test.core.sqlhandler;

import org.dc.jdbc.core.sqlhandler.PrintSqlLogHandler;
import org.junit.Test;

/**
 * Created by wyx on 2016/5/3.
 */
public class TestPrintSqlLogHandler {
    @Test
    public void testReplaceParamTag() throws Exception {
    	PrintSqlLogHandler.getInstance().handleRequest("select * from user where user = ? and pass = ?", new Object[]{"aaa", "bbb"});
    	PrintSqlLogHandler.getInstance().handleRequest("select * from user where user = ? and pass = ?", new Object[]{"???", "???"});
    }
}
