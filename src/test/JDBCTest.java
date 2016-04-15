package test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.helper.DBHelper;
import org.junit.Test;

public class JDBCTest {
	private static DBHelper accDBHelper = new DBHelper(Configure.accSource);
	private static DBHelper testDBHelper = new DBHelper(Configure.testSource);
	@Test
	public void select(){
		try {
			ConnectionManager.isTransaction.set(false);
			/*start*/
			Map<String,Object> map = testDBHelper.selectOne("select * from user limit 1");
			List<Map<String,Object>> mapList = testDBHelper.selectList("select * from user");
			/*end*/
			Map<String,Object> map_WithParam1 = testDBHelper.selectOne("select * from user where name=? age=? limit 1","dc",20);
			User user = testDBHelper.selectOne("select name,age from user where name=? age=? limit 1",User.class,"dc",20);
			//只返回年龄
			int age = testDBHelper.selectOne("select age from user where name=? age=? limit 1",Integer.class,"dc",20);
			String name = testDBHelper.selectOne("select name from user where name=? age=? limit 1",String.class,"dc",20);
			Map<String,Object> map_WithParam1_1 = testDBHelper.selectOne("select * from user where name=? age=? limit 1",new Object[]{"dc",20});
			
			//传入Map
			Map<String,Object> mapParams = new HashMap<String, Object>();
			mapParams.put("name", "dc");
			mapParams.put("age", 12);
			Map<String,Object> map_WithParam1_2 = testDBHelper.selectOne("select * from user where name=#{name} age=#{age} limit 1",mapParams);
			Map<String,Object> map_WithParam1_2_1 = testDBHelper.selectOne("$user.getOneUser",mapParams);
			
			//传入对象，也可以对象和Map一起作为参数传入方法
			User userObj = new User();
			userObj.setName("dc");
			userObj.setAge(12);
			Map<String,Object> map_WithParam1_3 = testDBHelper.selectOne("select * from user where name=#{name} age=#{age} limit 1",userObj);
			List<Map<String,Object>> mapList_withParam1 = testDBHelper.selectList("select * from user");
			List<User> mapList_withParam1_1 = testDBHelper.selectList("select name,age from user",User.class);
			List<String> mapList_withParam1_2 = testDBHelper.selectList("select name from user",String.class);
			List<Integer> mapList_withParam1_3 = testDBHelper.selectList("select name from user",Integer.class);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			ConnectionManager.closeConnection();
		}
	}
	@Test
	public void insert(){
		ConnectionManager.isTransaction.set(true);
		try {
			testDBHelper.insert("insert into user(name,age) values(?,?)", "dc",12);
			ConnectionManager.commit();
		} catch (Exception e) {
			e.printStackTrace();
			ConnectionManager.rollback();
		}finally{
			ConnectionManager.closeConnection();
		}
	}
}
class User{
	private String name;
	private int age;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
}