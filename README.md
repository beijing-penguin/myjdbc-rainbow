源码贡献者<br />
北京-null，北京-企鹅<br />

##轻量级orm持久层操作api，只依赖commons-logging日志架包<br />
##支持ORACLE，MYSQL，SQL Server等数据库<br />
##采用低耦合分层软件架构（共2层）：第一层DBHelper---经过DataBaseOperateProxy代理---第二层DataBaseDaoImp。每层总共享SqlContext上下文中的数据，且每层中，operate，sqlhandle等包中的任何一个类都可以单独拿出来使用<br />
##支持完整的sql日志打印，支持动态控制sql日志
##支持多数据源操作
##支持无缝对接当当开源的分库分表sharding-jdbc
##保证多数据源情况下的基本的跨库事务的数据一致性，如果是像mysql这种支持跨库事务的数据库，那么跨库事务会完全有效；如果是数据库的分布式存储（库不在同一个实例里面），那么本框架只保证基本的事务操作，但无法保证数据的最终一致性，这方面请自行考虑最大努力送达和TCC事务机制，大多数情况下能根据业务情况很好的处理数据不一致问题。
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
##一般使用dbhelper跟使用Spring框架JdbcTemplate非常类似

##
#注意事项
##本框架对jdbc操作轻量级封装，主要用于调试方便，开发方便，保证源码的可维护性。考虑到缓存命中率的问题，性能优化方面可以请根据自身业务需求的特点考虑自定义一级或二级缓存也许会更好。另外使用本框架源代码出现任何问题造成的经济损失，本人以及源码贡献者将不承担任何法律责任。

#联系方式
##QQ群号：133898743，欢迎进群讨论本框架以及后端服务技术