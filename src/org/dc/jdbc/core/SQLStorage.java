package org.dc.jdbc.core;

import java.util.HashMap;
import java.util.Map;

/**
 * sql存储仓库
 * @author dc
 * @time 2015-8-17
 */
public class SQLStorage {
	/**
	 * 保存了xml中定义的ID和对应的sql字符串。
	 */
    public static final Map<String,String> sqlSourceMap = new HashMap<String, String>();
    
    public static void put(String key,String sql){
    	sqlSourceMap.put(key, sql);
    }
    public static String getSql(String key){
        return sqlSourceMap.get(key);
    }
}
