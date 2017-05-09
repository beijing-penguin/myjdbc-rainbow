package org.dc.jdbc.example.master_slave;

import java.sql.SQLException;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.DBHelper;
import org.dc.jdbc.core.entity.SqlType;
import org.dc.jdbc.example.entity.User;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;

public class Test1 {
	public static void main(String[] args) throws Exception {
		HikariDataSource dataSource1 = new HikariDataSource();
		dataSource1.setJdbcUrl("jdbc:mysql://localhost:3306/myjdbc_test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		dataSource1.setUsername("root");
		dataSource1.setPassword("123456");
		
		HikariDataSource dataSource2 = new HikariDataSource();
		dataSource2.setJdbcUrl("jdbc:mysql://localhost:3306/myjdbc_test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		dataSource2.setUsername("root");
		dataSource2.setPassword("123456");
		
		HikariDataSource dataSource3 = new HikariDataSource();
		dataSource3.setJdbcUrl("jdbc:mysql://localhost:3306/myjdbc_test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		dataSource3.setUsername("root");
		dataSource3.setPassword("123456");
		
		DBHelper testDbHelper = new DBHelper(Lists.newArrayList(dataSource1,dataSource2,dataSource3));
		ConnectionManager.setTransaction(true);//设置开启事务
		testDbHelper.getFinalConnection(SqlType.SELECT);
		testDbHelper.getFinalConnection(SqlType.SELECT);
		testDbHelper.getFinalConnection(SqlType.SELECT);
	/*	try {
			User user = testDbHelper.selectOne("select * from user where id = ? and real_name = ?",User.class,3,"dc");
			System.out.println(JSON.toJSONString(user));
			
			ConnectionManager.commitAll();
		} catch (Exception e) {
			e.printStackTrace();
			ConnectionManager.rollbackAll();
		}finally {
			ConnectionManager.closeConnectionAll();
		}*/
	}
}
