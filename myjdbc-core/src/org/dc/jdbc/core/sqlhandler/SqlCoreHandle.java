package org.dc.jdbc.core.sqlhandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.dc.jdbc.core.SqlContext;
import org.dc.jdbc.core.pojo.ClassRelation;
import org.dc.jdbc.core.pojo.ColumnBean;
import org.dc.jdbc.core.pojo.FieldValue;
import org.dc.jdbc.core.pojo.SqlType;
import org.dc.jdbc.core.pojo.TableInfoBean;
import org.dc.jdbc.core.utils.JDBCUtils;
import org.dc.jdbc.core.utils.ObjectUtils;
import org.dc.jdbc.sqlparse.Lexer;
import org.dc.jdbc.sqlparse.Token;

/**
 * sql核心解析器
 * 
 * @author DC
 *
 */
public class SqlCoreHandle {
	private SqlCoreHandle() {
	}

	/**
	 * 批量处理方法，调用此方法处理请求
	 */
	public static SqlContext handleBatchRequest(String doSql, Object[] params) throws Exception {
		StringBuilder sql = new StringBuilder(doSql);
		LinkedList<String> keyList = new LinkedList<String>();
		Lexer lexer = new Lexer(sql.toString());
		int varType = 0;// 1? 2#
		int lastCharLen = 0;
		while (true) {
			lexer.nextToken();
			Token tok = lexer.token();
			if (tok == Token.EOF) {
				break;
			}
			String str = lexer.stringVal();
			int curpos = lexer.pos();
			if (tok.name == null && tok == Token.VARIANT) {// 异类匹配，这里的异类只有#号，sql编写规范的情况下，不需要判断str.contains("#")
				String key = str.substring(2, str.length() - 1);
				keyList.add(key);
				sql.replace(curpos - str.length() - lastCharLen, curpos - lastCharLen, "?");
				lastCharLen = lastCharLen + str.length() - 1;

				varType = 2;
			} else if (tok == Token.QUES) {
				varType = 1;
				break;
			}
		}
		List<Object> returnList = new ArrayList<Object>();
		if (params != null && params.length > 0) {

			switch (varType) {
			case 1:
				for (int i = 0; i < params.length; i++) {
					Object p = params[i];
					if (Collection.class.isAssignableFrom(p.getClass())) {
						returnList.add(((Collection<?>) p).toArray());
					} else if (Object[].class.isAssignableFrom(p.getClass())) {
						returnList.add(p);
					}
				}
				break;
			case 2:
				for (int i = 0; i < params.length; i++) {
					Object p = params[i];
					Map<Object, Object> tempMap = new HashMap<Object, Object>();
					if (Map.class.isAssignableFrom(p.getClass())) {
						Map<?, ?> paramMap = (Map<?, ?>) p;
						for (Object key : paramMap.keySet()) {
							tempMap.put(key, paramMap.get(key));
						}
					} else { // java对象
						Field[] fields = p.getClass().getDeclaredFields();
						for (Field field : fields) {
							field.setAccessible(true);
							Object value = field.get(p);
							String key = field.getName();
							if (tempMap.containsKey(key)) {
								throw new Exception("key=" + key + " is already repeated");
							} else {
								tempMap.put(key, value);
							}
						}
					}

					Object[] pp = new Object[keyList.size()];
					for (int j = 0; j < keyList.size(); j++) {
						String key = keyList.get(j);
						if (!tempMap.containsKey(key)) {
							throw new Exception(
									"sqlhandle analysis error! parameters \"" + key + "\" do not match to!");
						} else {
							pp[j] = tempMap.get(key);
						}
					}

					returnList.add(pp);
				}
				break;
			default:
				break;
			}
		}
		SqlContext sqlContext = SqlContext.getContext();
		sqlContext.setSql(sql.toString());
		sqlContext.setParamList(returnList);
		return sqlContext;
	}

