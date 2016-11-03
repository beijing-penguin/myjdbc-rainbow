package org.dc.jdbc.core.sqlhandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dc.jdbc.entity.SqlContext;
import org.dc.jdbc.sqlparse.Lexer;
import org.dc.jdbc.sqlparse.Token;

/**
 * sql核心解析器
 * @author DC
 *
 */
public class SqlCoreHandle{
	private static final SqlCoreHandle oper = new SqlCoreHandle();
	public static SqlCoreHandle getInstance(){
		return oper;
	}
	/**
	 * 处理方法，调用此方法处理请求
	 */
	public SqlContext handleRequest(String doSql,Object...params) throws Exception{
		List<Object> returnList = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder(doSql);

		Map<Object,Object> allparamMap = null;
		List<Object>  allParamList = null;
		if(params!=null && params.length>0){
			allparamMap = new HashMap<Object,Object>();
			allParamList = new ArrayList<Object>();
			for (Object param : params) {
				if(param==null){
					allParamList.add(param);
				}else if(Map.class.isAssignableFrom(param.getClass())){
					Map<?,?> paramMap = (Map<?, ?>) param;
					for (Object key : paramMap.keySet()) {
						if(allparamMap.containsKey(key)){
							throw new Exception("key="+key+" is already repeated");
						}else{
							allparamMap.put(key, paramMap.get(key));
						}
					}
				}else if(Object[].class.isAssignableFrom(param.getClass()) || Collection.class.isAssignableFrom(param.getClass())){
					allParamList.addAll((Collection<?>) param);
				}else if(param.getClass().getClassLoader()==null){
					allParamList.add(param);
				}else { // java对象
					Field[] fields = param.getClass().getDeclaredFields();
					for (Field field : fields) {
						field.setAccessible(true);
						Object value = field.get(param);
						String key = field.getName();
						if(allparamMap.containsKey(key)){
							throw new Exception("key="+key+" is already repeated");
						}else{
							allparamMap.put(key, value);
						}
					}
				}
			}
		}
		Lexer lexer = new Lexer(sql.toString());
		int lastCharLen = 0;
		while(true){
			lexer.nextToken();
			Token tok = lexer.token();
			if (tok == Token.EOF) {
				break;
			}
			String str = lexer.stringVal();
			int curpos = lexer.pos();
			if(tok.name == null && tok == Token.VARIANT){//异类匹配，这里的异类只有#号，sql编写规范的情况下，不需要判断str.contains("#")
				String key = str.substring(2, str.length()-1);
				if(allparamMap!=null && !allparamMap.containsKey(key)){
					throw new Exception("sqlhandle analysis error! parameters \""+key+"\" do not match to!");
				}
				Object value = allparamMap.get(key);
				returnList.add(value);
				sql.replace(curpos-str.length()-lastCharLen, curpos-lastCharLen, "?");
				lastCharLen = lastCharLen+str.length()-1;
			}else if(tok == Token.QUES){
				if(allParamList!=null){
					returnList = allParamList;
				}
			}
		}
		SqlContext sqlContext = SqlContext.getContext();
		sqlContext.setSql(sql.toString());
		sqlContext.setParams(returnList.toArray());
		return sqlContext;
	}
}
