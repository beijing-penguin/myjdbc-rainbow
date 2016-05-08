package test.core.sqlhandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.dc.jdbc.core.sqlhandler.XmlSqlHandler;
import org.dc.jdbc.entity.SqlEntity;

import junit.framework.TestCase;
import test.User;

public class LexerSqlHandleTest extends TestCase{
	/**
	 * 参数匹配规则说明：
	 * 1、handleRequest方法的第二个参数中如果有多个Map或者对象，如果字段名，或者某些key相同，则以这个相同的key最后一次出现的时候的值为准。如下test1案例
	 * 2、sql语句如果含有'#'通配符，则判定该handleRequest的第二个参数只能 传map或者对象。如果多传了集合或数组类型的参数进去，则根据sql分词的结果判定为准，即sql中有?通配符，则将传入的所有的数组或集合作为最终执行sql所必备的元素。如果sql语句中既有?也有#，则程序抛出异常,即#和?不可同时出现
	 * 3、还有其他疑问，请查阅源码，或直接提交相关bug和建议到github。
	 * @throws Exception
	 */
	public void test1() throws Exception{
		XmlSqlHandler xmlSqlHandler = XmlSqlHandler.getInstance();
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("name", "企鹅one号");
		m.put("age", "22");
		User u =new User();
		u.setAge(50);
		u.setName("企鹅2号");
		SqlEntity sqlEntity = xmlSqlHandler.handleRequest("select *,if(sva=1,'男','女') as ssva from user name=#{name} and filed1 = '#{name}' and age=#{age} ", new Object[]{"dc",12,m,u});
		System.out.println(sqlEntity.getSql());
		System.out.println(Arrays.toString(sqlEntity.getParams()));
	}
}
