package org.dc.jdbc.example;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.DataBaseStream;
import org.dc.jdbc.example.entity.User;

import com.alibaba.fastjson.JSON;
import com.zaxxer.hikari.HikariDataSource;

public class MainTest {
	public static void main(String[] args) throws Exception {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/myjdbc_test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		
		DataBaseStream db = new DataBaseStream(dataSource);
		ConnectionManager.setTransaction(true);//设置开启事务
		ConnectionManager.setReadOnly(true);
		try {
		    db.delete("delete from user where id =?",1);
			User user = db.selectOne("select * from user where id = ? and real_name = ?",User.class,3,"dc");
			System.out.println(JSON.toJSONString(user));
			db.commit();
		} catch (Exception e) {
		    db.rollback();
		}finally {
		    db.close();
		}
	}
}
