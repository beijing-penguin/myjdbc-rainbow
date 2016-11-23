package org.dc.jdbc.core.operate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.dc.jdbc.core.SqlContext;
import org.dc.jdbc.core.utils.JDBCUtils;

public class DataBaseDaoImp implements IDataBaseDao{
	private DataBaseDaoImp(){}
	
	private static final DataBaseDaoImp INSTANCE = new DataBaseDaoImp();
	public static DataBaseDaoImp getInstance(){
		return INSTANCE;
	}
	@Override
	public  <T> T selectOne(String sql,Class<? extends T> cls,Object[] params) throws Exception{
		List<T> list = this.selectList(sql, cls, params);
		if(list == null){
			return null;
		}
		if(list.size()>1){
			throw new Exception("Query results too much!");
		}
		return list.get(0);
	}
	@Override
	public <T> List<T> selectList(String sql, Class<? extends T> cls, Object[] params) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = SqlContext.getContext().getCurrentConnection().prepareStatement(sql);
			rs = JDBCUtils.preparedSQLReturnRS(ps, sql, params);

			return JDBCUtils.parseSqlResultList(rs, cls);
		} catch (Exception e) {
			throw e;
		}finally{
			JDBCUtils.close(rs,ps);
		}
	}
	
	@Override
	public int update(String sql,Class<?> returnClass, Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(SqlContext.getContext().getCurrentConnection(), sql, params);
	}

	@Override
	public int insert(String sql,Class<?> returnClass, Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(SqlContext.getContext().getCurrentConnection(), sql, params);
	}

	public int[] insertBatch(String sql, Object[][] params) throws Exception {
		PreparedStatement ps = null;
		try {
			ps = SqlContext.getContext().getCurrentConnection().prepareStatement(sql);
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
	public <T> T insertRtnPKKey(String sql,Class<?> returnClass, Object[] params) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = SqlContext.getContext().getCurrentConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
	public int delete(String sql, Class<?> returnClass,Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(SqlContext.getContext().getCurrentConnection(), sql, params);
	}
	@Override
	public int excuteSQL(String sql, Class<?> returnClass,Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(SqlContext.getContext().getCurrentConnection(), sql, params);
	}
	@Override
	public int updateEntity(Object entity) throws Exception {
		SqlContext sqlContext = SqlContext.getContext();
		return JDBCUtils.preparedAndExcuteSQL(sqlContext.getCurrentConnection(), sqlContext.getSql(), sqlContext.getParams());
	}
	@Override
	public int insertEntity(Object entity) throws Exception {
		SqlContext sqlContext = SqlContext.getContext();
		return JDBCUtils.preparedAndExcuteSQL(sqlContext.getCurrentConnection(), sqlContext.getSql(), sqlContext.getParams());
	}
}
