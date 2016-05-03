package test.core.sqlhandler;

import java.lang.reflect.Field;

import org.dc.jdbc.core.ContextHandle;
import org.dc.jdbc.helper.DBHelper;
import org.junit.Test;
import test.Configure;

/**
 * Created by wyx on 2016/5/3.
 */
public class TestPrintSqlLogHandler {

    private static DBHelper testDBHelper = new DBHelper(Configure.testSource);

    @Test
    public void testReplaceParamTag() throws Exception {
        Field contextHandlerField = DBHelper.class.getDeclaredField("contextHandler");
        contextHandlerField.setAccessible(true);
        ContextHandle contextHandle = (ContextHandle) contextHandlerField.get(testDBHelper);
        contextHandle.handleRequest("select * from user where user = ? and pass = ?", new Object[]{"aaa", "bbb"});
        contextHandle.handleRequest("select * from user where user = ? and pass = ?", new Object[]{"???", "???"});
    }
}
