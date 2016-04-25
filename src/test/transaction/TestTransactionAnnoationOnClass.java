package test.transaction;

import org.dc.jdbc.core.JDBCProxy;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

/**
 * 测试类上的事务注解
 * Created by wyx on 2016/4/25.
 */
public class TestTransactionAnnoationOnClass {
    private static UserServiceWithClassAnnotation userServiceWithClassAnnotation;

    @BeforeClass
    public static void init() {
        userServiceWithClassAnnotation=JDBCProxy.getInstance().getTarget(UserServiceWithClassAnnotation.class);
    }

    @Test
    public void testSelectWithReadonlyTransaction() throws Exception {
        Map<String, Object> userMap = userServiceWithClassAnnotation.login();
        Assert.assertNotNull(userMap);
    }


    @Test(expected = Exception.class)
    public void testInsertWithReadonlyTransaction() throws Exception {
        userServiceWithClassAnnotation.register();
    }
}
