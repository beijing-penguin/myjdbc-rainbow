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
	private static List<Object> parseSqlResultToListMap(ResultSet rs) throws Exception{
		List<Object> list = new ArrayList<Object>();
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		while(rs.next()){
			list.add(getMap(rs, metaData, cols_len));
		}
		return list;
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
			list.add(getObject(rs, metaData, cls, cols_len));
		}

		return list;
	}
	/**
	 * 将sql查询结果转化成java基本数据类型
	 * @param rs
	 * @return
	 * @throws Exception
	 */
	private static List<Object> parseSqlResultToListBaseType(ResultSet rs) throws Exception{
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
	/**
	 * 将sql查询结果封装成cls指定的泛型类型返回，且只返回一个结果
	 * @param rs
	 * @param cls
	 * @return
	 * @throws Exception 当查询结果超过一个，或者查询失败时，抛出程序可能出现一切异常
	 */
	public static <T> T  parseSqlResultOne(ResultSet rs, Class<? extends T> cls) throws Exception {
		List<T> list = parseSqlResultList(rs, cls);
		if(list == null){
			return null;
		}
		if(list.size()>1){
			throw new Exception("Query results too much!");
		}
		return list.get(0);
	}
	/**
	 * 将sql查询结果封装成cls指定的泛型类型的集合并
	 * @param rs
	 * @param cls
	 * @return
	 * @throws Exception 抛出程序可能出现一切异常
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T>  parseSqlResultList(ResultSet rs, Class<? extends T> cls) throws Exception {
		List<Object> list = null;
		if(cls==null || Map.class.isAssignableFrom(cls)){//封装成Map
			list = parseSqlResultToListMap(rs);
		}else{
			if(cls.getClassLoader()==null){//封装成基本类型
				list = parseSqlResultToListBaseType(rs);
			}else{//对象
				list = parseSqlResultToListObject(rs, cls);
			}
		}
		if(list==null || list.size()==0){
			return null;
		}else{
			return (List<T>) list;
		}
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
				Object cols_value =  getValueByObjectType(metaData, rs, i);

				field.setAccessible(true);
				field.set(obj_newInsten, cols_value);
			}
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
}
