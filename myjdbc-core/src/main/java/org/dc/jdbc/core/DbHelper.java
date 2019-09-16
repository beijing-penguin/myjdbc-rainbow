package org.dc.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.dc.jdbc.core.pojo.ResultSetData;
import org.dc.jdbc.core.sqlhandler.SqlCoreHandle;
import org.dc.jdbc.core.utils.JDBCUtils;
import org.dc.jdbc.exceptions.TooManyResultsException;
/**
 * 
 * @author dc
 * @date: 2015年8月17日
 */
public class DbHelper {
	
	private DataSource dataSource;
	public DbHelper(DataSource dataSource) {
		this.dataSource = dataSource;
	}
    public long selectCount(String sqlOrID, Object...params) throws Throwable {
    	String dosql = JDBCUtils.getFinalSql(sqlOrID);
		return this.selectOne("SELECT COUNT(*) FROM (" + dosql + ") t", Long.class, params);
    }
    public ResultSetData selectResultSet(String sqlOrID, Class<?> returnClass, Object[] params) throws Throwable {
    	String doSql = JDBCUtils.getFinalSql(sqlOrID);
    	SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
    	Connection conn = ConnectionManager.getConnection(dataSource);
    	PreparedStatement ps = conn.prepareStatement(context.getSql(),ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        if(conn.toString().equalsIgnoreCase("mysql")){
            ps.setFetchSize(Integer.MIN_VALUE);
        }
        ResultSet rs = JDBCUtils.setParamsReturnRS(ps, context.getParamList().toArray());
        return  new ResultSetData(returnClass,rs,ps);
    }

    @SuppressWarnings("unchecked")
	public <T> T selectOne(String sqlOrID, Class<? extends T> returnClass, Object...params) throws Throwable {
    	String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
		
    	Connection conn = ConnectionManager.getConnection(dataSource);
    	ResultSet rs = null;
        PreparedStatement ps = null;
        Object rt = null;
        try {
            ps = conn.prepareStatement(context.getSql());
            rs = JDBCUtils.setParamsReturnRS(ps, context.getParamList().toArray());
            int row_num = 0;
            while (rs.next()) {
                row_num++;
                if(row_num>1){
                    throw new TooManyResultsException();
                }
                rt = JDBCUtils.getBeanObjectByClassType(rs, returnClass);
            }
            if(rt==null) {
            	return null;
            }
            return  (T) rt;
        } catch (Throwable e) {
            throw e;
        } finally {
            JDBCUtils.close(rs, ps);
        }
    }

    public Map<String, Object> selectOne(String sqlOrID, Object...params) throws Throwable {
        return selectOne(sqlOrID, null, params);
    }
    public <T> List<T> selectList(String sqlOrID, Class<? extends T> returnClass, Object[] params) throws Throwable {
    	String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
    	Connection conn = ConnectionManager.getConnection(dataSource);
    	
    	ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(context.getSql());
            rs = JDBCUtils.setParamsReturnRS(ps, context.getParamList().toArray());
            return JDBCUtils.parseSqlResultList(rs, returnClass);
        } catch (Throwable e) {
            throw e;
        } finally {
            JDBCUtils.close(rs, ps);
        }
    }

    public List<Map<String, Object>> selectList(String sqlOrID, Object...params) throws Throwable {
        return selectList(sqlOrID, null, params);
    }

    public int excuteSql(String sqlOrID, Object... params) throws Throwable {
    	String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
    	Connection conn = ConnectionManager.getConnection(dataSource);
    	return JDBCUtils.preparedAndExcuteSQL(conn, context.getSql(), params);
    }

    @SuppressWarnings("unchecked")
	public <T> T excuteSqlReturnPK(String sqlOrID, Object...params) throws Throwable {
    	String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
    	Connection conn = ConnectionManager.getConnection(dataSource);
    	
    	PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(context.getSql(),Statement.RETURN_GENERATED_KEYS);
            JDBCUtils.setParams(ps, context.getParamList().toArray());
            int rowNum = ps.executeUpdate();
            if (rowNum > 1) {
                throw new Throwable("the insert too many");
            }
            rs = ps.getGeneratedKeys();
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                return (T) JDBCUtils.getValueByObjectType(metaData, rs, 0);
            }
            
        } catch (Throwable e) {
            throw e;
        } finally {
            JDBCUtils.close(rs, ps);
        }
        return null;
    }


    public List<Integer> insertBatch(String sqlOrID, Object...params) throws Throwable {
    	String doSql = JDBCUtils.getFinalSql(sqlOrID);
		SqlContext context = SqlCoreHandle.handleRequest(doSql, params).printSqlLog();
    	Connection conn = ConnectionManager.getConnection(dataSource);
    	
    	PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(context.getSql());
            int count = 0;
            List<Integer> rtnList = new ArrayList<>(params.length);
            for (Object param : context.getParamList()) {
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
            return rtnList;
        } catch (Throwable e) {
            throw e;
        } finally {
            JDBCUtils.close(ps);
        }
    }
	public int insert(String sqlOrID, Object...params) throws Throwable {
		return excuteSql(sqlOrID, params);
	}
	public int insertEntity(Object entity) throws Throwable {
		SqlContext context = SqlCoreHandle.handleInsertRequest(entity);
    	Connection conn = ConnectionManager.getConnection(dataSource);
    	return JDBCUtils.preparedAndExcuteSQL(conn, context.getSql(), context.getParamList().toArray());
	}
}