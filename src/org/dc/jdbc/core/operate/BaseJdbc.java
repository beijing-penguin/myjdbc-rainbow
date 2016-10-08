package org.dc.jdbc.core.operate;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

public class BaseJdbc {
	protected static void setParams(PreparedStatement ps, Object[] params) throws Exception {
		for (int i = 0; i < params.length; i++) {
			ps.setObject(i+1, params[i]);
		}
	}
	protected static Object getObject(ResultSet rs,ResultSetMetaData metaData,Class<?> cls,int cols_len) throws Exception{
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
	protected static Map<?,?> getMap(ResultSet rs,ResultSetMetaData metaData,int cols_len) throws Exception{
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		
		for(int i=0; i<cols_len; i++){
			String cols_name = metaData.getColumnLabel(i+1);  
			Object cols_value = JDBCUtils.getValueByObjectType(metaData, rs, i);
			map.put(cols_name, cols_value);
		}
		return map;
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
}
