package org.dc.jdbc.example.master_slave;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.DataBaseStream;
import org.dc.jdbc.example.entity.User;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;

/**
 * 主从数据库操作example
 * @author dc
 *
 */
public class Test2 {
    private static Logger LOG = Logger.getLogger(Test2.class.getName());
	public static void main(String[] args) throws Exception {
		try {
            String s = null;
            s.length();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "closeConnectionAll fail",e);
        }
		
	}
}
