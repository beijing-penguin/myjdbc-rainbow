package org.dc.jdbc.core;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.core.entity.SqlType;
import org.dc.jdbc.core.operate.DataBaseOperate;
import org.dc.jdbc.core.sqlhandler.SqlCoreHandle;
import org.dc.jdbc.core.utils.JDBCUtils;

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

	private static final Log LOG = LogFactory.getLog(DBHelper.class);
	private DataBaseOperate baseOperate = DataBaseOperate.getInstance();

	public DBHelper(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DBHelper(DataSource dataSource, boolean isPrintSqlLog) {
		this.dataSource = dataSource;
	}
	public DBHelper(DataSource[] masterDataSource,DataSource[] slaveDataSource) {
		this.masterDataSource = masterDataSource;
		this.slaveDataSource = slaveDataSource;
	}
	public DBHelper(DataSource[] masterDataSource) {
		this.masterDataSource = masterDataSource;
	}

	public long selectCount(String sqlOrID) throws Exception {
		return this.selectCount(sqlOrID, nullArgs);
	}

	public long selectCount(String sqlOrID, Object... params) throws Exception {
		String dosql = JDBCUtils.getFinalSql(sqlOrID);
		return this.selectOne("SELECT COUNT(*) FROM (" + dosql + ") t", Long.class, params);
	}

	public <T> T selectOne(String sqlOrID, Class<? extends T> returnClass, Object... params) throws Exception {
		String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
		return baseOperate.selectOne(getFinalConnection(SqlType.SELECT), doSql, returnClass, context.getParamList().toArray()).afterBindEvent().getData();
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
		String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
		return baseOperate.selectList(getFinalConnection(SqlType.SELECT), doSql, returnClass, context.getParamList().toArray()).afterBindEvent().getData();
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
		String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql,params).printSqlLog();
		return baseOperate.excuteSQL(getFinalConnection(), context.getSql(), context.getParamList().toArray()).afterBindEvent().getData();
	}

	/**
	 * 插入一个实体对象
	 * 
	 * @param entity
	 * @return 插入行数
	 * @throws Exception
	 */
	public int insertEntity(Object entity) throws Exception {
		Connection conn = getFinalConnection();
		SqlContext context = SqlCoreHandle.handleInsertRequest(entity).printSqlLog();
		return  baseOperate.excuteSQL(conn, context.getSql(), context.getParamList().toArray()).afterBindEvent().getData();
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
		String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
		return baseOperate.insertReturnPK(getFinalConnection(), doSql, null, context.getParamList().toArray()).afterBindEvent().getData();
	}

	public <T> T insertEntityRtnPK(Object entity) throws Exception {
		Connection conn = getFinalConnection();
		SqlContext context = SqlCoreHandle.handleInsertRequest(entity).printSqlLog();
		return baseOperate.insertReturnPK(conn, context.getSql(), null, context.getParamList().toArray()).afterBindEvent().getData();
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
		String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleBatchRequest(doSql, params).printSqlLog();
		return baseOperate.insertBatch(getFinalConnection(), context.getSql(), null, context.getParamList().toArray()).afterBindEvent().getData();
	}

	public int update(String sqlOrID, Object... params) throws Exception {
		String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql,params).printSqlLog();
		return baseOperate.excuteSQL(getFinalConnection(), context.getSql(), context.getParamList().toArray()).afterBindEvent().getData();
	}

	public int updateEntity(Object entity) throws Exception {
		Connection conn = getFinalConnection();
		SqlContext context = SqlCoreHandle.handleUpdateRequest(entity).printSqlLog();
		return baseOperate.excuteSQL(conn, context.getSql(), context.getParamList().toArray()).afterBindEvent().getData();
	}

	public int delete(String sqlOrID, Object... params) throws Exception {
		String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql,params).printSqlLog();
		return baseOperate.excuteSQL(getFinalConnection(), context.getSql(), context.getParamList().toArray()).afterBindEvent().getData();
	}

	public int deleteEntity(Object entity) throws Exception {
		Connection conn = getFinalConnection();
		SqlContext context = SqlCoreHandle.handleDeleteRequest(entity).printSqlLog();
		return (int) baseOperate.excuteSQL(conn, context.getSql(), context.getParamList().toArray()).afterBindEvent().getData();
	}

	public int excuteSQL(String sqlOrID, Object... params) throws Exception {
		String doSql = JDBCUtils.getFinalSql(sqlOrID);
		return baseOperate.excuteSQL(getFinalConnection(), doSql, params).afterBindEvent().getData();
	}
	
	
	public Connection getFinalConnection(SqlType... sqlType) throws Exception{
		SqlContext context = SqlContext.getContext();
		DataSource curDataSource = null;
		if(dataSource!=null){
			curDataSource = dataSource;
		}else{
			if(slaveDataSource!=null && SqlType.SELECT==sqlType[0]){
				curDataSource = slaveDataSource[slaveIndex.getAndIncrement()%slaveDataSource.length];
			}else{
				if(masterDataSource!=null) {
					curDataSource = masterDataSource[masterIndex.getAndIncrement()%masterDataSource.length];
				}
			}
		}
		context.setCurrentDataSource(curDataSource);
		return ConnectionManager.getConnection(curDataSource);
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

	public AtomicInteger getMasterIndex() {
		return masterIndex;
	}

	public void setMasterIndex(AtomicInteger masterIndex) {
		this.masterIndex = masterIndex;
	}

	public AtomicInteger getSlaveIndex() {
		return slaveIndex;
	}

	public void setSlaveIndex(AtomicInteger slaveIndex) {
		this.slaveIndex = slaveIndex;
	}

	public DataSource[] getMasterDataSource() {
		return masterDataSource;
	}

	public void setMasterDataSource(DataSource[] masterDataSource) {
		this.masterDataSource = masterDataSource;
	}

	public DataSource[] getSlaveDataSource() {
		return slaveDataSource;
	}

	public void setSlaveDataSource(DataSource[] slaveDataSource) {
		this.slaveDataSource = slaveDataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}