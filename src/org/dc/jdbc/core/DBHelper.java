package org.dc.jdbc.core;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.core.operate.DataBaseDaoImp;
import org.dc.jdbc.core.operate.IDataBaseDao;
import org.dc.jdbc.core.proxy.DataBaseOperateProxy;

/**
 * 数据持久化操作类
 * sql执行三部曲：1，sql解析，2，获得数据库连接，3，执行核心jdbc操作。
 * @author dc
 * @time 2015-8-17
 */
public class DBHelper {
	private volatile DataSource dataSource;

	private static final Log LOG = LogFactory.getLog(DBHelper.class);
	private static final IDataBaseDao dataBaseDaoProxy = (IDataBaseDao) new DataBaseOperateProxy(new DataBaseDaoImp()).getProxy();
	public DBHelper(DataSource dataSource){
		this.dataSource = dataSource;
	}
	public <T> T selectOne(String sqlOrID,Class<? extends T> returnClass,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.selectOne(null,sqlOrID, returnClass, params);
	}
	public Map<String,Object> selectOne(String sqlOrID,Object...params) throws Exception{
		return this.selectOne(sqlOrID, null,params);
	}
	public <T> List<T> selectList(String sqlOrID,Class<? extends T> returnClass,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.selectList(null, sqlOrID, returnClass, params);
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
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.insert(null, sqlOrID,null, params);
	}
	/**
	 * 插入数据并返回主键
	 * @param sqlOrID
	 * @param params
	 * @return 返回值有一个为基本类型，为多个就是List集合类型
	 * @throws Exception
	 */
	public Object insertReturnKey(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.insertRtnPKKey(null, sqlOrID,null, params);
	}

	public int update(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.update(null, sqlOrID,null, params);
	}


	public int delete(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.delete(null, sqlOrID,null, params);
	}
	
	public int excuteSQL(String sqlOrID,Object...params) throws Exception{
		SqlContext.getContext().setCurrentDataSource(dataSource);
		return dataBaseDaoProxy.excuteSQL(null, sqlOrID,null, params);
	}
	
	/**
	 * 仅仅只回滚当前连接
	 * @throws Exception
	 */
	public void rollback() throws Exception{
		try{
			Map<DataSource,Connection> connMap = SqlContext.getContext().getDataSourceMap();
			Connection conn = connMap.get(dataSource);
			conn.rollback();
		}catch (Exception e) {
			LOG.error("",e);
		}
	}
	/**
	 * 仅仅只提交当前连接
	 * @throws Exception
	 */
	public void commit() throws Exception{
		Map<DataSource,Connection> connMap = SqlContext.getContext().getDataSourceMap();
		Connection conn = connMap.get(dataSource);
		conn.commit();
	}
	/**
	 * 仅仅只关闭当前连接
	 * @throws Exception
	 */
	public void close(){
		try{
			Map<DataSource,Connection> connMap = SqlContext.getContext().getDataSourceMap();
			Connection conn = connMap.get(dataSource);
			conn.close();
		}catch (Exception e) {
			LOG.error("",e);
		}
	}
}
