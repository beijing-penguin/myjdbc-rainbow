#快速入门
##①定义实体，类名对应表名（只在对实体非select操作时才会强制类名和表名保持一致），实体中的字段可以随便定义，只要想要取数据库的数据，那么最好对象的字段和数据库字段保持一致，myjdbc才能正确查询出数据库的里面的数据并赋值给对象中匹配到的字段,但并不强求实体中一定要包含数据库表中的字段，一切尊重你的设计来定义。例如，你想获取username的数据库，那么实体中定义好username的字段即可。有了这种无限制的字段定义规范，那么spring mvc中，完全可以没有vo对象，只用一个po对象即可以用来接收前端参数，也可以用来操作数据库。实体对象的类名和字段遵循驼峰命名法。例如数据库表名user_table_system，那么类名则可以是User_table_system，也可以是UserTableSystem（推荐这种写法）
##对象字段匹配规则，可以强行匹配，如数据库字段是sys_user_name 那么对象字段可以是sys_user_name 同时也可以是sysUserName（依然建议用这种）
```java

public class User {

	private String name;
	private Integer id;
	private Integer sex;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getSex() {
		return sex;
	}
	public void setSex(Integer sex) {
		this.sex = sex;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
```
##②main方式简单使用，确保引入myjdbc源码jar 包，或者引用了pom.xml，或者直接 copy源代码到src下，引入druid，HikariDataSource，或者你习惯使用的任何一种数据源jar包，main方法查询test数据库中的数据集合如下
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
##③spring配置方式使用（dbhelper支持非常方便的多数据源操作，所以这里可以配置多个dbhelper，但是Myjdbc框架并没有实现强一致性事务，所以建议某个事务方法内只是用一个数据源的操作即可）
##dbhelper 完美的可嵌入性，可以随处new，随处使用。
```xml
 	<bean id="ucenterDBHelper" class="org.dc.jdbc.core.DBHelper">
    	<constructor-arg name="dataSource" ref="ucenterDataSource" />
    </bean>
    <bean id="yanxiuDBHelper" class="org.dc.jdbc.core.DBHelper">
    	<constructor-arg name="dataSource" ref="yanxiuDataSource" />
    </bean>
    <bean id="zgjyDBHelper" class="org.dc.jdbc.core.DBHelper">
    	<constructor-arg name="dataSource" ref="zgjyDataSource" />
    </bean>
    <bean id="manageDBHelper" class="org.dc.jdbc.core.DBHelper">
    	<constructor-arg name="dataSource" ref="manageDataSource" />
    </bean>
    <bean id="frameworkDBHelper" class="org.dc.jdbc.core.DBHelper">
    	<constructor-arg name="dataSource" ref="frameworkDataSource" />
    </bean>
```
##下面是在service层中的使用案例(PS：如果项目没必要的情况下，建议去掉dao层的概念与设计，因为dbhelper已经为您封装好了很多操作，无需dao层，就可以满足一般的业务系统)
```java
@Service
public class DataHandleService {

	@Autowired
	private DBHelper ucenterDBHelper;
	@Autowired
	private DBHelper yanxiuDBHelper;
	@Autowired
	private DBHelper zgjyDBHelper;
	@Autowired
	private UserConverter userConverter;
	@Autowired
	private DBHelper frameworkDBHelper;

	public void updateZG_Jiaoyan_User(ImpUserInfo user) throws Exception {
		if(StringUtils.isNotBlank(user.getMobile())){
			//更新密码
			zgjyDBHelper.update("update Service_.UserAccount_1 set password = ?,role=? where name=? limit 1", user.getPassword(),user.getRole(),user.getMobile());
			//更新省市县
			if(user.getProvinceCode()!=0){
				zgjyDBHelper.update("update Service_.UserInfo_1 set sheng = #{provinceCode} where name=#{mobile} limit 1", user);
			}
			if(user.getCityCode()!=0){
				zgjyDBHelper.update("update Service_.UserInfo_1 set shi = #{cityCode} where name=#{mobile} limit 1", user);
			}
			if(user.getAreaCode()!=0){
				zgjyDBHelper.update("update Service_.UserInfo_1 set quxian = #{areaCode} where name=#{mobile} limit 1", user);
			}
		}
	}

}
```
##上面的案例中，如果讨厌把sql写在业务层，可以把sql保存在其他地方，至于保存在哪个地方，myjdbc不提供方案，请自行设计，例如下面是保存在xml中
##原理说明：会在程序启动时，预先加载xml中的id_key和sql加到CenterCahce.java中的SQL_SOURCE_MAP的map中，这里的key是"$文件名.id" 如$user.updateUser，
##使用时 这可以用dbhelper.update("$user.updateUser")即可。dbhelper底层会默认识别如果第一个参数的第一个字符带有$符号，则会去SQL_SOURCE_MAP中找出sql
```xml
<mysql>
	<sql id="updateUser">
		<![CDATA[
		update user set 
			PASSPORT=#{PASSPORT},NICKNAME=#{NICKNAME},REAL_NAME=#{REAL_NAME},MOBILE=#{MOBILE},EMAIL=#{EMAIL},
			ID_CARD=#{ID_CARD},ACTI_FLAG=#{ACTI_FLAG},STATUS=#{STATUS},REG_APP_ID=#{REG_APP_ID},MODIFY_TIME=#{MODIFY_TIME}
		where UID=#{UID}

		]]>
	</sql>
	<sql id="getTreeInfo">
		<![CDATA[
			select * from menu_tree
		]]>
	</sql>
</mysql>
```

```java
@Service
public class SystemService {
	@Autowired
	public DBHelper manageDBHelper;
	@Transactional(readOnly=true)
	public List<NavTree> initTree() throws Exception{
		return manageDBHelper.selectList("$system.getTreeInfo",NavTree.class);
	}
}
```
##事务配置请参考另外一份SpringConfig.txt aop配置说明

##Myjdbc是一个轻量级orm持久层操作api，只依赖commons-logging日志架包<br />
##支持0配置0注解对实体对象的增删改查，也支持直接传入sql操作数据库
##主要支持MYSQL，兼容其他以jdbc为驱动的数据库<br />
##采用低耦合分层软件架构（共2层）：第一层DBHelper---经过DataBaseOperateProxy代理---第二层DataBaseDaoImp。每层总共享SqlContext上下文中的数据<br />
##支持完整的sql日志打印与日志是否输出动态控制
##支持多数据源操作
##支持无缝对接当当开源的分库分表sharding-jdbc
