package com.yanxiu.jdbc.core.entity;

import java.lang.reflect.Field;

public class ClassRelation {
	private Field field;
	private ColumnBean columnBean;
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public ColumnBean getColumnBean() {
		return columnBean;
	}
	public void setColumnBean(ColumnBean columnBean) {
		this.columnBean = columnBean;
	}
	
	
}
