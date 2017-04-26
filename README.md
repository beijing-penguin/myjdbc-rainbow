# 快速入门
## 入口类DBHelper.java，new出来就能用。
## ②main方式简单使用，确保引入myjdbc源码jar 包，或者引用了pom.xml，或者直接 copy源代码到src下。引入druid，HikariDataSource，或者你习惯使用的任何一种数据源jar包，main方法查询test数据库中的数据集合如下
```java
public class App {
	public static void main( String[] args ) {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");

		try{
			DBHelper dbHelper = new DBHelper(dataSource);
			//start 对象操作  操作原理说明，根据实体构造最终完整sql并执行操作，改操方法api最多有3个参数，第一个entity（Object类型） 第二个wheresql(String主要是where条件),第三个sql语句对应的参数(对象，list，map，数组) 
			User user = new User();
			user.setName("张三");
			user.setSex(23);
			ConnectionManager.setTransaction(true);
			//插入一个行数据到数据库，然后赋值给user对象的id属性
			Integer id = Integer.parseInt(dbHelper.insertEntityRtnPKKey(user).toString());
			user.setId(id);
			
			//更新一个对象，注意只能根据主键更新一个对象,只更新有值的字段，没有值的字段不会被更新，这点比hibernate要友好很多。
			//dbhelper能够自动检查对象User的字段中是否包含主键，如果没有主键，会抛出异常。
			//检查主键原理是根据User.java的类名确定是那张表，再确定对象中字段和数据库中的字段一一匹配，并完成主键匹配。
			//实现原理请关注new DBHelper时的JDBCUtils.initDataBaseInfo(dataSource);和执行对象操作时的SqlCoreHandle.java的相关操作即可
			System.out.println(dbHelper.updateEntity(user));
			
			//同理删除对象，也只能根据主键删除，如果主键没有，或者为空，则报错
			System.out.println(dbHelper.deleteEntity(user));
			
			//查询操作
			//默认这里返回结果为null，因为上一步已经吧那个对象已经删除了
			User my_user = dbHelper.selectOneEntity(user);//---->根据User实体中的字段值，如果字段值为空，则不参与构造查询条件，此处对应sql语句请自行查看系统打印的完整sql日志。
			List<User> userList = dbHelper.selectEntity(user);//---->返回null
			
			//条件查询
			User u1 = dbHelper.selectOneEntity(user," and sex > 20 and name like ? order by id desc","%张%");//-->请自行查看sql日志，依然是根据实体和wheresql构造最终sql
			//end 对象操作
			
			//纯sql操作,改操方法api最多有3个参数，第一个entity（Object类型） 第二个wheresql(String主要是where条件),第三个sql语句对应的参数(对象，list，map，数组)
			//目前只支持?和#匹配符，如果有朋友建议或需要的可以加上:或者其他通配符
			User u2 = dbHelper.selectOne("select * from user where sex = #{sex} and name=#{name}",User.class, user);
			User u3 = dbHelper.selectOne("select * from user where sex = ? and name=?",User.class, 23,"张三");
			//===>上一步等价于:
			User u4 = dbHelper.selectOne("select * from user where sex = ? and name=?",User.class,new Object[]{23,"张三"});
			//===>上一步等价于:
			List<Object> paramList = new ArrayList<Object>();
			paramList.add(23);
			paramList.add("张三");
			User u5 = dbHelper.selectOne("select * from user where sex = ? and name=?",User.class,paramList);
			
			//返回集合查询,这个传参方式跟使用selectOne一模一样，不过多举例。
			List<User> u6List = dbHelper.selectList("select * from user where sex = ? name=?",User.class,paramList);
			ConnectionManager.commitAll();
		}catch (Exception e) {
			ConnectionManager.rollbackAll();
		}finally {
			ConnectionManager.closeConnectionAll();
		}
	}
}
```
## 支持事务，配置请参考另外一份SpringConfig.txt aop配置说明
## Myjdbc是一个轻量级orm持久层操作api，只依赖commons-logging日志架包<br />
## 支持0配置0注解对实体对象的增删改查，也支持直接传入sql操作数据库
## 主要支持MYSQL，兼容其他以jdbc为驱动的数据库<br />
## 本框架采用低耦合分层软件架构（共2层）：第一层DBHelper---经过DataBaseOperateProxy代理---第二层DataBaseDaoImp。每层总共享SqlContext上下文中的数据<br />
## 支持完整的sql日志打印与日志是否输出动态控制
## 支持业务上常见的跨库操作+自带弱事务支持
## 支持无缝对接当当开源的分库分表sharding-jdbc