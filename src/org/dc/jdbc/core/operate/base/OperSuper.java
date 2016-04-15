package org.dc.jdbc.core.operate.base;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.utils.ClassesUtils;
/**
 * 元操作父类，所有操作继承此类
 * @author DC
 */
public abstract class OperSuper {
	private static Log jdbclog = LogFactory.getLog(OperSuper.class);
	public void close(PreparedStatement ps,ResultSet rs){
		if(rs!=null){
			try{
				rs.close();
				rs = null;
			}catch (Exception e) {
				jdbclog.error("",e);
			}
		}
		if(ps!=null){
			try{
				ps.close();
				ps = null;
			}catch (Exception e) {
				jdbclog.error("",e);
			}
		}
	}
	public void close(PreparedStatement ps){
		if(ps!=null){
			try{
				ps.close();
				ps = null;
			}catch (Exception e) {
				jdbclog.error("",e);
			}
		}
	}
	public void close(ResultSet rs){
		if(rs!=null){
			try{
				rs.close();
				rs = null;
			}catch (Exception e) {
				jdbclog.error("",e);
			}
		}
	}
	public int preparedAndExcuteSQL(Connection conn,String sql,Object[] params) throws Exception{
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			if(params!=null && params.length>0){
				for (int i = 0; i < params.length; i++) {
					ps.setObject(i+1, params[i]);
				}
			}
			return ps.executeUpdate();
		} catch (Exception e) {
			throw e;
		}finally{
			this.close(ps);
		}
	}
	public ResultSet preparedSQLReturnRS(PreparedStatement ps,String sql,Object[] params) throws Exception{
		if(params!=null && params.length>0){
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i+1, params[i]);
			}
		}
		return ps.executeQuery();
	}
	public Map<?, ?> parseSqlResultToMap(ResultSet rs,int cols_len,ResultSetMetaData metaData) throws Exception{
		Map<String, Object> map = new HashMap<String, Object>(cols_len,1);
		for(int i=0; i<cols_len; i++){  
			String cols_name = metaData.getColumnLabel(i+1);  
			Object cols_value = rs.getObject(cols_name);  
			map.put(cols_name, cols_value);
		}
		return  map;
	}
	public Object parseSqlResultToObject(ResultSet rs,Class<?> cls,int cols_len,ResultSetMetaData metaData) throws Exception{
		Object obj_newInsten = cls.newInstance();
		Field[] fields = cls.getDeclaredFields();
		for(int i = 0; i<cols_len; i++){
			String cols_name = metaData.getColumnLabel(i+1);  
			Object cols_value = rs.getObject(cols_name);
			for (int j = 0,j_len=fields.length; j < j_len; j++) {
				Field fd = fields[j];
				if(fd.getName().equals(cols_name)){
					fd.setAccessible(true);
					fd.set(obj_newInsten,ClassesUtils.convert(fd.getType(),cols_value));
					break;
				}
			}
		}
		return  obj_newInsten;
	}
}
