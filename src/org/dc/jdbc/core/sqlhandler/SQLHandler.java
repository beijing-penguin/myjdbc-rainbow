package org.dc.jdbc.core.sqlhandler;

import org.dc.jdbc.core.base.SQLHandleSuper;
import org.dc.jdbc.entity.SqlEntity;

public abstract class SQLHandler extends SQLHandleSuper{
    /**
     * 持有后继的责任对象
     */
    protected SQLHandler successor;
    /**
     * 示意处理请求的方法，虽然这个示意方法是没有传入参数的
     * 但实际是可以传入参数的，根据具体需要来选择是否传递参数
     */
    public abstract SqlEntity handleRequest(String sqlOrID,Object[] params) throws Exception;
    /**
     * 取值方法
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