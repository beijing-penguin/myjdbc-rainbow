package org.dc.jdbc.core.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.dc.jdbc.core.entity.DataBaseType;
import org.dc.jdbc.core.entity.ResultData;
import org.dc.jdbc.core.utils.JDBCUtils;
import org.dc.jdbc.exceptions.TooManyResultsException;

public class DataBaseOperate{
	private DataBaseOperate() {
	}

	private static final DataBaseOperate INSTANCE = new DataBaseOperate();

	public static DataBaseOperate getInstance() {
		return INSTANCE;
	}
	
	public ResultData selectResult(Connection conn,String sql, Class<?> cls, Object[] params,DataBaseType dataBaseType) throws Exception {
		PreparedStatement ps = conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		if(dataBaseType!=null){
			if(dataBaseType==DataBaseType.MYSQL){
				ps.setFetchSize(Integer.MIN_VALUE);
			}
		}
		ResultSet rs = JDBCUtils.setParamsReturnRS(ps, params);
		return  new ResultData(cls,rs,ps);
	}

	public ResultData selectOne(Connection conn,String sql, Class<?> cls, Object[] params) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		Object rt = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = JDBCUtils.setParamsReturnRS(ps, params);
			int row_num = 0;
			while (rs.next()) {
				row_num++;
				if(row_num>1){
					throw new TooManyResultsException();
				}
				rt = JDBCUtils.getBeanObjectByClassType(rs, cls);
			}
			return  new ResultData(rt);
		} catch (Exception e) {
			throw e;
		} finally {
			JDBCUtils.close(rs, ps);
		}
	}

	public ResultData selectList(Connection conn,String sql, Class<?> cls, Object[] params) throws Exception {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = JDBCUtils.setParamsReturnRS(ps, params);
			return new ResultData(JDBCUtils.parseSqlResultList(rs, cls));
		} catch (Exception e) {
			throw e;
		} finally {
			JDBCUtils.close(rs, ps);
		}
	}

	public ResultData insertReturnPK(Connection conn,String sql, Class<?> returnClass, Object[] params) throws Exception {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			JDBCUtils.setParams(ps, params);
			int rowNum = ps.executeUpdate();
			if (rowNum > 1) {
				throw new Exception("the insert too many");
			}
			rs = ps.getGeneratedKeys();
			ResultSetMetaData metaData = rs.getMetaData();
			Object rt = null;
			while (rs.next()) {
				rt =  JDBCUtils.getValueByObjectType(metaData, rs, 0);
			}
			return new ResultData(rt);
		} catch (Exception e) {
			throw e;
		} finally {
			JDBCUtils.close(rs, ps);
		}
	}


	public ResultData excuteSQL(Connection conn,String sql,  Object[] params) throws Exception {
		return new ResultData(JDBCUtils.preparedAndExcuteSQL(conn, sql, params));
	}


	public ResultData insertBatch(Connection conn,String sql, Class<?> returnClass, Object[] params) throws Exception {
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			int count = 0;
			List<Integer> rtnList = new ArrayList<>(params.length);
			for (Object param : params) {
				Object[] setParamsArr = (Object[]) param;
				for (int i = 0; i < setParamsArr.length; i++) {
					ps.setObject(i + 1, setParamsArr[i]);
				}
				ps.addBatch();
				if (++count % 1000 == 0) {// 分批提交，防止内存占用时间太长导致OutOfMemoryError
					count = 0;
					int[] batchArr = ps.executeBatch();
					for (int i = 0; i < batchArr.length; i++) {
						rtnList.add(batchArr[i]);
					}
				}
			}
			int[] batchArr = ps.executeBatch(); // insert remaining records

			for (int i = 0; i < batchArr.length; i++) {
				rtnList.add(batchArr[i]);
			}
			return new ResultData(rtnList);
		} catch (Exception e) {
			throw e;
		} finally {
			JDBCUtils.close(ps);
		}
	}
}
