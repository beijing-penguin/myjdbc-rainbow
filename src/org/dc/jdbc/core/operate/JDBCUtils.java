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
import org.dc.jdbc.core.GlobalCache;

public class JDBCUtils {
	private static final Log LOG = LogFactory.getLog(JDBCUtils.class);
	public static void close(PreparedStatement ps,ResultSet rs){
		close(rs);
		close(ps);
	}
	public static void close(AutoCloseable ac){
		if(ac!=null){
			try{
				ac.close();
			}catch (Exception e) {
				LOG.error("",e);
			}
		}
	}
	protected static void setParams(PreparedStatement ps, Object[] params) throws Exception {
		for (int i = 0; i < params.length; i++) {
			ps.setObject(i+1, params[i]);
		}
	}
	protected static Object getValueByObjectType(ResultSetMetaData metaData,ResultSet rs,int index) throws Exception{
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
			setParams(ps, params);
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
	public static List<Object> parseSqlResultToListMap(ResultSet rs) throws Exception{
		List<Object> list = new ArrayList<>();
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		while(rs.next()){
			Map<String, Object> map = new LinkedHashMap<String, Object>(cols_len,1);
			for(int i=0; i<cols_len; i++){  
				String cols_name = metaData.getColumnLabel(i+1);
				Object cols_value = getValueByObjectType(metaData, rs, i);
				map.put(cols_name, cols_value);
			}
			list.add(map);
		}
		return list;
	}
	/**
	 * 将sql查询结果转化成Map
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	public static Map<?,?> parseSqlResultToMap(ResultSet rs) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		Map<String, Object> map = new LinkedHashMap<String, Object>(cols_len,1);
		for(int i=0; i<cols_len; i++){
			String cols_name = metaData.getColumnLabel(i+1);  
			Object cols_value = getValueByObjectType(metaData, rs, i);
			map.put(cols_name, cols_value);
		}
		return (Map<?,?>)map;
	}
	/**
	 * 将sql查询结果转化成对象
	 * @param <T>
	 * @param rs
	 * @param cls
	 * @param list
	 * @throws Exception
	 */
	public static List<Object> parseSqlResultToListObject(ResultSet rs,Class<?> cls) throws Exception{
		List<Object> list = new ArrayList<Object>();
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		Map<String,Field> fieldsMap = GlobalCache.getCacheFields(cls);
		while(rs.next()){
			Object obj_newInsten = cls.newInstance();
			for(int i = 0; i<cols_len; i++){
				String cols_name = metaData.getColumnLabel(i+1);  
				Field field = fieldsMap.get(cols_name);
				if(field!=null){
					Object cols_value =  getValueByObjectType(metaData, rs, i);

					field.setAccessible(true);
					field.set(obj_newInsten, cols_value);
				}
			}
			list.add(obj_newInsten);
		}
		return list;
	}
	public static Object parseSqlResultToObject(ResultSet rs,Class<?> cls) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();

		Object obj_newInsten = cls.newInstance();
		Map<String,Field> fieldsMap = GlobalCache.getCacheFields(cls);
		for(int i = 0; i<cols_len; i++){
			String cols_name = metaData.getColumnLabel(i+1);
			Field field = fieldsMap.get(cols_name);
			if(field!=null){
				Object cols_value = getValueByObjectType(metaData, rs, i);
				field.setAccessible(true);
				field.set(obj_newInsten, cols_value);
			}
		}
		return obj_newInsten;
	}
	public static List<Object> parseSqlResultToListBaseType(ResultSet rs) throws Exception{
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
		return list;
	}
	public static Object parseSqlResultToBaseType(ResultSet rs) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();//列数
		if(cols_len>1){
			throw new Exception("The number of returned data columns is too many");
		}
		Object cols_value = getValueByObjectType(metaData, rs, 0);

		return cols_value;
	}
}
