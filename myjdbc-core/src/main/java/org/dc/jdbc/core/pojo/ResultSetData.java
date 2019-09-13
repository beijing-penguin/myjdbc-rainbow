package org.dc.jdbc.core.pojo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.SqlContext;
import org.dc.jdbc.core.utils.JDBCUtils;

public class ResultSetData {

    private Object data;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;
    private Class<?> classType;

    /**
     * sql执行完了之后    执行的事件
     * @throws Exception 
     */
    public ResultSetData afterBindEvent() throws Throwable{
        if (SqlContext.getContext().getReadOnly()) {
            //ConnectionManager.commitAll();
            ConnectionManager.closeConnectionAll();
        }
        return this;
    }

    public ResultSetData(Object data){
        this.data = data;
    }
    public ResultSetData(Class<?> classType,ResultSet resultSet,PreparedStatement preparedStatement){
        this.classType = classType;
        this.preparedStatement = preparedStatement;
        this.resultSet = resultSet;
    }
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        if(data==null){
            return null;
        }
        return (T) data;
    }

    public ResultSetData next() throws Exception {
        try{
            while(resultSet.next()){
                this.data = JDBCUtils.getBeanObjectByClassType(resultSet, classType);
                return this;
            }
        }catch (Exception e) {
            JDBCUtils.close(resultSet,preparedStatement);
            throw e;
        }
        return null;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public Class<?> getClassType() {
        return classType;
    }

    public void setClassType(Class<?> classType) {
        this.classType = classType;
    }
}
