package org.dc.jdbc.core;

import java.util.HashMap;
import java.util.Map;

/**
 * sql存储仓库
 * @author dc
 * @time 2015-8-17
 */
public class SQLStorage {
    public static final Map<String,String> sqlMap = new HashMap<String, String>();
    public static void put(String key,String sql){
        sqlMap.put(key, sql);
    }
    public static String getSql(String key){
        return sqlMap.get(key);
    }
}
