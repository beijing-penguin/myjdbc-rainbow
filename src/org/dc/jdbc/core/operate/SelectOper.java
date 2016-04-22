package org.dc.jdbc.core.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dc.jdbc.core.base.OperSuper;
/**
 * 查询数据的操作
 * @author dc
 * @updateTime 2015-8-17
 */
public class SelectOper extends OperSuper{
	private static final SelectOper oper = new SelectOper();
	public static SelectOper getInstance(){
		return oper;
	}
	@SuppressWarnings("unchecked")
	public  <T> T selectOne(Connection conn,String sql,Class<?> cls,Object[] params) throws Exception{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = super.preparedSQLReturnRS(ps, sql, params);

			int row = 0;
			if(rs.last() && (row = rs.getRow())>1){
				throw new Exception("查询结果太多");
			}
			if(row==1){//判断是否有返回结果
				if(cls==null || Map.class.isAssignableFrom(cls)){
					return (T) super.parseSqlResultToMap(rs);//返回结果不可能为空，所以不需要判断空异常 
				}else{
					if(cls.getClassLoader()==null){//java基本类型
						return (T) super.parseSqlResultToBaseType(rs);
					}else{//java对象
						return (T) super.parseSqlResultToObject(rs, cls);
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}finally{
			super.close(ps,rs);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> selectList(Connection conn,String sql,Class<?> cls,Object[] params) throws Exception{
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = super.preparedSQLReturnRS(ps, sql, params);
			rs.last();
			int rowNum = rs.getRow();
			if(rowNum>0){
				rs.beforeFirst();
				List<Object> list = new ArrayList<Object>(rowNum);

				if(cls==null || Map.class.isAssignableFrom(cls)){//封装成Map
					super.parseSqlResultToMap(rs,list);
				}else{
					if(cls.getClassLoader()==null){//封装成基本类型
						super.parseSqlResultToBaseType(rs,list);
					}else{//对象
						super.parseSqlResultToObject(rs,cls,list);
					}
				}
				return (List<T>) list;
			}
		} catch (Exception e) {
			throw e;
		}finally{
			super.close(ps,rs);
		}
		return null;
	}
}
