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
			return null;
		}
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
		return return_obj;
	}
}

/**
Mybatis中javaType和jdbcType对应关系

JDBC Type			Java Type
CHAR				String
VARCHAR				String
LONGVARCHAR			String
NUMERIC				java.math.BigDecimal
DECIMAL				java.math.BigDecimal
BIT				boolean
BOOLEAN				boolean
TINYINT				byte
SMALLINT			short
INTEGER				int
BIGINT				long
REAL				float
FLOAT				double
DOUBLE				double
BINARY				byte[]
VARBINARY			byte[]
LONGVARBINARY		        byte[]
DATE				java.sql.Date
TIME				java.sql.Time
TIMESTAMP			java.sql.Timestamp
CLOB				Clob
BLOB				Blob
ARRAY				Array
DISTINCT			mapping of underlying type
STRUCT				Struct
REF	                        Ref
DATALINK			java.net.URL[color=red][/color]
 **/