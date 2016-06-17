package org.dc.jdbc.cache.core;

import java.util.List;

import javax.sql.DataSource;

import org.dc.jdbc.helper.DBHelper;

public class Consumer implements Runnable {
	private Storage s = null;
	private static JedisHelper jedisHelper = new JedisHelper(JedisConfig.defaultJedisPool);
			public Consumer( Storage s) {
		this.s = s;
	}

	public void run() {
		try {
			while (true) {
				System.out.println("准备消费产品.");
				String sqlKey = s.pop();
				List<String> dataSourceList = jedisHelper.hmget(sqlKey, JedisHelper.DATASOURCE_KEY);
				DataSource dataSource = DBHelper.dataSourceMaps.get(dataSourceList.get(0));
				DBHelper dbHelper = new DBHelper(dataSource);
				System.out.println("已消费(" + sqlKey+ ").");
				System.out.println("===============");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
