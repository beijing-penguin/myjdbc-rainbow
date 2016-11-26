package org.dc.jdbc.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.core.CacheCenter;
import org.dc.jdbc.core.SqlContext;
import org.dc.jdbc.core.entity.ColumnBean;
import org.dc.jdbc.core.entity.TableInfoBean;
/**
 * jdbc api封装成的工具类
 * @author DC
 *
 */
public class JDBCUtils{
	private static final Log LOG = LogFactory.getLog(JDBCUtils.class);

	public static void close(AutoCloseable...ac){
		for (int i = 0; i < ac.length; i++) {
			AutoCloseable autoClose = ac[i];
			if(autoClose!=null){
				try {
					autoClose.close();
				} catch (Exception e) {
					LOG.error("",e);
				}
			}
		}
	}
	/**
	 * 编译sql并执行查询
	 * @param ps
	 * @param sql
	 * @param params
	 * @return 返回结果集对象
	 * @throws Exception
	 */
	public static ResultSet preparedSQLReturnRS(PreparedStatement ps,String sql,Object[] params) throws Exception{
		setParams(ps, params);
		return ps.executeQuery();
	}
	/**
	 * 执行sql语句，返回受影响的行数
	 * @param conn
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static int preparedAndExcuteSQL(Connection conn,String sql,Object[] params) throws Exception{
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			JDBCUtils.setParams(ps, params);
			return ps.executeUpdate();
		} catch (Exception e) {
			throw e;
		}finally{
			close(ps);
		}
	}
	/**
	 * 将sql查询结果转化成map类型的集合
	 * @param rs
	 * @param list
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> parseSqlResultToListMap(ResultSet rs) throws Exception{
		List<Object> list = new ArrayList<Object>();
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		while(rs.next()){
			list.add(getMap(rs, metaData, cols_len));
		}
		if(list.size()==0){
			return null;
		}else{
			return (List<T>) list;
		}
	}
	/**
	 * 将sql查询结果转化成对象
	 * @param <T>
	 * @param rs
	 * @param cls
	 * @param list
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> parseSqlResultToListObject(ResultSet rs,Class<? extends T> cls) throws Exception{
		List<Object> list = new ArrayList<Object>();
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		while(rs.next()){
			list.add(getObject(rs, metaData, cls, cols_len));
		}
		if(list.size()==0){
			return null;
		}else{
			return (List<T>) list;
		}
	}
	/**
	 * 将sql查询结果转化成java基本数据类型
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> parseSqlResultToListBaseType(ResultSet rs) throws Exception{
		List<Object> list = new ArrayList<Object>();
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		if(cols_len>1){
			throw new Exception("The number of returned data columns is too many");
		}
		while(rs.next()){
			Object cols_value = getValueByObjectType(metaData, rs, 0);
			list.add(cols_value);
		}
		if(list.size()==0){
			return null;
		}else{
			return (List<T>) list;
		}
	}
	/**
	 * 将sql查询结果封装成cls指定的泛型类型的集合并
	 * @param rs
	 * @param cls
	 * @return
	 * @throws Exception 抛出程序可能出现一切异常
	 */
	public static <T> List<T>  parseSqlResultList(ResultSet rs, Class<? extends T> cls) throws Exception {
		if(cls==null || Map.class.isAssignableFrom(cls)){//封装成Map
			return parseSqlResultToListMap(rs);
		}else{
			if(cls.getClassLoader()==null){//封装成基本类型
				return parseSqlResultToListBaseType(rs);
			}else{//对象
				return parseSqlResultToListObject(rs, cls);
			}
		}
	}
	public static void setParams(PreparedStatement ps, Object[] params) throws Exception {
		if(params!=null){
			for (int i = 0,len=params.length; i < len; i++) {
				ps.setObject(i+1, params[i]);
			}
		}
	}
	private static Object getObject(ResultSet rs,ResultSetMetaData metaData,Class<?> cls,int cols_len) throws Exception{
		TableInfoBean tabInfo = JDBCUtils.getTableInfo(cls,SqlContext.getContext().getCurrentDataSource());
		List<Field> fieldList = JDBCUtils.getFieldList(cls, tabInfo, false);
		List<ColumnBean> colNameList = CacheCenter.CLASS_SQL_COLNAME_CACHE.get(cls);
		Object obj_newInsten = cls.newInstance();
		for(int i = 0; i<cols_len; i++){
			//String field_name = getBeanName(metaData.getColumnLabel(i+1));
			String col_name = metaData.getColumnLabel(i+1);
			for (int j = 0; j < colNameList.size(); j++) {
				if(colNameList.get(j).getColumnName().equals(col_name)){
					Object cols_value =  getValueByObjectType(metaData, rs, i);
					Field field = fieldList.get(j);
					field.setAccessible(true);
					field.set(obj_newInsten, cols_value);
				}
			}
			/*Field field  = null;
			try{
				field = obj_newInsten.getClass().getDeclaredField(field_name);
			}catch (Exception e) {
			}
			if(field!=null && !Modifier.isStatic(field.getModifiers())){
				Object cols_value =  getValueByObjectType(metaData, rs, i);

				field.setAccessible(true);
				field.set(obj_newInsten, cols_value);
			}*/
		}
		return obj_newInsten;
	}
	private static Map<String, Object> getMap(ResultSet rs,ResultSetMetaData metaData,int cols_len) throws Exception{
		Map<String, Object> map = new LinkedHashMap<String, Object>();

		for(int i=0; i<cols_len; i++){
			String cols_name = metaData.getColumnLabel(i+1);  
			Object cols_value = getValueByObjectType(metaData, rs, i);
			map.put(cols_name, cols_value);
		}
		return map;
	}
	/**
	 * 获取index指定的值，处理java数据类型和数据库类型的转换问题
	 * @param metaData
	 * @param rs
	 * @param index
	 * @return
	 * @throws Exception
	 */
	public static Object getValueByObjectType(ResultSetMetaData metaData,ResultSet rs,int index) throws Exception{
		int columnIndex = index+1;
		Object return_obj = rs.getObject(columnIndex);
		if(return_obj!=null){
			int type = metaData.getColumnType(columnIndex);
			switch (type){
			case Types.BIT:
				return_obj = rs.getByte(columnIndex);
				break;
			case Types.TINYINT:
				return_obj = rs.getByte(columnIndex);
				break;
			case Types.SMALLINT:
				return_obj = rs.getShort(columnIndex);
				break;
			case Types.LONGVARBINARY:
				return_obj = rs.getBytes(columnIndex);
				break;
			default :
				return_obj = rs.getObject(columnIndex);
			}
		}
		return return_obj;
	}
	public static void initDataBaseInfo(DataSource dataSource){
		if(!CacheCenter.DATABASE_INFO_CACHE.containsKey(dataSource)){
			Connection conn = null;
			try {
				List<TableInfoBean> tabList = new ArrayList<TableInfoBean>();
				conn = dataSource.getConnection();
				DatabaseMetaData meta = conn.getMetaData(); 
				ResultSet tablesResultSet = meta.getTables(conn.getCatalog(), null, "%",new String[] { "TABLE" });  
				while(tablesResultSet.next()){
					TableInfoBean tableBean = new TableInfoBean();
					String tableName = tablesResultSet.getString("TABLE_NAME");
					ResultSet colRS = meta.getColumns(conn.getCatalog(), "%", tableName, "%");
					tableBean.setTableName(tableName);
					while(colRS.next()){
						ColumnBean colbean = new ColumnBean();
						String colName = colRS.getString("COLUMN_NAME");
						colbean.setColumnName(colName);
						tableBean.getColumnList().add(colbean);
					}
					//设置主键
					ResultSet primaryKeyResultSet = meta.getPrimaryKeys(conn.getCatalog(),null,tableName);
					while(primaryKeyResultSet.next()){
						String primaryKeyColumnName = primaryKeyResultSet.getString("COLUMN_NAME");
						for (int i = 0; i < tableBean.getColumnList().size(); i++) {
							ColumnBean colbean = tableBean.getColumnList().get(i);
							if(colbean.getColumnName().equals(primaryKeyColumnName)){
								colbean.setPrimaryKey(true);
								break;
							}
						}
					}
					//检查字段名规范
					List<ColumnBean> colList =  tableBean.getColumnList();
					for (int i = 0; i < colList.size(); i++) {
						String col_name = colList.get(i).getColumnName();
						for (int j = i+1; j < colList.size(); j++) {
							if(getBeanName(colList.get(j).getColumnName()).equalsIgnoreCase(getBeanName(col_name))){
								try{
									throw new Exception("field name='"+tableName+"."+col_name+"' is not standard");
								}catch(Exception e ){
									LOG.error("",e);
								}
							}
						}
					}
					//检查表明规范
					for (int i = 0; i < tabList.size(); i++) {
						if(getBeanName(tabList.get(i).getTableName()).equalsIgnoreCase(getBeanName(tableName))){
							try{
								throw new Exception("table name= '"+tabList.get(i).getTableName()+"' is not standard");
							}catch(Exception e ){
								LOG.error("",e);
							}
						}
					}

					tabList.add(tableBean);
				}
				CacheCenter.DATABASE_INFO_CACHE.put(dataSource, tabList);
			} catch (Exception e) {
				LOG.info("",e);
			}finally{
				try {
					if(conn!=null && !conn.isClosed()){
						conn.close();
					}
				} catch (SQLException e) {
					LOG.info("",e);
				}
			}
		}
	}
	/**
	 * 将字符串转化为java bean驼峰命名规范
	 * @param str
	 * @return
	 */
	public static String getBeanName(String str){
		int markIndex = str.lastIndexOf("_");
		if(markIndex!=-1){
			String startStr = str.substring(0, markIndex);
			String endStr = str.substring(markIndex, str.length());
			String newStr = startStr + endStr.substring(1, 2).toUpperCase()+endStr.substring(2);
			return getBeanName(newStr);
		}else{
			return str;
		}
	}
	/**
	 * 将驼峰命名的java字符串转下划线或者其他分隔符(默认分隔符为下划线)
	 * @param str
	 * @return
	 */
	public static String javaBeanToSeparator(String str,Character separatorChar){
		if(str==null || str.length()==0){
			return null;
		}
		if(separatorChar==null){
			separatorChar = '_';
		}
		StringBuilder sb =new StringBuilder(str);
		for (int i = 1; i < str.length(); i++) {
			char c = str.charAt(i);
			if(Character.isUpperCase(c)){
				sb.replace(i, i+1, String.valueOf(c).toLowerCase());
				sb.insert(i, separatorChar);
			}
		}
		return sb.toString().toLowerCase();
	}

