package org.dc.jdbc.core.operate;

import java.sql.Connection;
import java.util.List;

public interface IDataBaseDao {
	public <T> List<T> selectList(Connection conn,String sql,Class<? extends T> cls,Object[] params) throws Exception;
	public  <T> T selectOne(Connection conn,String sql,Class<? extends T> cls,Object[] params) throws Exception;
	public int update(Connection conn,String sql,Class<?> returnClass,Object[] params) throws Exception;
	public int insert(Connection conn,String sql,Class<?> returnClass,Object[] params) throws Exception;
	public <T> T insertRtnPKKey(Connection conn,String sql,Class<?> returnClass,Object[] params) throws Exception;
	public  int delete(Connection conn,String sql,Class<?> returnClass,Object[] params) throws Exception;
	public  int excuteSQL(Connection conn,String sql,Class<?> returnClass,Object[] params) throws Exception;
}
