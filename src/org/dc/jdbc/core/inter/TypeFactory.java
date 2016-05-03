package org.dc.jdbc.core.inter;

import java.sql.ResultSet;

public interface TypeFactory {
	/**
	 * 数据库字段类型转换
	 * @param databaseValue 数据库原数据
	 * @param typeStr 数据对应的原数据库类型名
	 * @throws Exception
	 */
	public Object typeChange(ResultSet rs,int index,String dbTypeStr) throws Exception;
}
