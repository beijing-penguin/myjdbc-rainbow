package org.dc.jdbc.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.dc.jdbc.config.JDBCConfig;
import org.dc.jdbc.core.base.SQLHandleSuper;
import org.dc.jdbc.entity.SqlEntity;
/**
 * SQL处理类
 * @author dc
 * @time 2015-8-17
 */
public class SQLHandler extends SQLHandleSuper{
	 private static SQLHandler sqlHandler = new SQLHandler();
	    public static SQLHandler getInstance(){
	        return sqlHandler;
	    }
	/**
	 * SQL语句参数匹配预处理，作用将sql中的占位符转化成标准的jdbc中的"?"占位符，并将对应参数转化成数组，
	 * 日后可能会基于这个方法做过滤器编程
	 * @param sqlOrID
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public SqlEntity sqlHandler(String sqlOrID,Object[] params) throws Exception{
		StringBuilder sql =new StringBuilder(sqlOrID.startsWith("$")?SQLStorage.getSql(sqlOrID):sqlOrID);
		SqlEntity sqlEntity = new SqlEntity();
		if(params!=null && params.length>0){
			List<Object> sqlparam = new ArrayList<Object>();
			for (int k = 0; k < params.length; k++) {
				Object param = params[k];
				if(param!=null){
					if(Map.class.isAssignableFrom(param.getClass())){
						super.parseMapSql(param, sql, sqlparam);
					}else if(Collection.class.isAssignableFrom(param.getClass())){
						sqlparam.addAll((Collection<?>) param);
					}else if(Object[].class.isAssignableFrom(param.getClass())){
						Object[] ps = (Object[])param;
						for (int i = 0; i < ps.length; i++) {
							sqlparam.add(ps[i]);
						}
					}else if(param.getClass().getClassLoader()==null){//java基本数据类型
						sqlparam.add(param);
					}else{//java对象类型
						super.parseObjectSql(param, sql, sqlparam);
					}
				}
			}
			sqlEntity.setParams(sqlparam.toArray());
		}
		sqlEntity.setSql(sql.toString());

		if(JDBCConfig.isPrintSqlLog){
			super.printSqlLog(sqlEntity.getSql(), sqlEntity.getParams());
		}
		return sqlEntity;
	}
}
