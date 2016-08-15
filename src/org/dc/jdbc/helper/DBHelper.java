package org.dc.jdbc.helper;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.operate.DataBaseDaoImp;
import org.dc.jdbc.core.operate.IDataBaseDao;
import org.dc.jdbc.core.proxy.DataBaseOperateProxy;
import org.dc.jdbc.entity.SqlContext;

import redis.clients.jedis.JedisPool;

/**
 * 数据持久化操作类
 * sql执行三部曲：1，sql解析，2，获得数据库连接，3，执行核心jdbc操作。
 * @author dc
 * @time 2015-8-17
 */
public class DBHelper {
	private volatile DataSource dataSource;
	private static final IDataBaseDao dataBaseDao = (IDataBaseDao) new DataBaseOperateProxy(new DataBaseDaoImp()).getProxy();
	public DBHelper(DataSource dataSource){
		this.dataSource = dataSource;
	}
	public DBHelper(DataSource dataSource,JedisPool jedisPool){
		this.dataSource = dataSource;
	}
	public <T> T selectOne(String sqlOrID,Class<? extends T> returnClass,Object...params) throws Exception{
		String sql = sqlOrID.startsWith("$")?SqlContext.getSourceSql(sqlOrID):sqlOrID;
		Connection conn = ConnectionManager.getConnection(dataSource);
		return dataBaseDao.selectOne(conn, sql, returnClass, params);
	}
	public Map<String,Object> selectOne(String sqlOrID,Object...params) throws Exception{
		return this.selectOne(sqlOrID, null,params);
	}
	public <T> List<T> selectList(String sqlOrID,Class<? extends T> returnClass,Object...params) throws Exception{
		String sql = sqlOrID.startsWith("$")?SqlContext.getSourceSql(sqlOrID):sqlOrID;
		Connection conn = ConnectionManager.getConnection(dataSource);
		return dataBaseDao.selectList(conn, sql, returnClass, params);
	}
	public List<Map<String,Object>> selectList(String sqlOrID,Object...params) throws Exception{
		return this.selectList(sqlOrID, null, params);
	}
	/**
	 * 返回受影响的行数
	 * @param sqlOrID
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int insert(String sqlOrID,Object...params) throws Exception{
		String sql = sqlOrID.startsWith("$")?SqlContext.getSourceSql(sqlOrID):sqlOrID;
		Connection conn = ConnectionManager.getConnection(dataSource);
		return dataBaseDao.insert(conn, sql, params);
	}
	/**
	 * 插入数据并返回主键
	 * @param sqlOrID
	 * @param params
	 * @return 返回值有一个为基本类型，为多个就是List集合类型
	 * @throws Exception
	 */
	public Object insertReturnKey(String sqlOrID,Object...params) throws Exception{
		String sql = sqlOrID.startsWith("$")?SqlContext.getSourceSql(sqlOrID):sqlOrID;
		Connection conn = ConnectionManager.getConnection(dataSource);
		return dataBaseDao.insertRtnPKKey(conn, sql, params);
	}

	public int update(String sqlOrID,Object...params) throws Exception{
		String sql = sqlOrID.startsWith("$")?SqlContext.getSourceSql(sqlOrID):sqlOrID;
		Connection conn = ConnectionManager.getConnection(dataSource);
		return dataBaseDao.update(conn, sql, params);
	}


	public int delete(String sqlOrID,Object...params) throws Exception{
		String sql = sqlOrID.startsWith("$")?SqlContext.getSourceSql(sqlOrID):sqlOrID;
		Connection conn = ConnectionManager.getConnection(dataSource);
		return dataBaseDao.delete(conn, sql, params);
	}
	/**
	 * 回滚之前所有操作
	 * @throws Exception
	 */
	public void rollback() throws Exception{
		ConnectionManager.rollback(dataSource);
	}
}
