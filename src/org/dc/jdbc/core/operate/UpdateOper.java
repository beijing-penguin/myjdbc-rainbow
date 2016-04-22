package org.dc.jdbc.core.operate;

import java.sql.Connection;

import org.dc.jdbc.core.base.OperSuper;
/**
 * 更新操作
 * @author dc
 * @time 2015-8-17
 */
public class UpdateOper extends OperSuper{
    private static final UpdateOper oper = new UpdateOper();
    public static UpdateOper getInstance(){
        return oper;
    }
	public int update(Connection conn,String sql,Object[] params) throws Exception{
		return super.preparedAndExcuteSQL(conn, sql, params);
	}
}
