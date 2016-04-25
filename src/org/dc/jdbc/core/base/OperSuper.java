package org.dc.jdbc.core.base;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.core.GlobalCache;
/**
 * 元操作父类，所有操作继承此类
 * @author DC
 */
public abstract class OperSuper{
	private static final Log jdbclog = LogFactory.getLog(OperSuper.class);
	public void close(PreparedStatement ps,ResultSet rs){
		close(rs);
		close(ps);
	}
	public void close(AutoCloseable ac){
		if(ac!=null){
			try{
				if(ac!=null){
					ac.close();
				}
			}catch (Exception e) {
				jdbclog.error("",e);
			}
		}
	}
	protected void setParams(PreparedStatement ps, Object[] params) throws Exception {
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i+1, params[i]);
			}
		}
	}
	protected Object getValueByObjectType(ResultSetMetaData metaData,ResultSet rs,String cols_name,int index) throws Exception{
		String typeName = metaData.getColumnTypeName(index+1);
		Object cols_value = rs.getObject(cols_name);
		if(cols_value!=null && typeName.equals("TINYINT")){
			cols_value = rs.getInt(cols_name);
		}
		return cols_value;
	}
	protected Object getValueByObjectType(ResultSetMetaData metaData,ResultSet rs,int index) throws Exception{
		String typeName = metaData.getColumnTypeName(index+1);
		Object cols_value = rs.getObject(index+1);
		if(cols_value!=null && typeName.equals("TINYINT")){
			cols_value = rs.getInt(index+1);
		}
		return cols_value;
	}
	public int preparedAndExcuteSQL(Connection conn,String sql,Object[] params) throws Exception{
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			this.setParams(ps, params);
			return ps.executeUpdate();
		} catch (Exception e) {
			throw e;
		}finally{
			this.close(ps);
		}
	}
	public ResultSet preparedSQLReturnRS(PreparedStatement ps,String sql,Object[] params) throws Exception{
		this.setParams(ps, params);
		return ps.executeQuery();
	}
	public void parseSqlResultToMap(ResultSet rs,List<Object> list) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		while(rs.next()){
			Map<String, Object> map = new HashMap<String, Object>(cols_len,1);
			for(int i=0; i<cols_len; i++){  
				String cols_name = metaData.getColumnLabel(i+1);
				Object cols_value = this.getValueByObjectType(metaData, rs, cols_name, i);
				map.put(cols_name, cols_value);
			}
			list.add(map);
		}
	}
	public Map<?,?> parseSqlResultToMap(ResultSet rs) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		Map<String, Object> map = new HashMap<String, Object>(cols_len,1);
		for(int i=0; i<cols_len; i++){  
			String cols_name = metaData.getColumnLabel(i+1);  
			Object cols_value = this.getValueByObjectType(metaData, rs, cols_name, i);
			map.put(cols_name, cols_value);
		}
		return map;
	}
	public void parseSqlResultToObject(ResultSet rs,Class<?> cls,List<Object> list) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		Map<String,Field> fieldsMap = GlobalCache.getCacheFields(cls);
		while(rs.next()){
			Object obj_newInsten = cls.newInstance();
			for(int i = 0; i<cols_len; i++){
				String cols_name = metaData.getColumnLabel(i+1);  
				Field field = fieldsMap.get(cols_name);
				if(field!=null){
					Object cols_value =  this.getValueByObjectType(metaData, rs, cols_name, i);
					
					field.setAccessible(true);
					field.set(obj_newInsten, cols_value);
				}
			}
			list.add(obj_newInsten);
		}
	}
	public Object parseSqlResultToObject(ResultSet rs,Class<?> cls) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();

		Object obj_newInsten = cls.newInstance();
		Map<String,Field> fieldsMap = GlobalCache.getCacheFields(cls);
		for(int i = 0; i<cols_len; i++){
			String cols_name = metaData.getColumnLabel(i+1);
			Field field = fieldsMap.get(cols_name);
			if(field!=null){
				Object cols_value = this.getValueByObjectType(metaData, rs, cols_name, i);
				field.setAccessible(true);
				field.set(obj_newInsten, cols_value);
			}
		}
		return obj_newInsten;
	}
	public void parseSqlResultToBaseType(ResultSet rs,List<Object> list) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();
		if(cols_len>1){
			throw new Exception("The number of returned data columns is too many");
		}
		while(rs.next()){
			for(int i=0; i<cols_len; i++){  
				Object cols_value = this.getValueByObjectType(metaData, rs, i);
				list.add(cols_value);
			}
		}
	}
	public Object parseSqlResultToBaseType(ResultSet rs) throws Exception{
		ResultSetMetaData metaData  = rs.getMetaData();
		int cols_len = metaData.getColumnCount();//列数
		if(cols_len>1){
			throw new Exception("The number of returned data columns is too many");
		}
		Object cols_value = this.getValueByObjectType(metaData, rs, 0);
		return cols_value;
	}
}
