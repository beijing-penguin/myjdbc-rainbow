package org.dc.jdbc.core;

import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;

/**
 * 连接管理
 * 
 * @author dc
 * @time 2015-8-17
 */
public class ConnectionManager {
    // 设置事务
    public static void setTransaction(boolean startTransaction) {
        SqlContext.getContext().setTransaction(startTransaction);
    }

    // 设置只读
    public static void setReadOnly(boolean readOnly) {
        SqlContext.getContext().setReadOnly(readOnly);
    }

    public static Connection getConnection(DataSource dataSource) throws Exception {
        SqlContext sqlContext = SqlContext.getContext();
        Connection conn = sqlContext.getDataSourceMap().get(dataSource);
        if (conn == null || conn.isClosed()) {
            conn = dataSource.getConnection();
            sqlContext.getDataSourceMap().put(dataSource, conn);
            sqlContext.setCurrentDataSource(dataSource);
        }
        // 设置事务
        conn.setAutoCommit(!sqlContext.getTransaction());
        conn.setReadOnly(sqlContext.getReadOnly());

        return conn;
    }

    public static void closeConnectionAll() throws Throwable {
        Map<DataSource, Connection> connMap = SqlContext.getContext().getDataSourceMap();
        Throwable ee = null;
        for (Connection conn : connMap.values()) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    conn = null;
                }
            } catch (Throwable e) {
            	if(ee == null) {
            		ee = e;
            	}
            }
        }
        //销毁
        SqlContext.getContext().destroySqlContext();
        if(ee!=null) {
        	throw ee;
        }
    }

    /**
     * 回滚所有数据源的操作，正常的数据库能够回滚，回滚异常也不用管，继续回滚下一个数据库，直到回滚操作结束
     * @throws Exception 
     */
    public static void rollbackAll() throws Throwable {
        Map<DataSource, Connection> connMap = SqlContext.getContext().getDataSourceMap();
        for (Connection conn : connMap.values()) {
            if (conn != null && !conn.isClosed() && conn.getAutoCommit() == false) {
                conn.rollback();
            }
        }
    }

    /**
     * 保证正常的数据的数据能提交成功，否则直接回滚
     * 
     * @throws Exception
     */
    public static void commitAll() throws Throwable {
        Map<DataSource, Connection> connMap = SqlContext.getContext().getDataSourceMap();
        for (Connection conn : connMap.values()) {
            try {
                if (conn != null && !conn.isClosed() && conn.getAutoCommit() == false) {
                    conn.commit();
                }
            } catch (Throwable e) {
                throw e;
            }
        }
    }
}
