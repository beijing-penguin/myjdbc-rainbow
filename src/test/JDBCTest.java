package test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dc.jdbc.config.JDBCConfig;
import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.helper.DBHelper;
import org.junit.Before;
import org.junit.Test;

public class JDBCTest {
	private static DBHelper accDBHelper;
	private static DBHelper testDBHelper;
	@Before
	public void initJdbc(){
		try {
			JDBCConfig.isPrintSqlLog = true;

			testDBHelper = new DBHelper(Configure.testSource);
			accDBHelper= new DBHelper(Configure.accSource);
			//LoadSqlUtil.loadSql("D:\\Git\\MyJdbc\\target\\classes\\test\\sql");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Test
	public void select(){
		try {
			//ConnectionManager.transactionThreadLocal.set(null);
			ConnectionManager.startTransaction();
			/*start*/
			Map<String,Object> map = testDBHelper.selectOne("select * from user limit 1");
			List<Map<String,Object>> mapList = testDBHelper.selectList("select * from user");
			/*end*/
			Map<String,Object> map_WithParam1 = testDBHelper.selectOne("select * from user where name=? and age=? limit 1","dc",20);
			User user = testDBHelper.selectOne("select name,age from user where name=? and age=? limit 1",User.class,"dc",20);
			//只返回年龄,如数据的该字段没查到则返回的数据为null，那么用Integer类型接受即可，不然会报NullPointerException
			//int age = testDBHelper.selectOne("select age from user where name=? and age=? limit 1",Integer.class,"dc",20);
			Integer age = testDBHelper.selectOne("select age from user where name=? and age=? limit 1",Integer.class,"dc",20);
			String name = testDBHelper.selectOne("select name from user where name=? and age=? limit 1",String.class,"dc",20);
			Map<String,Object> map_WithParam1_1 = testDBHelper.selectOne("select * from user where name=? and age=? limit 1",new Object[]{"dc",20});

			//传入Map
			Map<String,Object> mapParams = new HashMap<String, Object>();
			mapParams.put("name", "dc");
			mapParams.put("age", 12);
			Map<String,Object> map_WithParam1_2 = testDBHelper.selectOne("select * from user where name=#{name} and age=#{age} limit 1",mapParams);
			//这里通过$符号引用了xml中的sql语句，引用规则为          $文件名.SQL_ID
			Map<String,Object> map_WithParam1_2_1 = testDBHelper.selectOne("$user.getOneUser",mapParams);

			//传入对象，也可以对象和Map一起作为参数传入方法
			User userObj = new User();
			userObj.setName("dc");
			userObj.setAge(12);
			Map<String,Object> map_WithParam1_3 = testDBHelper.selectOne("select * from user where name=#{name} and age=#{age} limit 1",userObj);
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
	public void selectOne(){
		try{
			Map<String,Object> user = new HashMap<String,Object>();
			user.put("id", 4);
			Byte name = testDBHelper.selectOne("select age from user where id =#{id} limit 1",Byte.class,user);
			System.out.println(name);
			//String name = testDBHelper.selectOne("select name from user  limit 1",String.class);
			/*List<Integer> list = testDBHelper.selectList("select id from user ",Integer.class);
		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i));
		}
		List<User> list2 = testDBHelper.selectList("select name,id from user ",User.class);
		for (int i = 0; i < list2.size(); i++) {
			System.out.println(list2.get(i).getName() +"-"+list2.get(i).getId());
		}*/

			/*	Object o = testDBHelper.selectOne("select * from user where name = ? limit 1","dc");
		Map<String,Object> oo = testDBHelper.selectOne("select * from user where name = ? limit 1",HashMap.class,"dc");
		System.out.println(o.getClass());*/

		}catch(Exception e){
			e.printStackTrace();
		}
		ConnectionManager.closeConnection();
	}
	@Test
	public void insert(){
		//开启事务
		ConnectionManager.startTransaction();
		try {
			int num = testDBHelper.insert("insert into user(name,age) values(?,?)", "ddddddc",12);
			Integer age = testDBHelper.selectOne("select age from user limit 1",Integer.class);
			System.out.println(age);
			System.out.println(num);
			//提交
			ConnectionManager.commit();
		} catch (Exception e) {
			e.printStackTrace();
			ConnectionManager.rollback();
		}finally{
			ConnectionManager.closeConnection();
		}
	}
}
