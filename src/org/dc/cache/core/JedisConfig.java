package org.dc.cache.core;

import redis.clients.jedis.JedisPool;

public class JedisConfig {
	public static JedisPool jedisPool = new JedisPool("localhost",6379);
}
