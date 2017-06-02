package org.dc.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.core.entity.DataSourceBean;
import org.dc.jdbc.core.entity.ResultData;
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
	private static Map<DataSource,DataSourceBean> dataSourceMap = null;
	private volatile AtomicInteger masterIndex = new AtomicInteger(0);
	private volatile AtomicInteger slaveIndex = new AtomicInteger(0);
	private volatile List<DataSourceBean> masterDataSourceBeanList;
	private volatile List<DataSourceBean> slaveDataSourceBeanList;
	private volatile DataSource dataSource;

	private static final Log LOG = LogFactory.getLog(DBHelper.class);
	private DataBaseOperate baseOperate = DataBaseOperate.getInstance();

	public DBHelper(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public DBHelper(List<? extends DataSource> masterDataSourceList,List<? extends DataSource> slaveDataSourceList) {
		this.masterDataSourceBeanList = toDataSourceBeanList(masterDataSourceList);
		this.slaveDataSourceBeanList = toDataSourceBeanList(slaveDataSourceList);
	}
	public DBHelper(List<? extends DataSource> masterDataSourceList) {
		this.masterDataSourceBeanList = toDataSourceBeanList(masterDataSourceList);
	}
	private List<DataSourceBean> toDataSourceBeanList(List<? extends DataSource> dataSourceList){
		synchronized (Object.class) {
			if(dataSourceMap==null){
				dataSourceMap = new HashMap<>();
			}
			List<DataSourceBean> dataSourceBeanList = new ArrayList<DataSourceBean>();
			for (final DataSource dataSource : dataSourceList) {
				if(!dataSourceMap.containsKey(dataSource)){
					final DataSourceBean sourceBean = new DataSourceBean();
					sourceBean.setDataSource(dataSource);
					sourceBean.setUsed(true);
					dataSourceBeanList.add(sourceBean);

					new Thread(new Runnable() {
						@Override
						public void run() {
							while(true){
								Connection conn = null;
								try {
									conn = dataSource.getConnection();
									baseOperate.selectOne(conn, "SELECT 1", Object.class, null);
									sourceBean.setUsed(true);
								} catch (Exception e) {
									LOG.error("",e);
									sourceBean.setUsed(false);
								}finally{
									if(conn!=null){
										try {
											conn.close();
										} catch (SQLException e) {
											LOG.error("",e);
											sourceBean.setUsed(false);
										}
									}
								}
								try {
									Thread.sleep(60000);
								} catch (InterruptedException e) {
									LOG.error("",e);
									break;
								}
							}
						}
					}).start();
					dataSourceMap.put(dataSource,sourceBean);
				}else{
					dataSourceBeanList.add(dataSourceMap.get(dataSource));
				}
			}
			return dataSourceBeanList;
		}
	}
	public long selectCount(String sqlOrID, Object... params) throws Exception {
		String dosql = JDBCUtils.getFinalSql(sqlOrID);
		return this.selectOne("SELECT COUNT(*) FROM (" + dosql + ") t", Long.class, params);
	}
	public ResultData selectResult(String sqlOrID, Class<?> returnClass, Object... params) throws Exception {
		String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
		Connection conn = getFinalConnection(SqlType.SELECT);
		return baseOperate.selectResult(conn, doSql, returnClass, context.getParamList().toArray(),JDBCUtils.getDataBaseType(context.getCurrentDataSource())).afterBindEvent();
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
		DataSource curDataSource = null;
		DataSourceBean dataSourceBean = null;
		if(dataSource!=null){
			curDataSource = dataSource;
		}else{
			if(slaveDataSourceBeanList!=null && SqlType.SELECT==sqlType[0]){
				dataSourceBean = getFinalDataSource(slaveIndex, slaveDataSourceBeanList);
				if(dataSourceBean==null){
					dataSourceBean = getFinalDataSource(masterIndex, masterDataSourceBeanList);
				}
				if(dataSourceBean==null){
					dataSourceBean = slaveDataSourceBeanList.get(slaveIndex.get()%slaveDataSourceBeanList.size());
				}
			}else if(masterDataSourceBeanList!=null){
				dataSourceBean = getFinalDataSource(masterIndex, masterDataSourceBeanList);
				if(dataSourceBean==null){
					dataSourceBean = masterDataSourceBeanList.get(masterIndex.get()%masterDataSourceBeanList.size());
				}
			}
			curDataSource = dataSourceBean.getDataSource();
		}
		SqlContext.getContext().setCurrentDataSource(curDataSource);
		try{
			Connection conn = ConnectionManager.getConnection(curDataSource);
			if(dataSourceBean!=null){
				dataSourceBean.setUsed(true);
			}
			return conn;
		}catch (Exception e) {
			if(dataSourceBean!=null){
				dataSourceBean.setUsed(false);
			}
			throw e;
		}
	}
	public DataSourceBean getFinalDataSource(AtomicInteger index,List<DataSourceBean> dataSourceBeanList) throws Exception{
		int sourceIndex = index.getAndIncrement()%dataSourceBeanList.size();
		int max = 0;
		DataSourceBean dataSourceBean = null;
		while(true){
			max++;
			if(max>dataSourceBeanList.size()){
				LOG.warn("all datasource is unusable");
				return null;
			}
			if(sourceIndex>dataSourceBeanList.size()-1){
				sourceIndex=0;
			}
			dataSourceBean = dataSourceBeanList.get(sourceIndex);
			if(dataSourceBean.isUsed()){
				break;
			}
			sourceIndex++;
		}
		return dataSourceBean;
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

	public List<DataSourceBean> getMasterDataSourceBeanList() {
		return masterDataSourceBeanList;
	}
	public void setMasterDataSourceBeanList(List<DataSourceBean> masterDataSourceBeanList) {
		this.masterDataSourceBeanList = masterDataSourceBeanList;
	}
	public List<DataSourceBean> getSlaveDataSourceBeanList() {
		return slaveDataSourceBeanList;
	}
	public void setSlaveDataSourceBeanList(List<DataSourceBean> slaveDataSourceBeanList) {
		this.slaveDataSourceBeanList = slaveDataSourceBeanList;
	}
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}