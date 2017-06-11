package org.dc.jdbc.core.pojo;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface FieldValue {
	String value();
	DBType dbType() default DBType.UNKNOW;
	SqlType sqlType() default SqlType.UNKNOW;
}
