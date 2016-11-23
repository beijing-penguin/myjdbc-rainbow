package org.dc.jdbc.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.dc.jdbc.config.JDBCConfig;
import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.CacheCenter;
import org.dc.jdbc.core.SqlContext;
import org.dc.jdbc.core.sqlhandler.PrintSqlLogHandler;
import org.dc.jdbc.core.sqlhandler.SqlCoreHandle;
/**
 * 数据操作层代理
 * @author DC
 *
 */
public class DataBaseOperateProxy implements InvocationHandler{
	// 目标对象   
	private Object target;  

	/** 
	 * 构造方法 
	 * @param target 目标对象  
	 */  
	public DataBaseOperateProxy(Object target) {  
		super();  
		this.target = target;
	}  

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		SqlContext context = SqlContext.getContext();
		if(args.length==1){
			String methodName = method.getName();
			if(methodName.equals("updateEntity")){
				context = SqlCoreHandle.handleUpdateRequest(args[0]);
			}else if(methodName.equals("insertEntity")){
				SqlCoreHandle.handleInsertRequest(args[0]);
			}
		}else{
			String sqlOrId = args[0].toString();
			String dosql = sqlOrId.startsWith("$")?CacheCenter.SQL_SOURCE_MAP.get(sqlOrId):sqlOrId;
			context = SqlCoreHandle.handleRequest(dosql, (Object[])args[2]);
			args[0] = context.getSql();
			args[2] = context.getParams();
		}
		//打印日志
		if(JDBCConfig.isPrintSqlLog){
			PrintSqlLogHandler.getInstance().handleRequest(context.getSql() , context.getParams());
		}
		//获取连接
		ConnectionManager.getConnection(context.getCurrentDataSource());
		
		Object rt = method.invoke(target, args);
		
		//如果为只读事务，关闭连接，避免连接占用时间太长，会阻塞其他线程。
		if(context.getReadOnly()){
			ConnectionManager.closeConnectionAll();
		}
		return rt;
	}
	/** 
	 * 获取目标对象的代理对象 
	 * @return 代理对象 
	 */  
	public Object getProxy() {  
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),   
				target.getClass().getInterfaces(), this);  
	}
}
