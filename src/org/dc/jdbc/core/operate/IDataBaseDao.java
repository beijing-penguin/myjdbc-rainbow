package org.dc.jdbc.core.operate;

import java.util.List;

public interface IDataBaseDao {
	public <T> List<T> selectList(String sql,Class<? extends T> cls,Object[] params) throws Exception;
	public  <T> T selectOne(String sql,Class<? extends T> cls,Object[] params) throws Exception;
	public int update(String sql,Class<?> returnClass,Object[] params) throws Exception;
	public int insert(String sql,Class<?> returnClass,Object[] params) throws Exception;
	public <T> T insertRtnPKKey(String sql,Class<?> returnClass,Object[] params) throws Exception;
	public  int delete(String sql,Class<?> returnClass,Object[] params) throws Exception;
	public  int excuteSQL(String sql,Class<?> returnClass,Object[] params) throws Exception;
	
	public int updateEntity(Object entity) throws Exception;
	public int insertEntity(Object entity) throws Exception;
	public Object insertEntityRtnPKKey(Object entity) throws Exception;
	public int deleteEntity(Object entity) throws Exception;
}
