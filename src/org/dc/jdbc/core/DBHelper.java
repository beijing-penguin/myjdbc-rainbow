package org.dc.jdbc.core;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.config.JDBCConfig;
import org.dc.jdbc.core.entity.SqlType;
import org.dc.jdbc.core.operate.DataBaseDaoImp;
import org.dc.jdbc.core.operate.IDataBaseDao;
import org.dc.jdbc.core.proxy.DataBaseOperateProxy;
import org.dc.jdbc.exceptions.TooManyResultsException;

/**
 * 数据持久化操作类 sql执行步骤：1，sql解析，2，获得数据库连接，3，执行核心jdbc操作。
 * 
 * @author dc
 * @time 2015-8-17
 */
public class DBHelper {
	private final static Object[] nullArgs = new Object[0];
	private volatile AtomicInteger masterIndex = new AtomicInteger(0);
	private volatile AtomicInteger slaveIndex = new AtomicInteger(0);
	private volatile DataSource[] masterDataSource;
	private volatile DataSource[] slaveDataSource;
	private volatile DataSource dataSource;
	private volatile boolean isPrintSqlLog = JDBCConfig.isPrintSqlLog;

	private static final Log LOG = LogFactory.getLog(DBHelper.class);
	private static final IDataBaseDao dataBaseDaoProxy = (IDataBaseDao) new DataBaseOperateProxy(DataBaseDaoImp.getInstance()).getProxy();

	public DBHelper(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DBHelper(DataSource dataSource, boolean isPrintSqlLog) {
		this.dataSource = dataSource;
		this.isPrintSqlLog = isPrintSqlLog;
	}
	public DBHelper(DataSource[] masterDataSource,DataSource[] slaveDataSource, boolean isPrintSqlLog) {
		this.masterDataSource = masterDataSource;
		this.slaveDataSource = slaveDataSource;
		this.isPrintSqlLog = isPrintSqlLog;
	}
	public DBHelper(DataSource[] masterDataSource,DataSource[] slaveDataSource) {
		this.masterDataSource = masterDataSource;
		this.slaveDataSource = slaveDataSource;
	}
	public DBHelper(DataSource[] masterDataSource) {
		this.masterDataSource = masterDataSource;
	}
	public DBHelper(DataSource[] masterDataSource, boolean isPrintSqlLog) {
		this.masterDataSource = masterDataSource;
		this.isPrintSqlLog = isPrintSqlLog;
	}
	private DataSource getFinalDataSource(SqlType sqlType,DataSource[] masterDataSource,DataSource[] slaveDataSource,DataSource dataSource){
		if(dataSource!=null){
			return dataSource;
		}
		if(slaveDataSource!=null && SqlType.SELECT==sqlType){
			return slaveDataSource[slaveIndex.getAndIncrement()%slaveDataSource.length];
		}
		if(masterDataSource!=null) {
			return masterDataSource[masterIndex.getAndIncrement()%masterDataSource.length];
		}
		return null;
	}
	public <T> T selectOneEntity(Object entity) throws Exception {
		return this.selectOneEntity(entity, null, nullArgs);
	}

	public <T> T selectOneEntity(Object entity, String whereSql) throws Exception {
		return this.selectOneEntity(entity, whereSql, nullArgs);
	}

	public <T> T selectOneEntity(Object entity, String whereSql, Object... params) throws Exception {
		List<T> list = this.selectEntity(entity, whereSql, params);
		if (list == null) {
			return null;
		} else if (list.size() > 1) {
			throw new TooManyResultsException(list.size());
		} else {
			return list.get(0);
		}
	}

	public <T> List<T> selectEntity(Object entity) throws Exception {
		return this.selectEntity(entity, null, nullArgs);
	}

	public <T> List<T> selectEntity(Object entity, String whereSql) throws Exception {
		return this.selectEntity(entity, whereSql, nullArgs);
	}

	public <T> List<T> selectEntity(Object entity, String whereSql, Object... params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.SELECT, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.selectList(entity, whereSql, params);
	}

	public long selectCount(String sqlOrID) throws Exception {
		return this.selectCount(sqlOrID, nullArgs);
	}

	public long selectCount(String sqlOrID, Object... params) throws Exception {
		String dosql = sqlOrID.startsWith("$") ? CacheCenter.SQL_SOURCE_MAP.get(sqlOrID) : sqlOrID;
		return this.selectOne("SELECT COUNT(*) FROM (" + dosql + ") t", Long.class, params);
	}

	public <T> T selectOne(String sqlOrID, Class<? extends T> returnClass, Object... params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.SELECT, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.selectOne(sqlOrID, returnClass, params);
	}

	public Map<String, Object> selectOne(String sqlOrID, Object... params) throws Exception {
		return this.selectOne(sqlOrID, null, params);
	}

	/**
	 * 查询
	 * 
	 * @param sqlOrID
	 *            直接传入sql脚本或者是以$符号开头的引用key，具体位置保存在CacheCenter.SQL_SOURCE_MAP中
	 * @param returnClass
	 *            返回值类型
	 * @param params
	 *            传入参数，如果sql以?匹配参数，则可以传入数组或者list集合，如果是以#{key}匹配，则传入map或者一个实体对象（传入对象，程序会自动根据字段名匹配#{key}）
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> selectList(String sqlOrID, Class<? extends T> returnClass, Object... params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.SELECT, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.selectList(sqlOrID, returnClass, params);
	}

	public List<Map<String, Object>> selectList(String sqlOrID, Object... params) throws Exception {
		return this.selectList(sqlOrID, null, params);
	}

	/**
	 * 返回受影响的行数
	 * 
	 * @param sqlOrID
	 * @param params
	 * @return 插入行数
	 * @throws Exception
	 */
	public int insert(String sqlOrID, Object... params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.INSERT, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.insert(sqlOrID, null, params);
	}

	/**
	 * 插入一个实体对象
	 * 
	 * @param entity
	 * @return 插入行数
	 * @throws Exception
	 */
	public int insertEntity(Object entity) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.INSERT, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.insertEntity(entity);
	}

