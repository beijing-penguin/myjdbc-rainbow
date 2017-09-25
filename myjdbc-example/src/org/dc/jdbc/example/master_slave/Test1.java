package org.dc.jdbc.example.master_slave;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.DBHelper;
import org.dc.jdbc.example.entity.User;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 主从数据库操作example
 * @author dc
 *
 */
public class Test1 {
	public static void main(String[] args) throws Exception {
		HikariDataSource dataSource1 = new HikariDataSource();
		dataSource1.setJdbcUrl("jdbc:mysql://localhost:3306/myjdbc_test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		dataSource1.setUsername("root");
		dataSource1.setPassword("123456");
		dataSource1.setConnectionTimeout(1000);
		HikariDataSource dataSource2 = new HikariDataSource();
		dataSource2.setJdbcUrl("jdbc:mysql://localhost:3306/myjdbc_test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		dataSource2.setUsername("root");
		dataSource2.setPassword("123456");
		dataSource2.setConnectionTimeout(1000);

		HikariDataSource dataSource3 = new HikariDataSource();
		dataSource3.setJdbcUrl("jdbc:mysql://localhost:3306/myjdbc_test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		dataSource3.setUsername("root");
		dataSource3.setPassword("123456");
		dataSource3.setConnectionTimeout(1000);

		DBHelper testDbHelper = new DBHelper(Lists.newArrayList(dataSource1,dataSource2,dataSource3));
		DBHelper testDbHelper2 = new DBHelper(Lists.newArrayList(dataSource1),Lists.newArrayList(dataSource1,dataSource2,dataSource3));
		ConnectionManager.setTransaction(true);//设置开启事务
		while(true){
			try {
				User user1 = testDbHelper.selectOne("select * from user where id = ? and real_name = ?",User.class,3,"dc");
				User user2 = testDbHelper2.selectOne("select * from user where id = ? and real_name = ?",User.class,3,"dc");
				System.out.println(JSON.toJSONString(user1));
				System.out.println(JSON.toJSONString(user2));

				ConnectionManager.commitAll();
			} catch (Exception e) {
				e.printStackTrace();
				ConnectionManager.rollbackAll();
			}finally {
				ConnectionManager.closeConnectionAll();
			}
			Thread.sleep(10000);
		}
	}
}
