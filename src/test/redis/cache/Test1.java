package test.redis.cache;

import org.dc.jdbc.config.JDBCConfig;
import org.dc.jdbc.helper.DBHelper;
import org.junit.Before;
import org.junit.Test;

import test.Configure;

public class Test1 {
	private static DBHelper testDBHelper;
	@Before
	public void initJdbc(){
		try {
			JDBCConfig.isPrintSqlLog = true;
			JDBCConfig.isSQLCache =true;
			testDBHelper = new DBHelper(Configure.testSource);
			//LoadSqlUtil.loadSql("D:\\Git\\MyJdbc\\target\\classes\\test\\sql");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	@Test
	public void cache1(){
		try {
			/*while (true) {
				System.out.println(testDBHelper.selectList("select * from user"));
			}*/
			System.out.println(testDBHelper.insert("insert into user(name) values(?)","张三"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
