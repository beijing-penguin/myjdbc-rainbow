package org.dc.jdbc.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * 全局缓存策略提供类
 * @author DC
 * @time 2016-04-23
 */
public class GlobalCache {
	private static final Log LOG = LogFactory.getLog(GlobalCache.class);
	private static final Lock lock = new ReentrantLock();
	//对象的字段缓存
	/**
	 * key 属性名 <br>
	 * value 属性实例
	 */
	private static final Map<Class<?>,Map<String,Field>> cacheFields = new HashMap<Class<?>, Map<String,Field>>();

	public static Map<String,Field> getCacheFields(Class<?> cls){
		Map<String,Field> returnCache = cacheFields.get(cls);
		if(returnCache!=null){
			return returnCache;
		}

		try {
			lock.lock();
			returnCache = cacheFields.get(cls);
			if(returnCache!=null){
				return returnCache;
			}
			Field[] fields = cls.getDeclaredFields();
			Map<String,Field> dataMap = new HashMap<String,Field>();
			for(Field field : fields){
				dataMap.put(field.getName(), field);
			}
			cacheFields.put(cls, dataMap);
			return dataMap;
		} catch (Exception e) {
			LOG.error("",e);
		}finally{
			lock.unlock();
		}
		return null;
	}
}
