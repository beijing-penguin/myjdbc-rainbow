package org.dc.jdbc.core.base;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

public class JdbcSuper {
	protected void setParams(PreparedStatement ps, Object[] params) throws Exception {
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i+1, params[i]);
			}
		}
	}
	protected Object getValueByObjectType(ResultSetMetaData metaData,ResultSet rs,int index) throws Exception{
		int columnIndex = index+1;
		Object return_obj = rs.getObject(columnIndex);
		if(return_obj==null){
			return return_obj;
		}else{
			int type = metaData.getColumnType(columnIndex);
			if(type==Types.BIT){//防止原生jdbc将此类型转化成boolean。
				return rs.getByte(columnIndex);
			}
			return rs.getObject(columnIndex);
		}
	}
}
