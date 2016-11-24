package org.dc.jdbc.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.dc.jdbc.core.entity.ColumnBean;
import org.dc.jdbc.core.entity.TableInfoBean;

public class CacheCenter {
	public static final Map<String,String> SQL_SOURCE_MAP = new HashMap<String, String>();
	public static final Map<DataSource,List<TableInfoBean>> DATABASE_INFO_CACHE = new ConcurrentHashMap<DataSource, List<TableInfoBean>>();
	public static final Map<Class<?>,TableInfoBean> SQL_TABLE_CACHE = new ConcurrentHashMap<Class<?>,TableInfoBean>();
	public static final Map<Class<?>,List<Field>> CLASS_SQL_FIELD_CACHE = new ConcurrentHashMap<Class<?>,List<Field>>();
	public static final Map<Class<?>,List<String>> CLASS_SQL_COLNAME_CACHE = new ConcurrentHashMap<Class<?>,List<String>>();
	
	public static final Map<Class<?>,String> INSERT_SQL_CACHE = new ConcurrentHashMap<Class<?>,String>();
}
