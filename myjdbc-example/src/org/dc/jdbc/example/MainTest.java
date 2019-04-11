package org.dc.jdbc.example;

import java.sql.Connection;

import org.dc.jdbc.core.DbHelper;
import org.dc.jdbc.example.entity.User;

import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.HikariDataSource;

public class MainTest {
	public static void main(String[] args) throws Exception {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		Connection conn = dataSource.getConnection();
		try {
		    DbHelper dbHelper = new DbHelper();
			User user = dbHelper.selectOne(conn, "select * from user where id = ? and real_name = ?",User.class,new Object[] {3,"dc"});
			System.out.println(JSON.toJSONString(user));
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			conn.close();
		}
		dataSource.close();
	}
}
