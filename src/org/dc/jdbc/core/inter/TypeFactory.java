package org.dc.jdbc.core.inter;

public interface TypeFactory {
	/**
	 * 数据库字段类型转换
	 * @param databaseValue 数据库原数据
	 * @param typeStr 数据对应的原数据库类型名
	 * @throws Exception
	 */
	public void typeChange(Object databaseValue,String dbTypeStr) throws Exception;
}
