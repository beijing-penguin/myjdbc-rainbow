package org.dc.jdbc.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.dc.jdbc.config.JDBCConfig;
import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.sqlhandler.PrintSqlLogHandler;
import org.dc.jdbc.core.sqlhandler.XmlSqlHandler;
import org.dc.jdbc.entity.SqlContext;

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
		if(args[0].toString()==null || args[0].toString().trim().length()==0){
			throw new Throwable("connection is null");
		}
		String sqlOrID = args[1].toString();
		String sql = sqlOrID.startsWith("$")?SqlContext.getSourceSql(sqlOrID):sqlOrID;
		if(sql==null || sql.trim().length()==0){
			throw new Throwable("sql is null");
		}
		
		SqlContext context = SqlContext.getContext();
		if(args.length==4){
			XmlSqlHandler.getInstance().handleRequest(sql, (Object[]) args[3]);
			args[3] = context.getParams();
		}else if(args.length==3){
			XmlSqlHandler.getInstance().handleRequest(sql, (Object[]) args[2]);
			args[2] = context.getParams();
		}
		args[1] = context.getSql();
		
		if(JDBCConfig.isPrintSqlLog){
			PrintSqlLogHandler.getInstance().handleRequest(context.getSql(), context.getParams());
		}

		Object rt = method.invoke(target, args);
		if(!context.getTransaction()){
			ConnectionManager.closeConnection();
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
