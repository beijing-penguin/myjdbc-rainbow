package test;
import java.sql.SQLException;

import com.alibaba.druid.pool.DruidDataSource;

public class Configure {
	public static void main(String[] args) {
		try {
			testSource.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Configure.testSource.getName());
	}
    /**
     * sql包目录，定义多个目录用逗号分割 
     */
    public static DruidDataSource testSource = new DruidDataSource();
	public static DruidDataSource accSource = new DruidDataSource();
	static{
		testSource.setUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&useSSL=false");
		testSource.setUsername("root");
		testSource.setPassword("123456");
		/*密码加密
			try {
				testSource.setFilters("config");
			} catch (Exception e) {
				log.error("",e);
			}
			testSource.setConnectionProperties("config.decrypt=true");
		*/
		testSource.setInitialSize(1);
		testSource.setMaxActive(4);
		testSource.setMinIdle(0);
		testSource.setMaxWait(60000);
		testSource.setValidationQuery("SELECT 1");
		testSource.setTestOnBorrow(false);
		testSource.setTestWhileIdle(true);
		testSource.setPoolPreparedStatements(false);
		testSource.setDriverClassName("com.mysql.jdbc.Driver");
	}
	static{
		accSource.setUrl("jdbc:mysql://localhost:3306/account_ms?useUnicode=true&characterEncoding=UTF-8");
		accSource.setUsername("root");
		accSource.setPassword("123456");
		/*密码加密
			try {
				testSource.setFilters("config");
			} catch (Exception e) {
				log.error("",e);
			}
			testSource.setConnectionProperties("config.decrypt=true");
		*/
		accSource.setInitialSize(2);
		accSource.setMaxActive(4);
		accSource.setMinIdle(0);
		accSource.setMaxWait(60000);
		accSource.setValidationQuery("SELECT 1");
		accSource.setTestOnBorrow(false);
		accSource.setTestWhileIdle(true);
		accSource.setPoolPreparedStatements(false);
		accSource.setDriverClassName("com.mysql.jdbc.Driver");
	}
}
