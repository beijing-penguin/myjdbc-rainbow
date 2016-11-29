package com.yanxiu.jdbc.core.entity;

import java.util.ArrayList;
import java.util.List;

public class TableInfoBean {
	private String tableName;
	private List<ColumnBean> columnList=new ArrayList<ColumnBean>();
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public List<ColumnBean> getColumnList() {
		return columnList;
	}
	public void setColumnList(List<ColumnBean> columnList) {
		this.columnList = columnList;
	}
}
