package org.dc.jdbc.core;

import java.util.HashMap;
import java.util.Map;

public class GlobalCache {
	private static Map<Class<?>,Map<String,String>> fieldsCache = new HashMap<Class<?>, Map<String,String>>();
}
