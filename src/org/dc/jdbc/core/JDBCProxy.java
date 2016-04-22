package org.dc.jdbc.core;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;


import org.dc.jdbc.anno.Transactional;
import org.dc.jdbc.core.ConnectionManager;
/**
 * 动态反向代理,主要作用：拦截参数、管理数据库事务
 * @author dc
 * @time 2015-8-17
 */
public final class JDBCProxy implements MethodInterceptor {
    private final static  JDBCProxy jdbcProxy = new JDBCProxy();
    public static JDBCProxy getInstance(){
        return jdbcProxy;
    }
    private JDBCProxy(){}
	public Object intercept(Object obj, Method method, Object[] objects, MethodProxy proxy) throws Throwable {
		Object invokeObj = null;
		try{
			Transactional t  = method.getAnnotation(Transactional.class);
	        ConnectionManager.isTransaction.set(t==null); 
		    //执行目标方法
			invokeObj = proxy.invokeSuper(obj, objects);
			
			ConnectionManager.commit();
		}catch(Throwable e){
			ConnectionManager.rollback();
			throw e;
		}finally{
			ConnectionManager.closeConnection();
		}
		return invokeObj;
	}
	
	public Object getTarget(Object target) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(target.getClass());  
		// 回调方法  
		enhancer.setCallback(this);
		// 创建代理对象  
		return enhancer.create();
	}
}