	/**
	 * 一般处理方法，调用此方法处理请求
	 */
	public static SqlContext handleRequest(String doSql, Object... params) throws Exception {
		List<Object> returnList = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder(doSql);

		Map<Object, Object> allparamMap = null;
		List<Object> allParamList = null;
		if (params != null && params.length > 0) {
			allparamMap = new HashMap<Object, Object>();
			allParamList = new ArrayList<Object>();
			for (Object param : params) {
				if (param == null) {
					allParamList.add(param);
				} else if (Map.class.isAssignableFrom(param.getClass())) {
					Map<?, ?> paramMap = (Map<?, ?>) param;
					for (Object key : paramMap.keySet()) {
						if (allparamMap.containsKey(key)) {
							throw new Exception("key=" + key + " is already repeated");
						} else {
							allparamMap.put(key, paramMap.get(key));
						}
					}
				} else if (Collection.class.isAssignableFrom(param.getClass())) {
					allParamList.addAll((Collection<?>) param);
				} else if (Object[].class.isAssignableFrom(param.getClass())) {
					Object[] p = (Object[]) param;
					for (int i = 0; i < p.length; i++) {
						allParamList.add(p[i]);
					}
				} else if (param.getClass().getClassLoader() == null) {
					allParamList.add(param);
				} else { // java对象
					Field[] fields = param.getClass().getDeclaredFields();
					for (Field field : fields) {
						field.setAccessible(true);
						Object value = field.get(param);
						String key = field.getName();
						if (allparamMap.containsKey(key)) {
							throw new Exception("key=" + key + " is already repeated");
						} else {
							allparamMap.put(key, value);
						}
					}
				}
			}
		}
		Lexer lexer = new Lexer(sql.toString());
		int lastCharLen = 0;
		while (true) {
			lexer.nextToken();
			Token tok = lexer.token();
			if (tok == Token.EOF) {
				break;
			}
			String str = lexer.stringVal();
			int curpos = lexer.pos();
			if (tok.name == null && tok == Token.VARIANT) {// 异类匹配，这里的异类只有#号，sql编写规范的情况下，不需要判断str.contains("#")
				String key = str.substring(2, str.length() - 1);
				if (allparamMap != null && !allparamMap.containsKey(key)) {
					throw new Exception("sqlhandle analysis error! parameters \"" + key + "\" do not match to!");
				}
				Object value = allparamMap.get(key);
				returnList.add(value);
				sql.replace(curpos - str.length() - lastCharLen, curpos - lastCharLen, "?");
				lastCharLen = lastCharLen + str.length() - 1;
			} else if (tok == Token.QUES) {
				if (allParamList != null) {
					returnList = allParamList;
				}
				break;
			}
		}
		SqlContext sqlContext = SqlContext.getContext();
		sqlContext.setSql(sql.toString());
		sqlContext.setParamList(returnList);

		return sqlContext;
	}

	/**
	 * 处理update对象请求
	 * 
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static SqlContext handleUpdateRequest(Object entity) throws Exception {
		SqlContext sqlContext = SqlContext.getContext();
		Class<?> entityClass = entity.getClass();
		TableInfoBean tabInfo = JDBCUtils.getTableInfoByClass(entityClass, sqlContext.getCurrentDataSource());
		if(tabInfo==null){
			throw new Exception("table is not exist");
		}
		List<ClassRelation> classRelationsList = JDBCUtils.getClassRelationList(entityClass, tabInfo);

		String sql = "UPDATE " + tabInfo.getTableName() + " SET ";
		List<Object> paramsList = new ArrayList<Object>();
		List<Object> paramsPKList = new ArrayList<Object>(3);
		String wheresql = null;
		for (int i = 0, len = classRelationsList.size(); i < len; i++) {
			ColumnBean colBean = classRelationsList.get(i).getColumnBean();
			Field field = classRelationsList.get(i).getField();
			field.setAccessible(true);
			Object value = field.get(entity);

			if(value!=null){
				if (!colBean.isPrimaryKey()) {
					sql = sql + colBean.getColumnName() + "=" + "?,";
					paramsList.add(value);
				} else {
					if (wheresql == null) {
						wheresql = new String(" WHERE " + colBean.getColumnName() + "=?");
					} else {
						wheresql = wheresql + " AND " + colBean.getColumnName() + "=?";
					}
					paramsPKList.add(value);
				}
			}
		}
		if (paramsPKList.size() == 0) {
			throw new Exception("primary key is not exist");
		}
		if (paramsList.size() == 0) {
			throw new Exception("No update parameters");
		}
		sqlContext.setSql(sql.substring(0, sql.length() - 1) + wheresql);
		paramsList.addAll(paramsPKList);
		sqlContext.setParamList(paramsList);

		return sqlContext;
	}

	/**
	 * 处理update对象请求
	 * 
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public static SqlContext handleInsertRequest(Object entity) throws Exception {
		Class<?> entityClass = entity.getClass();
		SqlContext sqlContext = SqlContext.getContext();
		List<Object> paramsList = new ArrayList<Object>();
		TableInfoBean tabInfo = JDBCUtils.getTableInfoByClass(entityClass, sqlContext.getCurrentDataSource());
		if(tabInfo==null){
			throw new Exception("table is not exist");
		}
		List<ClassRelation> classRelationsList = JDBCUtils.getClassRelationList(entityClass, tabInfo);
		String insertSql = "INSERT INTO " + tabInfo.getTableName() + " (";
		String sql_values = " VALUES(";
		for (int i = 0, len = classRelationsList.size(); i < len; i++) {
			Field field = classRelationsList.get(i).getField();
			field.setAccessible(true);
			Object obj_value = field.get(entity);
			FieldValue fieldvalue = null;
			if(obj_value==null){
				fieldvalue = field.getAnnotation(FieldValue.class);
				if(fieldvalue!=null && fieldvalue.sqlType()==SqlType.INSERT && fieldvalue.dbType()==JDBCUtils.getDataBaseType(sqlContext.getCurrentDataSource())){
					obj_value = ObjectUtils.getValueByFieldType(fieldvalue.value(), field.getType());
				}
			}
			if (obj_value != null) {
				insertSql = insertSql + classRelationsList.get(i).getColumnBean().getColumnName() + ",";
				if(fieldvalue!=null  && fieldvalue.sqlScript()){
					sql_values = sql_values + obj_value+",";
				}else{
					sql_values = sql_values + "?,";
					paramsList.add(obj_value);
				}
			}
		}
		/*if (paramsList.size() == 0) {
			throw new Exception("insert condition is empty");
		}*/
		insertSql = insertSql.substring(0, insertSql.length() - 1) + ")"+ sql_values.substring(0, sql_values.length() - 1) + ")";

