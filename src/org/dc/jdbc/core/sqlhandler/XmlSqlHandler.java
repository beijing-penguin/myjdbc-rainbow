package org.dc.jdbc.core.sqlhandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.dc.jdbc.core.SQLStorage;
import org.dc.jdbc.entity.SqlEntity;

public class XmlSqlHandler extends SQLHandler{
	private static final XmlSqlHandler oper = new XmlSqlHandler();
	public static XmlSqlHandler getInstance(){
		return oper;
	}
	/**
	 * 处理方法，调用此方法处理请求
	 */
	@Override
	public SqlEntity handleRequest(String sqlOrID,Object[] params) throws Exception{

		/*		
		if(super.getSuccessor() != null){            
			super.getSuccessor().handleRequest(sqlOrID,params);
		}else{     */       
		StringBuilder sql =new StringBuilder(sqlOrID.startsWith("$")?SQLStorage.getSql(sqlOrID):sqlOrID);
		SqlEntity sqlEntity = new SqlEntity();
		if(params!=null && params.length>0){
			List<Object> sqlparam = new ArrayList<Object>();
			for (Object param : params) {
				if(param!=null){
					if(Map.class.isAssignableFrom(param.getClass())){
						super.parseMapSql(param, sql, sqlparam);
					}else if(Collection.class.isAssignableFrom(param.getClass())){
						sqlparam.addAll((Collection<?>) param);
					}else if(Object[].class.isAssignableFrom(param.getClass())){
						Object[] ps = (Object[])param;
						Collections.addAll(sqlparam, ps);
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
		return sqlEntity;
	}
}
