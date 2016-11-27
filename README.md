##轻量级orm持久层操作api，只依赖commons-logging日志架包<br />
##支持0配置0注解对实体对象的增删改查
##支持直接传入sql操作数据库
##支持MYSQL，兼容其他sql标准的数据<br />
##采用低耦合分层软件架构（共2层）：第一层DBHelper---经过DataBaseOperateProxy代理---第二层DataBaseDaoImp。每层总共享SqlContext上下文中的数据<br />
##支持完整的sql日志打印与日志是否输出动态控制
##支持多数据源操作
##支持无缝对接当当开源的分库分表sharding-jdbc
#使用方式
##①确保引入myjdbc源码jar 包，或者引用了pom.xml，或者直接 copy源代码到src下
##②请引入druid，HikariDataSource，或者你习惯使用的任何一种数据源jar包，main方法查询test数据库中的数据集合如下
```java
package com.dc.test.hikaricp;

import java.util.List;
import java.util.Map;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.DBHelper;

import com.zaxxer.hikari.HikariDataSource;

public class HikaricpTest {
	public static void main(String[] args)  {
		HikariDataSource hds = new HikariDataSource();
		hds.setUsername("root");
		hds.setPassword("123456");
		hds.setJdbcUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=UTC");
		
		DBHelper testDbhelper = new DBHelper(hds);
		try {
			ConnectionManager.startTransaction();//默认查询可以不用设置事务，这里只是演示用
			List<Map<String, Object>> list = testDbhelper.selectList("select * from user");
			System.out.println(list);
			ConnectionManager.commitAll();//提交操作。
		} catch (Exception e) {
			e.printStackTrace();
			ConnectionManager.rollbackAll();//回滚
		}finally {
			ConnectionManager.closeConnectionAll();//关闭当前所有连接
		}
	}
}
```
##③DBHelper api详情请看DBHelper.java类中的注释。增删改查的操作方法有且最多只有3个参数，第一个sql或者动态引用key，第二个参数返回值类型，第三个参数sql匹配参数（Map，List，Object（实体对象））
##事务配置请参考另外一份SpringConfig.txt aop配置说明
##配置dbhelper跟配置Spring的JdbcTemplate非常类似
