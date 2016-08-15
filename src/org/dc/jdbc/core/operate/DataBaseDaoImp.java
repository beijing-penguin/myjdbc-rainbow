package org.dc.jdbc.core.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class DataBaseDaoImp implements IDataBaseDao{
	@SuppressWarnings("unchecked")
	@Override
	public  <T> T selectOne(Connection conn,String sql,Class<? extends T> cls,Object[] params) throws Exception{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = JDBCUtils.preparedSQLReturnRS(ps, sql, params);

			int row = 0;
			if(rs.last() && (row = rs.getRow())>1){
				throw new Exception("Query results too much!");
			}
			if(row==1){//判断是否有返回结果，有的话执行下面转化操作
				if(cls==null || Map.class.isAssignableFrom(cls)){
					return (T) JDBCUtils.parseSqlResultToMap(rs);
				}else{
					if(cls.getClassLoader()==null){//java基本类型
						return (T) JDBCUtils.parseSqlResultToBaseType(rs);
					}else{//java对象
						return (T) JDBCUtils.parseSqlResultToObject(rs, cls);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}finally{
			JDBCUtils.close(ps,rs);
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> selectList(Connection conn, String sql, Class<? extends T> cls, Object[] params) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = JDBCUtils.preparedSQLReturnRS(ps, sql, params);
			rs.last();
			int rowNum = rs.getRow();
			if(rowNum>0){
				rs.beforeFirst();
				if(cls==null || Map.class.isAssignableFrom(cls)){//封装成Map
					return (List<T>) JDBCUtils.parseSqlResultToListMap(rs);
				}else{
					if(cls.getClassLoader()==null){//封装成基本类型
						return (List<T>) JDBCUtils.parseSqlResultToListBaseType(rs);
					}else{//对象
						return (List<T>) JDBCUtils.parseSqlResultToListObject(rs,cls);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}finally{
			JDBCUtils.close(ps,rs);
		}
		return null;
	}

	@Override
	public int update(Connection conn, String sql, Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(conn, sql, params);
	}

	@Override
	public int insert(Connection conn, String sql, Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(conn, sql, params);
	}

	@Override
	public int[] insertBatch(Connection conn, String sql, Object[][] params) throws Exception {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			if(params!=null && params.length>0){
				for (int i = 0; i < params.length; i++) {
					JDBCUtils.setParams(ps, params[i]);
					ps.addBatch();
				}
			}
			return ps.executeBatch();
		} catch (Exception e) {
			throw e;
		}finally{
			JDBCUtils.close(ps);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T insertRtnPKKey(Connection conn, String sql, Object[] params) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			JDBCUtils.setParams(ps, params);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			rs.last();
			int rowNum = rs.getRow();
			if(rowNum>0){
				if(rowNum==1){
					return (T) rs.getObject(1);
				}else{
					rs.beforeFirst();
					return (T) JDBCUtils.parseSqlResultToListBaseType(rs);
				}
			}
		} catch (Exception e) {
			throw e;
		}finally{
			JDBCUtils.close(ps,rs);
		}
		return null;
	}

	@Override
	public int delete(Connection conn, String sql, Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(conn, sql, params);
	}
	
}
