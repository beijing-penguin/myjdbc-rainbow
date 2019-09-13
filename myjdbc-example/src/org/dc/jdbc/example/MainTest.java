package org.dc.jdbc.example;

import java.sql.Connection;

import org.dc.jdbc.config.JDBCConfig;
import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.DbHelper;
import org.dc.jdbc.example.entity.User;

import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.HikariDataSource;

public class MainTest {
	public static void main(String[] args) throws Exception {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&serverTimezone=GMT");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		JDBCConfig.isPrintSqlLog = true;
		try {
		    DbHelper dbHelper = new DbHelper(dataSource);
			User user = dbHelper.selectOne("select * from pp_0 where id = ? and name = ?",User.class,3,"dc");
			System.out.println(JSON.toJSONString(user));
		} catch (Throwable e) {
			e.printStackTrace();
		}finally {
			try {
				ConnectionManager.closeConnectionAll();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		dataSource.close();
	}
}
