package org.dc.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dc.jdbc.core.pojo.ResultData;
import org.dc.jdbc.core.utils.JDBCUtils;
import org.dc.jdbc.exceptions.TooManyResultsException;
/**
 * 
 * @author dc
 * @date: 2015年8月17日
 */
public class DbHelper {
	
	
    public static long selectCount(Connection conn ,String sql, Object... params) throws Exception {
        return selectOne(conn,"SELECT COUNT(*) FROM (" + sql + ") t", Long.class, params);
    }
    public static ResultData selectResult(Connection conn, String sql, Class<?> returnClass, Object[] params) throws Exception {
    	PreparedStatement ps = conn.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        if(conn.toString().equalsIgnoreCase("mysql")){
            ps.setFetchSize(Integer.MIN_VALUE);
        }
        ResultSet rs = JDBCUtils.setParamsReturnRS(ps, params);
        return  new ResultData(returnClass,rs,ps);
    }

    @SuppressWarnings("unchecked")
	public static <T> T selectOne(Connection conn,String sql, Class<? extends T> returnClass, Object[] params) throws Exception {
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
                rt = JDBCUtils.getBeanObjectByClassType(rs, returnClass);
            }
            if(rt==null) {
            	return null;
            }
            return  (T) rt;
        } catch (Exception e) {
            throw e;
        } finally {
            JDBCUtils.close(rs, ps);
        }
    }

    public static Map<String, Object> selectOne(Connection conn,String sqlOrID, Object[] params) throws Exception {
        return selectOne(conn,sqlOrID, null, params);
    }
    public static <T> List<T> selectList(Connection conn,String sql, Class<? extends T> returnClass, Object[] params) throws Exception {
    	ResultSet rs = null;
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = JDBCUtils.setParamsReturnRS(ps, params);
            return JDBCUtils.parseSqlResultList(rs, returnClass);
        } catch (Exception e) {
            throw e;
        } finally {
            JDBCUtils.close(rs, ps);
        }
    }

    public static List<Map<String, Object>> selectList(Connection conn,String sqlOrID, Object[] params) throws Exception {
        return selectList(conn,sqlOrID, null, params);
    }

    public static int excuteSql(Connection conn,String sql, Object[] params) throws Exception {
    	return JDBCUtils.preparedAndExcuteSQL(conn, sql, params);
    }

    @SuppressWarnings("unchecked")
	public static <T> T excuteSqlReturnPK(Connection conn,String sql, Object[] params) throws Exception {
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
            while (rs.next()) {
                return (T) JDBCUtils.getValueByObjectType(metaData, rs, 0);
            }
            
        } catch (Exception e) {
            throw e;
        } finally {
            JDBCUtils.close(rs, ps);
        }
        return null;
    }


    public List<Integer> insertBatch(Connection conn,String sql, Object[] params) throws Exception {
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
            return rtnList;
        } catch (Exception e) {
            throw e;
        } finally {
            JDBCUtils.close(ps);
        }
    }
}