package org.dc.jdbc.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import javax.sql.DataSource;
import org.dc.jdbc.core.CacheCenter;
import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.SqlContext;
import org.dc.jdbc.core.entity.OperateType;
import org.dc.jdbc.core.entity.SqlType;
import org.dc.jdbc.core.operate.DataBaseOperate;
import org.dc.jdbc.core.sqlhandler.PrintSqlLogHandler;
import org.dc.jdbc.core.sqlhandler.SqlCoreHandle;
import org.dc.jdbc.core.utils.JDBCUtils;

/**
 * 数据操作层代理
 * 
 * @author DC
 *
 */
public class DataBaseOperateJdkProxy implements InvocationHandler {
	private DataBaseOperate baseOperate = DataBaseOperate.getInstance();
	/** 私有构造器 */  
	private DataBaseOperateJdkProxy() {}
	private static DataBaseOperateJdkProxy instance= null;
	/** 
	 * 初始化示例。 
	 */  
	private static synchronized void initInstance(){
		if (instance == null){
			instance = new DataBaseOperateJdkProxy();  
		}
	}
	/** 
	 * 获取该类实例 
	 * @return 
	 */  
	public static DataBaseOperateJdkProxy getInstance(){
		if (instance == null){
			initInstance();
		}
		return instance;
	}
	// 目标对象
	private Object target;


	@Override
	public Object invoke(Object target, Method targetMethod, Object[] params) throws Throwable {
		SqlContext context = SqlContext.getContext();
		String sqlOrID = params[0].toString();
		String dosql = sqlOrID.startsWith("$") ? CacheCenter.SQL_SOURCE_MAP.get(sqlOrID) : sqlOrID;
		//当前sql类型
		SqlType sqlType =  JDBCUtils.getSqlType(dosql);
		context.setSqlType(sqlType);

		DataSource currentDataSource = this.getFinalDataSource();
		OperateType operateType = context.getOperateType();
		if (operateType==OperateType.INSERT_BATCH) {
			context = SqlCoreHandle.handleBatchRequest(dosql, (Object[]) params[2]);
		} else {
			context = SqlCoreHandle.handleRequest(dosql, (Object[]) params[2]);
		}
		Connection conn = ConnectionManager.getConnection(currentDataSource);

		// 打印日志
		if (context.isPrintSqlLog()) {
			PrintSqlLogHandler.getInstance().handleRequest(context.getSql(), context.getParamList().toArray());
		}
		Class<?> cls = params[1]==null?null:(Class<?>)params[1];
		Object obj_rt = null;
		if(sqlType==SqlType.SELECT){
			if(operateType==OperateType.SELECT_ONE){
				obj_rt = baseOperate.selectOne(conn,dosql, cls, context.getParamList().toArray());
			}else{
				obj_rt = baseOperate.selectList(conn,dosql, cls, context.getParamList().toArray());
			}
		}else{
			if(operateType==OperateType.INSERT_BATCH){
				obj_rt = baseOperate.insertBatch(conn,dosql, cls, context.getParamList().toArray());
			}if(operateType==OperateType.INSERT_RETURN_PK){
				obj_rt = baseOperate.insertReturnPK(conn, dosql, cls, context.getParamList().toArray());
			} else{
				obj_rt = baseOperate.excuteSQL(conn,dosql, cls,  context.getParamList().toArray());
			}
		}
		// 如果为只读事务，关闭连接，避免连接占用时间太长，会阻塞其他线程。
		if (context.getReadOnly()) {
			ConnectionManager.closeConnectionAll();
		}
		return obj_rt;
	}
	public DataSource getFinalDataSource(){
		SqlContext context = SqlContext.getContext();
		DataSource curDataSource = null;
		DataSource dataSource = context.getDbHelper().getDataSource();
		if(dataSource!=null){
			curDataSource = dataSource;
		}else{
			DataSource[] slaveDataSource = context.getDbHelper().getSlaveDataSource();
			if(slaveDataSource!=null && SqlType.SELECT==context.getSqlType()){
				curDataSource = slaveDataSource[SqlContext.getContext().getDbHelper().getSlaveIndex().getAndIncrement()%slaveDataSource.length];
			}else{
				DataSource[] masterDataSource = context.getDbHelper().getMasterDataSource();
				if(masterDataSource!=null) {
					curDataSource = masterDataSource[SqlContext.getContext().getDbHelper().getMasterIndex().getAndIncrement()%masterDataSource.length];
				}
			}
		}
		context.setCurrentDataSource(curDataSource);
		return curDataSource;
	}
	/**
	 * 获取目标对象的代理对象
	 * 
	 * @return 代理对象
	 */
	public Object getProxy() {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), target.getClass().getInterfaces(),
				this);
	}
}
