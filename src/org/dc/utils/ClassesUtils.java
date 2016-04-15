package org.dc.utils;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

/**
 * class，类型转换器
 * @author 北京-企鹅
 * @time 2016-01-17 01:05
 */
public final class ClassesUtils {
		/**
	 * 数据类型转换，若数据为空则返回null
	 * @param classType
	 * @param value
	 * @return
	 * @throws Exception 
	 */
	public static Object convert(Class<?> classType,Object value) throws Exception{
		if(value==null){
			return null;
		}
		if(String.class.isAssignableFrom(classType)){
			return value.toString();
		}
		if(Byte.TYPE.isAssignableFrom(classType)){
			return Byte.parseByte(value.toString());
		}
		if(Byte.class.isAssignableFrom(classType)){
			return Byte.valueOf(value.toString());
		}
		if(Character.class.isAssignableFrom(classType)){
			if(value.toString().length()>1){
				throw new ClassCastException("字符串太长，不能强转成Character");
			}else{
				return (Character)value.toString().charAt(0);
			}
		}
		if(Character.TYPE.isAssignableFrom(classType)){
			if(value.toString().length()>1){
				throw new ClassCastException("字符串太长，不能强转成char");
			}else{
				return value.toString().charAt(0);
			}
		}
		if(Integer.TYPE.isAssignableFrom(classType)){
			return Integer.parseInt(value.toString());
		}
		if(Integer.class.isAssignableFrom(classType)){
			return Integer.valueOf(value.toString());
		}
		if(Short.class.isAssignableFrom(classType)){
			return Short.valueOf(value.toString());
		}
		if(Short.TYPE.isAssignableFrom(classType)){
			return Short.parseShort(value.toString());
		}
		if(Float.TYPE.isAssignableFrom(classType)){
			return Float.parseFloat(value.toString());
		}
		if(Float.class.isAssignableFrom(classType)){
			return Float.valueOf(value.toString());
		}
		if(Long.class.isAssignableFrom(classType)){
			return Long.valueOf(value.toString());
		}
		if(Long.TYPE.isAssignableFrom(classType)){
			return Long.parseLong(value.toString());
		}
		if(Double.TYPE.isAssignableFrom(classType)){
			return Double.parseDouble(value.toString());
		}
		if(Double.class.isAssignableFrom(classType)){
			return Double.valueOf(value.toString());
		}
		if(Boolean.class.isAssignableFrom(classType)){
			return Boolean.valueOf(value.toString());
		}
		if(Boolean.TYPE.isAssignableFrom(classType)){
			return Boolean.parseBoolean(value.toString());
		}
		return null;
	}
	/**
	 * 
	 * @param classType
	 * @param values 参数值
	 * @return
	 * @throws Exception
	 */
	public static Object convert(Class<?> classType,String[] values) throws Exception{
		if(values==null){
			return null;
		}
		if(classType.isArray()){
			classType = classType.getComponentType();
			int len = values.length;

			if(String.class.isAssignableFrom(classType)){
				String[] returnObj = new String[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] = values[i];
				}
				return returnObj;
			}
			if(Byte.TYPE.isAssignableFrom(classType)){
				byte[] returnObj = new byte[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] = Byte.parseByte(values[i]);
				}
				return returnObj;
			}
			if(Byte.class.isAssignableFrom(classType)){
				Byte[] returnObj = new Byte[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] = Byte.valueOf(values[i]);
				}
				return returnObj;
			}
			if(Character.class.isAssignableFrom(classType)){
				Character[] returnObj = new  Character[len];
				for (int i = 0; i < values.length; i++) {
					if(values[i].length()>1){
						throw new Exception("字符串太长，不能强转成Character");
					}else{
						returnObj[i] = (Character)values[i].charAt(0);
					}
				}
				return returnObj;
			}
			if(Character.TYPE.isAssignableFrom(classType)){
				char[] returnObj = new char[len];
				for (int i = 0; i < values.length; i++) {
					if(values[i].length()>1){
						throw new Exception("字符串太长，不能强转成char");
					}else{
						returnObj[i] = values[i].charAt(0);
					}
				}
				return returnObj;
			}
			if(Integer.TYPE.isAssignableFrom(classType)){
				int[] returnObj = new int[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] = Integer.parseInt(values[i]);
				}
				return returnObj;
			}
			if(Integer.class.isAssignableFrom(classType)){
				Integer[] returnObj = new Integer[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] = Integer.valueOf(values[i]);
				}
				return returnObj;
			}
			if(Short.class.isAssignableFrom(classType)){
				Short[] returnObj = new Short[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Short.valueOf(values[i]);
				}
				return returnObj;
			}
			if(Short.TYPE.isAssignableFrom(classType)){
				short[] returnObj = new short[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Short.parseShort(values[i]);
				}
				return returnObj;
			}
			if(Float.TYPE.isAssignableFrom(classType)){
				float[] returnObj = new float[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Float.parseFloat(values[i]);
				}
				return returnObj;
			}
			if(Float.class.isAssignableFrom(classType)){
				Float[] returnObj = new Float[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Float.valueOf(values[i]);
				}
				return returnObj;
			}
			if(Long.class.isAssignableFrom(classType)){
				Long[] returnObj = new Long[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Long.valueOf(values[i]);
				}
				return returnObj;
			}
			if(Long.TYPE.isAssignableFrom(classType)){
				long[] returnObj = new long[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Long.parseLong(values[i]);
				}
				return returnObj;
			}
			if(Double.TYPE.isAssignableFrom(classType)){
				double[] returnObj = new double[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Double.parseDouble(values[i]);
				}
				return returnObj;
			}
			if(Double.class.isAssignableFrom(classType)){
				Double[] returnObj = new Double[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Double.valueOf(values[i]);
				}
				return returnObj;
			}
			if(Boolean.class.isAssignableFrom(classType)){
				Boolean[] returnObj = new Boolean[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Boolean.valueOf(values[i]);
				}
				return returnObj;
			}
			if(Boolean.TYPE.isAssignableFrom(classType)){
				boolean[] returnObj = new boolean[len];
				for (int i = 0; i < values.length; i++) {
					returnObj[i] =Boolean.parseBoolean(values[i]);
				}
				return returnObj;
			}
			return null;
		}else{
			if(values.length>1){
				throw new Exception("请求参数与controller参数映射失败");
			}else{
				return ClassesUtils.convert(classType, (Object)values[0]);
			}
		}
	}
	/** 
	 * 获取方法参数名称，匹配同名的某一个方法 
	 *  
	 * @param clazz 
	 * @param method 
	 * @return 
	 * @throws NotFoundException 
	 *             如果类或者方法不存在 
	 * @throws MissingLVException 
	 *             如果最终编译的class文件不包含局部变量表信息 
	 */  
	public static String[] getMethodParamNames(String className, String method) throws Exception {  
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(new ClassClassPath(Class.forName(className))); //在servlet容器中启动
		CtClass cc = pool.get(className);
		CtMethod cm = cc.getDeclaredMethod(method);  
		return getMethodParamNames(cm);  
	}  
	/** 
	 * 获取方法参数名称 
	 *  
	 * @param cm 
	 * @return 
	 * @throws NotFoundException 
	 * @throws MissingLVException 
	 *             如果最终编译的class文件不包含局部变量表信息 
	 */  
	protected static String[] getMethodParamNames(CtMethod cm) throws Exception {  
		CtClass cc = cm.getDeclaringClass();  
		MethodInfo methodInfo = cm.getMethodInfo();  
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();  
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);  
		if (attr == null)  
			throw new Exception(cc.getName());  

		String[] paramNames = new String[cm.getParameterTypes().length];  
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;  
		for (int i = 0; i < paramNames.length; i++)  
			paramNames[i] = attr.variableName(i + pos);  
		return paramNames;  
	}  
}