	public static TableInfoBean getTableInfo(Class<?> entityClass,DataSource dataSource) throws Exception{
		TableInfoBean tabInfo = CacheCenter.SQL_TABLE_CACHE.get(entityClass);
		if(tabInfo==null){
			String className = entityClass.getSimpleName();
			String class_tabName = javaBeanToSeparator(className, null);
			List<TableInfoBean> db_tabList = CacheCenter.DATABASE_INFO_CACHE.get(dataSource);
			for (int i = 0; i < db_tabList.size(); i++) {
				TableInfoBean db_tabInfo = db_tabList.get(i);
				String tabname = db_tabInfo.getTableName();
				if(tabname.equalsIgnoreCase(class_tabName)){
					tabInfo =  db_tabInfo;
					break;
				}
			}
			if(tabInfo == null){
				throw new Exception("table "+JDBCUtils.javaBeanToSeparator(entityClass.getSimpleName(), null)+" is not exist");
			}
			CacheCenter.SQL_TABLE_CACHE.put(entityClass, tabInfo);
		}
		return tabInfo;
	}
	public static List<Field> getFieldList(Class<?> entityClass,TableInfoBean tabInfo,boolean ischeckPK) throws Exception{
		List<Field> fieldList =  CacheCenter.CLASS_SQL_FIELD_CACHE.get(entityClass);
		List<ColumnBean> colNameList = CacheCenter.CLASS_SQL_COLNAME_CACHE.get(entityClass);
		if(fieldList==null){
			fieldList = new ArrayList<Field>();
			colNameList = new ArrayList<ColumnBean>();

			Field[] fieldArr = entityClass.getDeclaredFields();
			Field pk_field = null;
			ColumnBean col_pk = null;
			for (int i = 0; i < fieldArr.length; i++) {
				Field field = fieldArr[i];
				if(!Modifier.isStatic(field.getModifiers())){//去除静态类型字段
					String fdName = field.getName();
					for (int j = 0; j < tabInfo.getColumnList().size(); j++) {
						ColumnBean col = tabInfo.getColumnList().get(j);
						if(fdName.equalsIgnoreCase(col.getColumnName()) || JDBCUtils.getBeanName(col.getColumnName()).equalsIgnoreCase(fdName)){
							if(col.isPrimaryKey()){
								pk_field = field;
								if(col_pk!=null){
									throw new Exception("primary key ="+col_pk.getColumnName()+" is too many.Make sure there is only one primary key.");
								}
								col_pk = col;
								break;
							}else{
								colNameList.add(col);
								fieldList.add(field);
							}
						}
					}
				}
			}
			if(ischeckPK && col_pk==null ){
				throw new Exception("primary key is not exist");
			}else{
				if(col_pk!=null){
					colNameList.add(col_pk);
					fieldList.add(pk_field);
				}
			}
			CacheCenter.CLASS_SQL_FIELD_CACHE.put(entityClass, fieldList);
			CacheCenter.CLASS_SQL_COLNAME_CACHE.put(entityClass, colNameList);
		}
		return fieldList;
	}
}
