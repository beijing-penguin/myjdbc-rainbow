package org.dc.jdbc.core;

import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.entity.SqlContext;


/**
 * 连接管理
 * @author dc
 * @time 2015-8-17
 */
public class ConnectionManager {
	private static final Log LOG = LogFactory.getLog(ConnectionManager.class);

	//当前线程连接对象的参与元素
	public static void startTransaction(){
		SqlContext sqlContext = SqlContext.getContext();
		sqlContext.setTransaction(true);
	}
	public static void setReadOnly(){
		SqlContext sqlContext = SqlContext.getContext();
		sqlContext.setReadOnly(true);
	}
	public static Connection getConnection(DataSource dataSource) throws Exception{
		SqlContext sqlContext = SqlContext.getContext();
		Connection conn = sqlContext.getDataSourceMap().get(dataSource);
		if(conn==null || conn.isClosed()){
			conn = dataSource.getConnection();
			sqlContext.getDataSourceMap().put(dataSource, conn);
		}
		//设置事务
		conn.setAutoCommit(!sqlContext.getTransaction());
		conn.setReadOnly(sqlContext.getReadOnly());
		return conn;
	}

	/**
	 * 关闭当前操作中的所有连接对象，如果关闭失败，则继续关闭其他conn对象，直到关闭所有连接，改方法属于最后一步的操作，除非线程挂掉或者被kill掉，否则最后一定会被执行。
	 */
	public static void closeConnection(){
		SqlContext sqlContext = SqlContext.getContext();
		Map<DataSource,Connection> connMap = sqlContext.getDataSourceMap();
		for (Connection conn : connMap.values()) {
			try{
				if(conn!=null && !conn.isClosed()){
					conn.close();
					conn = null;
				}
			}catch (Exception e) {
				LOG.error("",e);
			}
		}
	}
	/**
	 * 回滚所有数据源的操作，正常的数据库能够回滚，回滚异常也不用管，继续回滚下一个数据库，知道回滚操作结束
	 */
	public static void rollback() {
		SqlContext sqlContext = SqlContext.getContext();
		Map<DataSource,Connection> connMap = sqlContext.getDataSourceMap();
		for (Connection conn : connMap.values()) {
			try{
				if(conn!=null && !conn.isClosed()){
					conn.rollback();
				}
			}catch (Exception e) {
				LOG.error("",e);
			}
		}
	}
	/**
	 * 回滚当前连接
	 * @param dataSource
	 * @throws Exception 
	 */
	public static void rollback(DataSource dataSource) throws Exception {
		Map<DataSource,Connection> connMap = SqlContext.getContext().getDataSourceMap();
		Connection conn = connMap.get(dataSource);
		conn.rollback();
	}
	/**
	 * 保证正常的数据的数据能提交成功，否则直接回滚，并继续执行下一个数据源的提交操作。
	 * @throws Exception 
	 */
	public static void commit() throws Exception{
		SqlContext sqlContext = SqlContext.getContext();
		Map<DataSource,Connection> connMap = sqlContext.getDataSourceMap();
		for (Connection conn : connMap.values()) {
			try{
				if(conn!=null && !conn.isClosed() && conn.getAutoCommit()==false){
					conn.commit();
				}
			}catch (Exception e) {
				throw e;
			}
		}
	}
}
