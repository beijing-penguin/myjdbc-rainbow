package org.dc.jdbc.example;

import java.util.List;

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
		    User user = new User();
		    user.setId(10L);
		    
			List<User> userList1 = dbHelper.selectEntityList(user,User.class);
	        System.out.println(JSON.toJSONString(userList1));
	         
			User user2 = dbHelper.selectOneEntity(user,User.class);
			System.out.println(JSON.toJSONString(user2));
		} catch (Throwable e) {
			e.printStackTrace();
		}finally {
			try {
				ConnectionManager.closeConnectionAll();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
