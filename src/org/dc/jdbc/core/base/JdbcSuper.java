package org.dc.jdbc.core.base;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.dc.jdbc.core.inter.TypeFactory;

public class JdbcSuper {
	public static volatile TypeFactory typeFactory = null;
	protected void setParams(PreparedStatement ps, Object[] params) throws Exception {
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i+1, params[i]);
			}
		}
	}
	protected Object getValueByObjectType(ResultSetMetaData metaData,ResultSet rs,int index) throws Exception{
		int columnIndex = index+1;
		String typeName = metaData.getColumnTypeName(columnIndex);
		//Object dbvalue = rs.getObject(columnIndex);
		return typeFactory.typeChange(rs,columnIndex, typeName);
	}
}
