package test.transaction;

import org.dc.jdbc.anno.Transactional;
import org.dc.jdbc.helper.DBHelper;
import test.Configure;
import test.User;

import java.util.Map;

/**
 * Created by wyx on 2016/4/25.
 */
@Transactional(readonly = true)
public class UserServiceWithClassAnnotation {
    private DBHelper userDBHelper = new DBHelper(Configure.testSource);

    @Transactional(readonly = true)
    public Map<String, Object> login() throws Exception {
        return userDBHelper.selectOne("$user.getOneUser");
    }
    public User login2() throws Exception {
        return userDBHelper.selectOne("select * from user limit 1",User.class);
    }

    public boolean register() throws Exception {
        int ret = userDBHelper.insert("insert into user(name,age) values(?,?)", "dc", 12);
        return ret > 0;
    }
}
