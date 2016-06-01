package test.sharding;

import java.util.List;

import org.junit.Test;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;

public class Demo1{
	@Test
	public void getTablesss() {
		try{
			String sql = "select * from test t1 left join test2 t2 on t1.id=t2.id where id = 1";
			List<SQLStatement>  sqlstatList = SQLUtils.parseStatements(sql, null);
			SQLStatement sqlstat = sqlstatList.get(0);
			SchemaStatVisitor ss = SQLUtils.createSchemaStatVisitor(null);
			sqlstat.accept(ss);
			ss.setCurrentTable("11");
			System.out.println(sqlstat.toString());
			System.out.println(ss.getTables());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
