package org.dc.jdbc.core.operate;

import java.sql.Connection;

import org.dc.jdbc.core.base.OperSuper;

/**
 * 删除操作
 * @author dc
 * @time 2015-8-17
 */
public class DeleteOper extends OperSuper{
    private static DeleteOper deleteOper = new DeleteOper();
    public static DeleteOper getInstance(){
        return deleteOper;
    }
	public  int delete(Connection conn,String sql,Object[] params) throws Exception{
		return super.preparedAndExcuteSQL(conn, sql, params);
	}
}
