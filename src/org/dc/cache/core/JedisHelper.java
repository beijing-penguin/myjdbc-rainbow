package org.dc.cache.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.entity.SqlEntity;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
/**
 * reids缓存操作
 * @author DC
 * @time 2016-05-16
 */
public class JedisHelper {
	private static final Log log = LogFactory.getLog(JedisHelper.class);
	//private JedisPool jedisPool = new JedisPool("localhost", 6379);
	private volatile JedisPool jedisPool;
	public JedisHelper(JedisPool jedisPool){
		this.jedisPool = jedisPool;
	}
	public <T> T getSQLCache(SqlEntity sqlEntity) throws Exception{
		return this.getObject(this.getSQLKey(sqlEntity));
	}
	public void setSQLCache(SqlEntity sqlEntity,Object value) throws Exception{
		String sqlKey = this.getSQLKey(sqlEntity);
		
		Jedis jedis = null;
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {  
			jedis = jedisPool.getResource();
			Transaction t = jedis.multi();

			//序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			oos.flush();
			baos.flush();
			
			jedis.set(sqlKey.getBytes(), baos.toByteArray());
			for (String tableName : sqlEntity.getTables()) {
				jedis.sadd(tableName, sqlKey);
			}
			t.exec();
		} catch (Exception e) {
			log.error("",e);
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				log.error("",e);
			}
			try {
				baos.close();
			} catch (IOException e) {
				log.error("",e);
			}
			//返还到连接池  
			jedis.close();
		}
	}
	public void delSQLCache(SqlEntity sqlEntity){
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			Transaction t = jedis.multi();
			
			for (String tableName : sqlEntity.getTables()) {
				Set<String> sqlkeySet = jedis.smembers(tableName);
				jedis.del((String[]) sqlkeySet.toArray());
				jedis.del(tableName);
			}
			t.exec();
		} catch (Exception e) {
			log.error("",e);
		} finally {
			//返还到连接池  
			jedis.close();
		}
	}
	
	public String getSQLKey(SqlEntity sqlEntity){
		Object[] params_obj = sqlEntity.getParams();
		StringBuilder params = new StringBuilder();
		for (int i = 0; i < params_obj.length; i++) {
			params.append(String.valueOf(params_obj[i]));
		}
		return sqlEntity.getSql()+params.toString();
	}
	public Set<String> getKeys(String key)  throws Exception{
		Jedis jedis = null;  
		try {
			jedis = jedisPool.getResource();
			return jedis.keys(key);
		} catch (Exception e) {  
			throw e;
		} finally {  
			//返还到连接池  
			jedis.close();
		}
	}
	public String get(String key) throws Exception{  

		Jedis jedis = null;  
		try {  
			jedis = jedisPool.getResource();
			return jedis.get(key);  
		} catch (Exception e) {  
			throw e;
		} finally {  
			//返还到连接池  
			jedis.close();
		}
	}
	@SuppressWarnings("unchecked")
	public <T>  T getObject(String key) throws Exception{  
		Jedis jedis = null;
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		try {  
			jedis = jedisPool.getResource();
			byte[] obj_bytes = jedis.get(key.getBytes());
			if(obj_bytes!=null){
				bais = new ByteArrayInputStream(obj_bytes);
				ois = new ObjectInputStream(bais);
				return (T) ois.readObject();
			}
		} catch (Exception e) {  
			throw e;
		} finally {
			if(ois!=null){
				try {
					ois.close();
					ois = null;
				} catch (IOException e) {
					log.error("",e);
				}
			}
			if(bais!=null){
				try {
					bais.close();
					bais = null;
				} catch (IOException e) {
					log.error("",e);
				}
			}
			//返还到连接池  
			jedis.close();
		}
		return null;  
	}
	public String set(String key,String value) throws Exception{  
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.set(key, value);
		} catch (Exception e) {  
			throw e;
		} finally {  
			//返还到连接池  
			jedis.close();
		}
	}
	/**
	 * 存储字节数组
	 * @param key
	 * @param value
	 * @return
	 */
	public String setObject(String key,Object value) throws Exception{  
		Jedis jedis = null;
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {  
			jedis = jedisPool.getResource();


			//序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			oos.flush();
			baos.flush();

			return jedis.set(key.getBytes(), baos.toByteArray());
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				oos.close();
			} catch (IOException e) {
				log.error("",e);
			}
			try {
				baos.close();
			} catch (IOException e) {
				log.error("",e);
			}
			//返还到连接池  
			jedis.close();
		}
	}
	public Long delObject(byte[]...keys) throws Exception{
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.del(keys);
		} catch (Exception e) {  
			throw e;
		} finally {  
			//返还到连接池  
			jedis.close();
		}
	}
	public Long del(String...keys) throws Exception{  
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.del(keys);
		} catch (Exception e) {
			throw e;
		} finally {  
			//返还到连接池  
			jedis.close();
		}
	}
	public String hmset(String key,Map<String,String> valueMap) throws Exception{  
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.hmset(key,valueMap);
		} catch (Exception e) {
			throw e;
		} finally {  
			//返还到连接池  
			jedis.close();
		}
	}
	public List<String> hmget(String key,String...fields) throws Exception{  
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.hmget(key, fields);
		} catch (Exception e) {
			throw e;
		} finally {  
			//返还到连接池  
			jedis.close();
		}
	}
	public Long sadd(String key,String...value) throws Exception{
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.sadd(key, value);
		} catch (Exception e) {
			throw e;
		} finally {  
			//返还到连接池  
			jedis.close();
		}
	}
	public Set<String> smembers(String key) throws Exception{
		Jedis jedis = null;
		try {  
			jedis = jedisPool.getResource();
			return jedis.smembers(key);
		} catch (Exception e) {
			throw e;
		} finally {  
			//返还到连接池  
			jedis.close();
		}
	}
	public static void main(String[] args) {
		try{
			JedisHelper helper = new JedisHelper(JedisConfig.jedisPool);
			/*String key1 = "userschoollog";
			String key2 = "userlog";
			String key3 = "school";
			helper.setObject((key1+"_"+"select * from user"), new Object[]{1,2,3,4,5});
			helper.setObject((key2+"_"+"select * from user"), new Object[]{1,2,3,4,5});
			helper.setObject((key3+"_"+"select * from user"), new Object[]{1,2,3,4,5});*/
			System.out.println(helper.getKeys("*school*[_]*"));
		}catch(Exception e){
			e.printStackTrace();
		}

	}
}
