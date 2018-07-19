package org.dc.jdbc.core.utils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ObjectUtils {
	public static Object getValueByFieldType(Object value, Class<?> fieldType) throws Exception{
		if(value==null){
			return null;
		}
		String v = String.valueOf(value);
		String type = fieldType.getSimpleName();
		if(type.equals("String")){
			return v;
		}else if (type.equals("Integer") || type.equals("int")){
			return Integer.parseInt(v);
		}else if (type.equals("Long") || type.equals("long")){
			return Long.parseLong(v);
		}else if (type.equals("Double") || type.equals("double")){
			return Long.parseLong(v);
		}else if (type.equals("Short") || type.equals("short")){
			return Short.parseShort(v);
		}else if (type.equals("Byte") || type.equals("byte")){
			return Byte.parseByte(v);
		}else if (type.equals("Boolean") || type.equals("boolean")){
			return Boolean.parseBoolean(v);
		}else if (type.equals("Bigdecimal") ){
			return new BigDecimal(v);
		}else if (type.equals("BigInteger") ){
			return new BigInteger(v);
		}
		throw new Exception(type + " is not suport");
	}
}