	/**
	 * 插入数据并返回自增后的主键
	 * 
	 * @param sqlOrID
	 * @param params
	 * @return 主键
	 * @throws Exception
	 */
	@Deprecated
	public <T> T insertReturnKey(String sqlOrID, Object... params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.INSERT, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.insertReturnPK(sqlOrID, null, params);
	}

	/**
	 * 插入数据并返回自增后的主键
	 * 
	 * @param sqlOrID
	 * @param params
	 * @return 主键
	 * @throws Exception
	 */
	public <T> T insertReturnPK(String sqlOrID, Object... params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.INSERT, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.insertReturnPK(sqlOrID, null, params);
	}

	public <T> T insertEntityRtnPK(Object entity) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.INSERT, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.insertEntityRtnPK(entity);
	}

	/**
	 * 批量插入
	 * 
	 * @param sqlOrID
	 * @param params
	 * @return 返回每条插入语句受影响的行数
	 * @throws Exception
	 */
	public List<Integer> insertBatch(String sqlOrID, Object[] params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.INSERT, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.insertBatch(sqlOrID, null, params);
	}

	public int update(String sqlOrID, Object... params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.UPDATE, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.update(sqlOrID, null, params);
	}

	public int updateEntity(Object entity) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.UPDATE, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.updateEntity(entity);
	}

	public int delete(String sqlOrID, Object... params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.DELETE, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.delete(sqlOrID, null, params);
	}

	public int deleteEntity(Object entity) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.DELETE, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.deleteEntity(entity);
	}

	public int excuteSQL(String sqlOrID, Object... params) throws Exception {
		SqlContext.getContext().setCurrentDataSource(this.getFinalDataSource(SqlType.DELETE, masterDataSource, slaveDataSource, dataSource)).setPrintSqlLog(isPrintSqlLog);
		return dataBaseDaoProxy.excuteSQL(sqlOrID, null, params);
	}

	/**
	 * 仅仅只回滚当前连接
	 * 
	 * @throws Exception
	 */
	public void rollback() {
		try {
			Map<DataSource, Connection> connMap = SqlContext.getContext().getDataSourceMap();
			Connection conn = connMap.get(SqlContext.getContext().getCurrentDataSource());
			conn.rollback();
		} catch (Exception e) {
			LOG.error("", e);
		}
	}

	/**
	 * 仅仅只提交当前连接
	 * 
	 * @throws Exception
	 */
	public void commit() throws Exception {
		Map<DataSource, Connection> connMap = SqlContext.getContext().getDataSourceMap();
		Connection conn = connMap.get(SqlContext.getContext().getCurrentDataSource());
		conn.commit();
	}

	/**
	 * 仅仅只关闭当前连接
	 * 
	 * @throws Exception
	 */
	public void close() {
		try {
			Map<DataSource, Connection> connMap = SqlContext.getContext().getDataSourceMap();
			Connection conn = connMap.get(SqlContext.getContext().getCurrentDataSource());
			conn.close();
		} catch (Exception e) {
			LOG.error("", e);
		}
	}
}