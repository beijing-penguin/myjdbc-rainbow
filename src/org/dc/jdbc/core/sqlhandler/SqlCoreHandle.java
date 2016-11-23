package org.dc.jdbc.core.sqlhandler;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dc.jdbc.core.CacheCenter;
import org.dc.jdbc.core.SqlContext;
import org.dc.jdbc.core.entity.ColumnBean;
import org.dc.jdbc.core.entity.TableInfoBean;
import org.dc.jdbc.core.utils.JDBCUtils;
import org.dc.jdbc.sqlparse.Lexer;
import org.dc.jdbc.sqlparse.Token;

/**
 * sql核心解析器
 * @author DC
 *
 */
public class SqlCoreHandle{
	private SqlCoreHandle(){}
	/**
	 * 处理方法，调用此方法处理请求
	 */
	public static SqlContext handleRequest(String doSql,Object...params) throws Exception{
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
				break;
			}
		}
		SqlContext sqlContext = SqlContext.getContext();
		sqlContext.setSql(sql.toString());
		sqlContext.setParams(returnList.toArray());
		return sqlContext;
	}
	/**
	 * 处理update对象请求
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static SqlContext handleUpdateRequest(Object entity) throws Exception{
		Class<?> entityClass = entity.getClass();
		TableInfoBean tabInfo = CacheCenter.UPDATE_SQL_TABLE_CACHE.get(entityClass);
		if(tabInfo==null){
			tabInfo = JDBCUtils.getTableInfo(entityClass.getSimpleName(), SqlContext.getContext().getCurrentDataSource());
			CacheCenter.UPDATE_SQL_TABLE_CACHE.put(entityClass, tabInfo);
		}
		if(tabInfo == null){
			throw new Exception("table "+JDBCUtils.javaBeanToSeparator(entityClass.getSimpleName(), null)+" is not exist");
		}
		List<Field> fieldList =  CacheCenter.UPDATE_SQL_FIELD_CACHE.get(entityClass);
		List<String> colNameList = CacheCenter.UPDATE_SQL_COLNAME_CACHE.get(entityClass);
		if(fieldList==null){
			fieldList = new ArrayList<Field>();
			colNameList = new ArrayList<String>();
			
			Field[] fieldArr = entityClass.getDeclaredFields();
			Field pk_field = null;
			String col_pk_name = null;
			for (int i = 0; i < fieldArr.length; i++) {
				Field field = fieldArr[i];
				if(!Modifier.isStatic(field.getModifiers())){//去除静态类型字段
					String fdName = field.getName();
					for (int j = 0; j < tabInfo.getColumnList().size(); j++) {
						ColumnBean col = tabInfo.getColumnList().get(j);
						if(JDBCUtils.getBeanName(col.getColumnName()).equalsIgnoreCase(fdName)){
							if(col.isPrimaryKey()){
								pk_field = field;
								col_pk_name = col.getColumnName();
								break;
							}else{
								colNameList.add(col.getColumnName());
								fieldList.add(field);
							}
						}
					}
				}
			}
			if(col_pk_name==null){
				throw new Exception("primary key is not exist");
			}
			colNameList.add(col_pk_name);
			fieldList.add(pk_field);

			CacheCenter.UPDATE_SQL_FIELD_CACHE.put(entityClass, fieldList);
			CacheCenter.UPDATE_SQL_COLNAME_CACHE.put(entityClass, colNameList);
		}
		String sql = "UPDATE "+tabInfo.getTableName() +" SET ";
		List<Object> paramsList = new ArrayList<Object>();
		for (int i = 0,len=fieldList.size(); i < len; i++) {
			Field field = fieldList.get(i);
			field.setAccessible(true);
			Object value = field.get(entity);
			if(i==(len-1)){
				sql = sql.substring(0,sql.length()-1) + " WHERE "+colNameList.get(i) +"=?";
				paramsList.add(value);
			}else{
				if(value!=null){
					sql = sql + colNameList.get(i) + "=" +"?,";
					paramsList.add(value);
				}
			}
		}
		
		SqlContext sqlContext = SqlContext.getContext();
		sqlContext.setSql(sql);
		sqlContext.setParams(paramsList.toArray());
		return sqlContext;
	}
	
	/**
	 * 处理update对象请求
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static SqlContext handleInsertRequest(Object entity) throws Exception{
		Class<?> entityClass = entity.getClass();
		TableInfoBean tabInfo = CacheCenter.UPDATE_SQL_TABLE_CACHE.get(entityClass);
		if(tabInfo==null){
			tabInfo = JDBCUtils.getTableInfo(entityClass.getSimpleName(), SqlContext.getContext().getCurrentDataSource());
			CacheCenter.INSERT_SQL_TABLE_CACHE.put(entityClass, tabInfo);
		}
		if(tabInfo == null){
			throw new Exception("table "+JDBCUtils.javaBeanToSeparator(entityClass.getSimpleName(), null)+" is not exist");
		}
		SqlContext sqlContext = SqlContext.getContext();
		Class<?> entityClass = entity.getClass();
		String insertSql = CacheCenter.INSERT_SQL_CACHE.get(entityClass);
		if(insertSql!=null){
			sqlContext.setSql(insertSql);
		}
		TableInfoBean tabInfo = getTableInfo(entityClass.getSimpleName(), dataSource);
		if(tabInfo == null){
			throw new Exception("table is null");
		}
		Field[] fieldArr = entityClass.getDeclaredFields();
		String sql = "INSERT INTO "+tabInfo.getTableName() +" (";
		String values = "(";
		for (int i = 0; i < fieldArr.length; i++) {
			Field field = fieldArr[i];
			if(!Modifier.isStatic(field.getModifiers())){//去除静态类型字段
				String fdName = field.getName();
				String colName = fdName;
				if(tabInfo!=null){
					for (int j = 0; j < tabInfo.getColumnList().size(); j++) {
						ColumnBean col = tabInfo.getColumnList().get(j);
						if(getBeanName(col.getColumnName()).equals(getBeanName(fdName))){
							colName = col.getColumnName().toUpperCase();
						}
					}
				}
				sql = sql + colName + ",";

				values = values+"#{"+fdName+"},";
			}
		}
		insertSql = sql.substring(0, sql.length()-1)+")"+" VALUES "+values.substring(0, values.length()-1)+")";
		CacheCenter.INSERT_SQL_CACHE.put(entityClass, insertSql);
	}
}
