package org.dc.jdbc.core.sqlhandler;

import org.dc.jdbc.entity.SqlEntity;

public abstract class SQLHandler{
    /**
     * 持有后继的责任对象
     */
    protected SQLHandler successor;
    
    public abstract SqlEntity handleRequest(String sqlOrID,Object[] params) throws Exception;
    /**
     * 获取后继的责任对象
     */
    public SQLHandler getSuccessor() {
        return successor;
    }
    /**
     * 赋值方法，设置后继的责任对象
     */
    public SQLHandler setSuccessor(SQLHandler successor) {
        this.successor = successor;
        return this;
    }
    
}