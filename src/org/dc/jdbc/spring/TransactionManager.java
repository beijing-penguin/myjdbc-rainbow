package org.dc.jdbc.spring;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.transaction.annotation.Transactional;

import org.dc.jdbc.core.ConnectionManager;

public class TransactionManager {
	// 用来做环绕通知的方法可以第一个参数定义为org.aspectj.lang.ProceedingJoinPoint类型
	public Object doAround(ProceedingJoinPoint call) throws Throwable {
		Signature sig = call.getSignature();
		MethodSignature ms = (MethodSignature) sig;
		Method method = call.getTarget().getClass().getDeclaredMethod(ms.getName(), ms.getParameterTypes());

		Transactional transactional = method.getAnnotation(Transactional.class);
		if (transactional == null) {
			transactional = method.getDeclaringClass().getAnnotation(Transactional.class);
		}

		if (transactional != null) {// 如果不为空，则开启事务
			ConnectionManager.setTransaction(true);
			ConnectionManager.setReadOnly(transactional.readOnly());
		}
		Object invokeObj = null;
		try {
			// 执行目标方法
			invokeObj = call.proceed();
			// invokeObj = method.invoke(call.getTarget(), call.getArgs());
			ConnectionManager.commitAll();
		} catch (Throwable e) {
			ConnectionManager.rollbackAll();
			throw e;
		} finally {
			ConnectionManager.closeConnectionAll();
		}
		return invokeObj;
	}
}