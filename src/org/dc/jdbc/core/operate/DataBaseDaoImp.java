package org.dc.jdbc.core.operate;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;

import org.dc.jdbc.core.ConnectionManager;
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
			ps = ConnectionManager.getConnection(SqlContext.getContext().getCurrentDataSource()).prepareStatement(sql);
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
		return JDBCUtils.preparedAndExcuteSQL(ConnectionManager.getConnection(SqlContext.getContext().getCurrentDataSource()), sql, params);
	}

	@Override
	public int insert(String sql,Class<?> returnClass, Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(ConnectionManager.getConnection(SqlContext.getContext().getCurrentDataSource()), sql, params);
	}

	public int[] insertBatch(String sql, Object[][] params) throws Exception {
		PreparedStatement ps = null;
		try {
			ps = ConnectionManager.getConnection(SqlContext.getContext().getCurrentDataSource()).prepareStatement(sql);
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
			ps = ConnectionManager.getConnection(SqlContext.getContext().getCurrentDataSource()).prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			JDBCUtils.setParams(ps, params);
			int rowNum = ps.executeUpdate();
			if(rowNum>0){
				rs = ps.getGeneratedKeys();
				ResultSetMetaData metaData  = rs.getMetaData();
				int num = 0;
				T rt = null;
				while(rs.next()){
					if(num>0){
						throw new Exception("the insert too many");
					}
					num++;
					rt = (T) JDBCUtils.getValueByObjectType(metaData, rs, 0);
				}
				return rt;
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
		return JDBCUtils.preparedAndExcuteSQL(ConnectionManager.getConnection(SqlContext.getContext().getCurrentDataSource()), sql, params);
	}
	@Override
	public int deleteEntity(Object entity) throws Exception {
		SqlContext sqlContext = SqlContext.getContext();
		return JDBCUtils.preparedAndExcuteSQL(ConnectionManager.getConnection(
				sqlContext.getCurrentDataSource()), 
				sqlContext.getSql(), 
				sqlContext.getParamList().toArray());
	}
	@Override
	public int excuteSQL(String sql, Class<?> returnClass,Object[] params) throws Exception {
		return JDBCUtils.preparedAndExcuteSQL(ConnectionManager.getConnection(SqlContext.getContext().getCurrentDataSource()), sql, params);
	}
	@Override
	public int updateEntity(Object entity) throws Exception {
		SqlContext sqlContext = SqlContext.getContext();
		return JDBCUtils.preparedAndExcuteSQL(ConnectionManager.getConnection(sqlContext.getCurrentDataSource()), sqlContext.getSql(), sqlContext.getParamList().toArray());
	}
	@Override
	public int insertEntity(Object entity) throws Exception {
		SqlContext sqlContext = SqlContext.getContext();
		return JDBCUtils.preparedAndExcuteSQL(ConnectionManager.getConnection(sqlContext.getCurrentDataSource()), sqlContext.getSql(), sqlContext.getParamList().toArray());
	}
	@Override
	public <T> T insertEntityRtnPKKey(Object entity) throws Exception {
		SqlContext sqlContext = SqlContext.getContext();
		return this.insertRtnPKKey(sqlContext.getSql(), null, sqlContext.getParamList().toArray());
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> selectList(Object entity, String whereSql, Object... params) throws Exception {
		SqlContext sqlContext = SqlContext.getContext();
		return (List<T>) this.selectList(sqlContext.getSql(), entity.getClass(), sqlContext.getParamList().toArray());
	}
	@Override
	public BigInteger insertBatch(String sql, Object object, Object[] params) throws Exception {
		PreparedStatement ps = null;
		try{
			Connection connection = ConnectionManager.getConnection(SqlContext.getContext().getCurrentDataSource());
			ps = connection.prepareStatement(sql);
			int count = 0;
			BigInteger bigInteger = BigInteger.ZERO;
			for (Object param: params) {
				Object[] setParamsArr = (Object[]) param;
				for (int i = 0; i < setParamsArr.length; i++) {
					ps.setObject(i+1, setParamsArr[i]);
				}
				ps.addBatch();
				if(++count%1000 == 0) {//分批提交，防止内存占用时间太长导致OutOfMemoryError
					count = 0;
					int[] batchArr = ps.executeBatch();
					for (int i = 0; i < batchArr.length; i++) {
						bigInteger = bigInteger.add(new BigInteger(String.valueOf(batchArr[i])));
					}
				}
			}
			int[] batchArr = ps.executeBatch(); // insert remaining records
			
			for (int i = 0; i < batchArr.length; i++) {
				bigInteger = bigInteger.add(new BigInteger(String.valueOf(batchArr[i])));
			}
			return bigInteger;
		}catch (Exception e) {
			throw e;
		}finally {
			JDBCUtils.close(ps);
		}
	}
}
