package org.dc.jdbc.core.base;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * SQL处理父类
 * @author DC
 * @time 2016-04-14
 */
public abstract class SQLHandleSuper {
	private static Log log = LogFactory.getLog(SQLHandleSuper.class);
	public void parseMapSql(Object param,StringBuilder sql,List<Object> sqlparam){
		Map<?,?> p = (Map<?,?>)param;
		for (int i = 0,len=sql.length(); i <len; i++) {
			if(sql.charAt(i)=='#'){
				int indexEnd = sql.indexOf("}");
				String key = sql.substring(i+2,indexEnd);
				sql = sql.replace(i,indexEnd+1, "?");
				len= sql.length();
				Object value = p.get(key);
				sqlparam.add(value);
			}
		}
	}
	public void parseObjectSql(Object param,StringBuilder sql,List<Object> sqlparam) throws Exception{
		Field[] fields = param.getClass().getDeclaredFields();
		for (int i = 0,len=sql.length(); i <len; i++) {
			if(sql.charAt(i)=='#'){
				int indexEnd = sql.indexOf("}");
				String key = sql.substring(i+2,indexEnd);
				for (int j = 0; j < fields.length; j++) {
					Field field =  fields[j];
					if(field.getName().equals(key)){
						fields[j].setAccessible(true);
						Object value = fields[j].get(param);
						sqlparam.add(value);
						break;
					}
				}
				sql = sql.replace(i,indexEnd+1, "?");
				len= sql.length();
			}
		}
	}
	public void printSqlLog(String sql,Object[] params){
		StringBuilder sbsql = new StringBuilder(sql);
		if(params!=null && params.length>0){
			int index = 0;
			int i = 0;
			while((i = sbsql.indexOf("?"))!=-1){
				Object value = params[index];
				if(value.getClass().isAssignableFrom(String.class)){
					sbsql.replace(i, i+1, "\""+params[index]+"\"");
				}else{
					sbsql.replace(i, i+1, params[index].toString());
				}
				index++;
			}
		}
		log.info(sbsql.toString());
	}
}