		sqlContext.setParamList(paramsList);
		sqlContext.setSql(insertSql);
		return sqlContext;
	}
	public static SqlContext handleDeleteRequest(Object entity) throws Exception {
		Class<?> entityClass = entity.getClass();
		SqlContext sqlContext = SqlContext.getContext();
		TableInfoBean tabInfo = JDBCUtils.getTableInfoByClass(entityClass, sqlContext.getCurrentDataSource());
		if(tabInfo==null){
			throw new Exception("table is not exist");
		}
		List<ClassRelation> classRelationsList = JDBCUtils.getClassRelationList(entityClass, tabInfo);

		String wheresql = null;
		int pk_num = 0;
		List<Object> paramList = new ArrayList<Object>();
		for (int i = 0, len = classRelationsList.size(); i < len; i++) {
			ClassRelation classRel = classRelationsList.get(i);
			if (classRel.getColumnBean().isPrimaryKey()) {
				pk_num++;
				if (wheresql == null) {
					wheresql = new String(" WHERE " + classRel.getColumnBean().getColumnName() + "=?");
				} else {
					wheresql = wheresql + " AND " + classRel.getColumnBean().getColumnName() + "=?";
				}

				Field field = classRel.getField();
				field.setAccessible(true);
				Object value = field.get(entity);
				if (value != null) {
					paramList.add(value);
				}
			}
		}
		if (paramList.size() == 0 || pk_num != paramList.size()) {
			throw new Exception("primary key set error");
		}
		sqlContext.setSql("DELETE FROM " + tabInfo.getTableName() + wheresql);
		sqlContext.setParamList(paramList);

		return sqlContext;
	}

	public static SqlContext handleSelectRequest(Object entity, Object whereSql, Object params) throws Exception {
		Class<?> entityClass = entity.getClass();
		SqlContext sqlContext = SqlContext.getContext();
		TableInfoBean tabInfo = JDBCUtils.getTableInfoByClass(entityClass, sqlContext.getCurrentDataSource());
		if(tabInfo==null){
			throw new Exception("table is not exist");
		}
		List<ClassRelation> classRelationsList = JDBCUtils.getClassRelationList(entityClass, tabInfo);
		String sql = "SELECT * FROM " + tabInfo.getTableName() + " WHERE 1=1 ";
		if (whereSql != null) {
			String tempsql = sql + whereSql;
			sqlContext = handleRequest(tempsql, params);
		}
		List<Object> valuesList = new ArrayList<Object>();
		for (int i = 0, len = classRelationsList.size(); i < len; i++) {
			ClassRelation classRelation = classRelationsList.get(i);
			Field field = classRelation.getField();
			field.setAccessible(true);
			Object value = field.get(entity);
			if (value != null) {
				sql = sql + " AND " + classRelation.getColumnBean().getColumnName() + "=?";
				valuesList.add(value);
			}
		}
		if (whereSql != null) {
			sql = sql + whereSql;
			valuesList.addAll(sqlContext.getParamList());
		}
		sqlContext.setSql(sql);
		sqlContext.setParamList(valuesList);
		return sqlContext;
	}
}
