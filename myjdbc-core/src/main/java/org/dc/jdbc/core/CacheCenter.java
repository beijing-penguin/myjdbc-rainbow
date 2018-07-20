package org.dc.jdbc.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.dc.jdbc.core.pojo.ClassRelation;
import org.dc.jdbc.core.pojo.TableInfoBean;

public class CacheCenter {
    public static final Map<String, String> SQL_SOURCE_MAP = new HashMap<String, String>();
    public static final Map<DataSource, List<TableInfoBean>> DATABASE_INFO_CACHE = new ConcurrentHashMap<DataSource, List<TableInfoBean>>();
    public static final Map<Class<?>, TableInfoBean> SQL_TABLE_CACHE = new ConcurrentHashMap<Class<?>, TableInfoBean>();
    public static final Map<Class<?>, List<ClassRelation>> CLASS_REL_FIELD_CACHE = new ConcurrentHashMap<Class<?>, List<ClassRelation>>();
}
