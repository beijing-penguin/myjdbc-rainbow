package org.dc.jdbc.core;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 连接管理
 * 
 * @author dc
 * @time 2015-8-17
 */
public class ConnectionManager {
	private static final Log LOG = LogFactory.getLog(ConnectionManager.class);

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
		}
		// 设置事务
		conn.setAutoCommit(!sqlContext.getTransaction());
		conn.setReadOnly(sqlContext.getReadOnly());
		return conn;
	}

	/**
	 * 关闭当前操作中的所有连接对象，如果关闭失败，则继续关闭其他conn对象，直到关闭所有连接，改方法属于最后一步的操作，除非线程挂掉或者被kill掉，否则最后一定要被执行。
	 * 异常conn选择捕获，而不选择抛出去，原因：保证当前线程所有的conn能尽可能快速的被放回连接池。异常处理主要由close方法实现类中的代码去处理失败的conn，本方法不做处理，所以默认直接成功
	 */
	public static void closeConnectionAll() {
		Map<DataSource, Connection> connMap = SqlContext.getContext().getDataSourceMap();
		for (Connection conn : connMap.values()) {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close();
					conn = null;
				}
			} catch (Exception e) {
				LOG.error("closeConnectionAll fail", e);
			}
		}
		//销毁
		SqlContext.getContext().destroySqlContext();
	}

	/**
	 * 回滚所有数据源的操作，正常的数据库能够回滚，回滚异常也不用管，继续回滚下一个数据库，直到回滚操作结束
	 */
	public static void rollbackAll() {
		Map<DataSource, Connection> connMap = SqlContext.getContext().getDataSourceMap();
		for (Connection conn : connMap.values()) {
			try {
				if (conn != null && !conn.isClosed() && conn.getAutoCommit() == false) {
					conn.rollback();
				}
			} catch (Exception e) {
				LOG.error("rollbackAll fail", e);
			}
		}
	}

	/**
	 * 保证正常的数据的数据能提交成功，否则直接回滚
	 * 
	 * @throws Exception
	 */
	public static void commitAll() throws Exception {
		Map<DataSource, Connection> connMap = SqlContext.getContext().getDataSourceMap();
		for (Connection conn : connMap.values()) {
			try {
				if (conn != null && !conn.isClosed() && conn.getAutoCommit() == false) {
					conn.commit();
				}
			} catch (Exception e) {
				throw e;
			}
		}
	}
}
