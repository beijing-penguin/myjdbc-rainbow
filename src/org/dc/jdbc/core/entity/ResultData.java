package org.dc.jdbc.core.entity;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.SqlContext;

public class ResultData {
	private Object data;

	
	/**
	 * sql执行完了之后    执行的事件
	 */
	public ResultData afterBindEvent(){
		if (SqlContext.getContext().getReadOnly()) {
			ConnectionManager.closeConnectionAll();
		}
		return this;
	}
	
	public ResultData(Object data){
		this.data = data;
	}
	@SuppressWarnings("unchecked")
	public <T> T getData() {
		if(data==null){
			return null;
		}
		return (T) data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
