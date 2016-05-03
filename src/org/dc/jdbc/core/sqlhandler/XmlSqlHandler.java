package org.dc.jdbc.core.sqlhandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
		StringBuilder sqlSource = new StringBuilder(sqlOrID.startsWith("$")?SQLStorage.getSql(sqlOrID):sqlOrID);
		List<Object> sqlparam = null;
		if(params!=null && params.length>0){
			if(sqlSource.indexOf("?") != -1){//问号匹配符
				sqlparam = new ArrayList<Object>();
				for (Object param : params) {
					if(Object[].class.isAssignableFrom(param.getClass())){
						Collections.addAll(sqlparam, (Object[])param);
					}else if(Collection.class.isAssignableFrom(param.getClass())){
						sqlparam.addAll((Collection<?>) param);
					}else if(param.getClass().getClassLoader()==null){//java基本数据类型
						sqlparam.add(param);
					}
				}
			}else if(sqlSource.indexOf("#") != -1){
				sqlparam = new ArrayList<Object>();
				Map<Object,Object> allMap = new HashMap<Object,Object>();
				for (Object param : params) {
					if(Map.class.isAssignableFrom(param.getClass())){
						Map<?,?> paramMap = (Map<?, ?>) param;
						allMap.putAll(paramMap);
					}else { // java对象
						Field[] fields = param.getClass().getDeclaredFields();
						for (Field field : fields) {
							field.setAccessible(true);
							Object value = field.get(param);
							allMap.put(field.getName(), value);
						}
					}
				}
				int quote_Seq_index = 0;
				while((quote_Seq_index = sqlSource.indexOf("#",quote_Seq_index))!=-1){
					int indexEnd = sqlSource.indexOf("}",quote_Seq_index);
					String key = sqlSource.substring(quote_Seq_index+2,indexEnd);
					if(allMap.containsKey(key)){
						sqlparam.add(allMap.get(key));
					}else{
						throw new Exception("parameters do not match to!");
					}
					sqlSource.replace(quote_Seq_index,indexEnd+1, "?");
				}
			}else{
				throw new Exception("parameters do not match to!");
			}
		}
		SqlEntity sqlEntity = new SqlEntity();

		sqlEntity.setSql(sqlSource.toString());
		if(sqlparam!=null){
			sqlEntity.setParams(sqlparam.toArray());
		}
		return sqlEntity;
	}
}
