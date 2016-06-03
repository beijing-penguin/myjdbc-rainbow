package org.dc.cache.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisConfig {
	private static final Log log = LogFactory.getLog(JedisHelper.class);
	public static JedisPool defaultJedisPool;
	static{
		InputStream is = null;
		try {
			Properties properties = new Properties();
			is = JedisConfig.class.getResourceAsStream("/jdbc_redis.properties");
			properties.load(is);
			JedisPoolConfig config = new JedisPoolConfig();
			//最大空闲连接数, 默认8个
			config.setMaxIdle(Integer.parseInt(properties.getProperty("redis.maxIdle","8")));
			//最小空闲连接数, 默认0
			config.setMinIdle(Integer.parseInt(properties.getProperty("redis.minIdle","0")));
			
			//最大连接数, 默认8个
			config.setMaxTotal(Integer.parseInt(properties.getProperty("redis.maxTotal", "8")));
			
			//得到一个jedis实例的最大的等待时间(毫秒)，默认阻塞，如果超过等待时间，则直接抛出JedisConnectionException；  
			config.setMaxWaitMillis(Long.parseLong(properties.getProperty("redis.maxWaitMillis","-1")));
			
			//在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；  
			config.setTestOnBorrow(Boolean.parseBoolean(properties.getProperty("redis.testOnBorrow","false")));  
			defaultJedisPool = new JedisPool(config, properties.getProperty("redis.host"), Integer.parseInt(properties.getProperty("redis.port")));
		} catch (Exception e) {
			log.error("",e);
		}finally{
			if(is!=null){
				try {
					is.close();
				} catch (IOException e) {
					log.error("",e);
				}
			}
		}
	}
}
