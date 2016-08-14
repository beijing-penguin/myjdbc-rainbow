package org.dc.jdbc.core.sqlhandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dc.jdbc.entity.SqlContext;

import com.alibaba.druid.sql.parser.Lexer;
import com.alibaba.druid.sql.parser.Token;

public class XmlSqlHandler{
	private static final XmlSqlHandler oper = new XmlSqlHandler();
	public static XmlSqlHandler getInstance(){
		return oper;
	}
	/**
	 * 处理方法，调用此方法处理请求
	 */
	public SqlContext handleRequest(String doSql,Object[] params) throws Exception{
		SqlContext sqlContext = SqlContext.getContext();
		List<Object> returnList = new ArrayList<Object>();
		Set<String> tableSet =new HashSet<String>();
		StringBuffer sql = new StringBuffer(doSql);

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
					allparamMap.putAll(paramMap);
				}else if(Object[].class.isAssignableFrom(param.getClass())){
					Collections.addAll(allParamList, (Object[])param);
				}else if(Collection.class.isAssignableFrom(param.getClass())){
					allParamList.addAll((Collection<?>) param);
				}else if(param.getClass().getClassLoader()==null){
					allParamList.add(param);
				}else { // java对象
					Field[] fields = param.getClass().getDeclaredFields();
					for (Field field : fields) {
						field.setAccessible(true);
						Object value = field.get(param);
						allparamMap.put(field.getName(), value);
					}
				}
			}
		}
		Lexer lexer = new Lexer(sql.toString());
		int lastCharLen = 0;
		Token lastTok = null;
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
				if(allparamMap!=null && allparamMap.containsKey(key)){
					returnList.add(allparamMap.get(key));
				}else{
					throw new Exception("sqlhandle analysis error! parameters \""+key+"\" do not match to!");
				}
				sql.replace(curpos-str.length()-lastCharLen, curpos-lastCharLen, "?");
				lastCharLen = lastCharLen+str.length()-1;
			}else if(tok == Token.QUES){
				if(allParamList!=null){
					returnList = allParamList;
				}
			}else if(tok == Token.FROM || tok == Token.IDENTIFIER || tok == Token.INTO || tok == Token.UPDATE || tok == Token.USER){//记录表名
				if((lastTok != null && tok== Token.IDENTIFIER || tok== Token.USER) && (lastTok == Token.FROM || lastTok == Token.INTO || lastTok == Token.UPDATE)){
					tableSet.add(lexer.stringVal());
				}
				lastTok = tok;
			}
		}
		sqlContext.setSql(sql.toString());
		sqlContext.setParams(returnList.toArray());
		sqlContext.setTables(tableSet);
		return sqlContext;
	}
}
