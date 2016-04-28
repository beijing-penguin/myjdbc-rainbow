package org.dc.jdbc.core.base;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class JdbcSuper {
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
	/*protected Object getValueByObjectType(ResultSetMetaData metaData,ResultSet rs,int index) throws Exception{
		String typeName = metaData.getColumnTypeName(index+1);
		Object cols_value = rs.getObject(index+1);
		if(cols_value!=null && typeName.equals("TINYINT")){
			cols_value = rs.getInt(index+1);
		}
		return cols_value;
	}*/

}
