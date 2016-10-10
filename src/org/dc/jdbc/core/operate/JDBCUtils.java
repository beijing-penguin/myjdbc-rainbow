package org.dc.jdbc.core.operate;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	private static List<Object> parseSqlResultToListMap(ResultSet rs) throws Exception{
		List<Object> list = new ArrayList<Object>();
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		while(rs.next()){
			list.add(JDBCUtils.getMap(rs, metaData, cols_len));
		}
		return list;
	}
	/**
	 * 将sql查询结果转化成Map
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	private static Map<?,?> parseSqlResultToMap(ResultSet rs) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		
		return JDBCUtils.getMap(rs, metaData, cols_len);
	}
	/**
	 * 将sql查询结果转化成对象
	 * @param <T>
	 * @param rs
	 * @param cls
	 * @param list
	 * @throws Exception
	 */
	private static List<Object> parseSqlResultToListObject(ResultSet rs,Class<?> cls) throws Exception{
		List<Object> list = new ArrayList<Object>();
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		while(rs.next()){
			list.add(JDBCUtils.getObject(rs, metaData, cls, cols_len));
		}

		return list;
	}
	private static Object parseSqlResultToObject(ResultSet rs,Class<?> cls) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		return JDBCUtils.getObject(rs, metaData, cls, cols_len);
	}
	public static List<Object> parseSqlResultToListBaseType(ResultSet rs) throws Exception{
		List<Object> list = new ArrayList<Object>();
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		if(cols_len>1){
			throw new Exception("The number of returned data columns is too many");
		}
		while(rs.next()){
			Object cols_value = JDBCUtils.getValueByObjectType(metaData, rs, 0);
			list.add(cols_value);
		}
		return list;
	}
	private static Object parseSqlResultToBaseType(ResultSet rs) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();//列数
		if(cols_len>1){
			throw new Exception("The number of returned data columns is too many");
		}
		Object cols_value = JDBCUtils.getValueByObjectType(metaData, rs, 0);
		
		return cols_value;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T  parseSqlResultOne(ResultSet rs, Class<? extends T> cls) throws Exception {
		int row = 0;
		if(rs.last() && (row = rs.getRow())>1){
			throw new Exception("Query results too much!");
		}
		if(row == 0){
			return null;
		}
		if(cls==null || Map.class.isAssignableFrom(cls)){
			return (T) parseSqlResultToMap(rs);
		}else{
			if(cls.getClassLoader()==null){//java基本类型
				return  (T)parseSqlResultToBaseType(rs);
			}else{//java对象
				return (T) parseSqlResultToObject(rs, cls);
			}
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> List<T>  parseSqlResultList(ResultSet rs, Class<? extends T> cls) throws Exception {
		rs.last();
		int rowNum = rs.getRow();
		if(rowNum>0){
			rs.beforeFirst();
			
			if(cls==null || Map.class.isAssignableFrom(cls)){//封装成Map
				return (List<T>) JDBCUtils.parseSqlResultToListMap(rs);
			}else{
				if(cls.getClassLoader()==null){//封装成基本类型
					return (List<T>) JDBCUtils.parseSqlResultToListBaseType(rs);
				}else{//对象
					return (List<T>) JDBCUtils.parseSqlResultToListObject(rs,cls);
				}
			}
		}
		return null;
	}
	
	
	public static void setParams(PreparedStatement ps, Object[] params) throws Exception {
		for (int i = 0; i < params.length; i++) {
			ps.setObject(i+1, params[i]);
		}
	}
	private static Object getObject(ResultSet rs,ResultSetMetaData metaData,Class<?> cls,int cols_len) throws Exception{
		Object obj_newInsten = cls.newInstance();
		for(int i = 0; i<cols_len; i++){
			String cols_name = metaData.getColumnLabel(i+1);  
			Field field  = null;
			try{
				field = obj_newInsten.getClass().getDeclaredField(cols_name);
			}catch (Exception e) {
			}
			if(field!=null){
				Object cols_value =  JDBCUtils.getValueByObjectType(metaData, rs, i);

				field.setAccessible(true);
				field.set(obj_newInsten, cols_value);
			}
		}
		return obj_newInsten;
	}
	private static Map<?,?> getMap(ResultSet rs,ResultSetMetaData metaData,int cols_len) throws Exception{
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		
		for(int i=0; i<cols_len; i++){
			String cols_name = metaData.getColumnLabel(i+1);  
			Object cols_value = JDBCUtils.getValueByObjectType(metaData, rs, i);
			map.put(cols_name, cols_value);
		}
		return map;
	}
	private static Object getValueByObjectType(ResultSetMetaData metaData,ResultSet rs,int index) throws Exception{
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
}
