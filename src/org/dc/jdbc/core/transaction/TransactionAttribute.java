package org.dc.jdbc.core.transaction;

/**
 * 事务定义标识
 * Created by wyx on 2016/4/23.
 */
public class TransactionAttribute {
    private boolean readonly;

    public boolean isReadonly() {
        return readonly;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }
}
