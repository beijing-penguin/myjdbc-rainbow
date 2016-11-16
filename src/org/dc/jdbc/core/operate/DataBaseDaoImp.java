package org.dc.jdbc.core.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.dc.jdbc.core.utils.JDBCUtils;
public class DataBaseDaoImp implements IDataBaseDao{
	private DataBaseDaoImp(){}
	
	private static final DataBaseDaoImp INSTANCE = new DataBaseDaoImp();
	public static DataBaseDaoImp getInstance(){
		return INSTANCE;
	}
	@Override
	public  <T> T selectOne(Connection conn,String sql,Class<? extends T> cls,Object[] params) throws Exception{
		List<T> list = this.selectList(conn, sql, cls, params);
		if(list == null){
			return null;
		}
		if(list.size()>1){
			throw new Exception("Query results too much!");
		}
		return list.get(0);
	}
	@Override
	public <T> List<T> selectList(Connection conn, String sql, Class<? extends T> cls, Object[] params) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = JDBCUtils.preparedSQLReturnRS(ps, sql, params);

			return JDBCUtils.parseSqlResultList(rs, cls);
		} catch (Exception e) {
			throw e;
		}finally{
			JDBCUtils.close(rs,ps);
		}
	}
	
	@Override
	public int update(Connection conn, String sql,Class<?> returnClass, Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(conn, sql, params);
	}

	@Override
	public int insert(Connection conn, String sql,Class<?> returnClass, Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(conn, sql, params);
	}

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
	public <T> T insertRtnPKKey(Connection conn, String sql,Class<?> returnClass, Object[] params) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			JDBCUtils.setParams(ps, params);
			int rowNum = ps.executeUpdate();
			if(rowNum>0){
				rs = ps.getGeneratedKeys();
				List<Object> list = new ArrayList<Object>();
				ResultSetMetaData metaData  = rs.getMetaData();
				while(rs.next()){
					Object cols_value = JDBCUtils.getValueByObjectType(metaData, rs, 0);
					list.add(cols_value);
				}
				if(list.size()==0){
					return null;
				}
				if(list.size()==1){
					return (T) list.get(0);
				}else{
					return (T) list;
				}
			}
		} catch (Exception e) {
			throw e;
		}finally{
			JDBCUtils.close(rs,ps);
		}
		return null;
	}

	@Override
	public int delete(Connection conn, String sql, Class<?> returnClass,Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(conn, sql, params);
	}
	@Override
	public int excuteSQL(Connection conn, String sql, Class<?> returnClass,Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(conn, sql, params);
	}
}
