package jdbc_utils;

import java.sql.Connection;

import org.dc.jdbc.utils.JdbcRequest;

public class Test {
    public static void main(String[] args) {
        Connection conn = null;
        String sql = "select * from user";
        User user = new User();

        JdbcRequest.build().setConnection(conn)
                .setPhSql(sql).setSqlParam(user).setReturnCls(User.class).doReq();
    }
}
