package org.dc.cache.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
/**
 * reids缓存操作
 * @author DC
 * @time 2016-05-16
 */
public class JedisUtils {
	private static final Log log = LogFactory.getLog(JedisUtils.class);

	private static JedisPool pool = new JedisPool("localhost", 6379);
	public static String get(String key){  

		Jedis jedis = null;  
		try {  
			jedis = pool.getResource();
			return jedis.get(key);  
		} catch (Exception e) {  
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;  
	}
	@SuppressWarnings("unchecked")
	public static <T>  T getObject(byte[] key){  
		Jedis jedis = null;
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		try {  
			jedis = pool.getResource();
			byte[] obj_bytes = jedis.get(key);
			if(obj_bytes!=null){
				bais = new ByteArrayInputStream(obj_bytes);
				ois = new ObjectInputStream(bais);
				return (T) ois.readObject();
			}
		} catch (Exception e) {  
			log.error("",e);
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
	public static String set(String key,String value){  
		Jedis jedis = null;
		try {  
			jedis = pool.getResource();
			return jedis.set(key, value);
		} catch (Exception e) {  
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	/**
	 * 存储字节数组
	 * @param key
	 * @param value
	 * @return
	 */
	public static String setObject(byte[] key,Object value){  
		Jedis jedis = null;
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {  
			jedis = pool.getResource();


			//序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(value);
			oos.flush();
			baos.flush();

			return jedis.set(key, baos.toByteArray());
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
		return null;
	}
	public static Long del(byte[]...keys){  
		Jedis jedis = null;
		try {  
			jedis = pool.getResource();
			return jedis.del(keys);
		} catch (Exception e) {  
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public static Long del(String...keys){  
		Jedis jedis = null;
		try {  
			jedis = pool.getResource();
			return jedis.del(keys);
		} catch (Exception e) {
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public static String hmset(String key,Map<String,String> valueMap){  
		Jedis jedis = null;
		try {  
			jedis = pool.getResource();
			return jedis.hmset(key,valueMap);
		} catch (Exception e) {
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public static List<String> hmget(String key,String...fields){  
		Jedis jedis = null;
		try {  
			jedis = pool.getResource();
			return jedis.hmget(key, fields);
		} catch (Exception e) {
			log.error("",e);
		} finally {  
			//返还到连接池  
			jedis.close();
		}
		return null;
	}
	public static void main(String[] args) {
		try{
			int i = 0;
			String s = "中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中"
					+ "文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文"
					+ "中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中"
					+ "文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中"
					+ "文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文"
					+ "中文中文中文中文中文中文中文中文中文中文中文中文中文中中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文"
					+ "中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中"
					+ "文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中"
					+ "文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文"
					+ "中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中"
					+ "文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文"
					+ "中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中"
					+ "文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中"
					+ "文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文中文";
			String sql = "select * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.idselect * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.idselect * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.idselect * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.idselect * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.idselect * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.idselect * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.idselect * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.idselect * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.idselect * from user where username = \"dc\" and age =1 left join userinfo on t1.id=t2.id";
			System.out.println(JedisUtils.get(sql+"_"+1));
			/*while(true){
				System.out.print(JedisUtils.set(sql+"_"+i, s+"_"+i));
				i++;
				System.out.print("_"+i+"\n");
			}*/
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
