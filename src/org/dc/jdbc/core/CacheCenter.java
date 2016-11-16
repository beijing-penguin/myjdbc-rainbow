package org.dc.jdbc.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.dc.jdbc.core.entity.TableInfo;

public class CacheCenter {
	public static final Map<String,String> sqlSourceMap = new HashMap<String, String>();
	public static final Map<DataSource,List<TableInfo>> databaseInfoCache = new HashMap<DataSource, List<TableInfo>>();
	public static final Map<Class<?>,String> insertSqlCache = new HashMap<Class<?>,String>();
}
