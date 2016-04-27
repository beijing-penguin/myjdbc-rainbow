package org.dc.jdbc.helper;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.dc.jdbc.core.ConnectionManager;
import org.dc.jdbc.core.operate.DeleteOper;
import org.dc.jdbc.core.operate.InsertOper;
import org.dc.jdbc.core.operate.SelectOper;
import org.dc.jdbc.core.operate.UpdateOper;
import org.dc.jdbc.core.sqlhandler.PrintSqlLogHandler;
import org.dc.jdbc.core.sqlhandler.SQLHandler;
import org.dc.jdbc.core.sqlhandler.XmlSqlHandler;
import org.dc.jdbc.entity.SqlEntity;

/**
 * 数据持久化操作类
 * sql执行三部曲：1，获取连接，2，适配sql与参数，3，执行核心jdbc操作。
 * @author dc
 * @time 2015-8-17
 */
public class DBHelper {
	private DataSource dataSource;
	private static final SQLHandler sqlHandler = PrintSqlLogHandler.getInstance().setSuccessor(XmlSqlHandler.getInstance());
	private static final SelectOper selectOper = SelectOper.getInstance();
	private static final UpdateOper updateOper = UpdateOper.getInstance();
	private static final InsertOper insertOper = InsertOper.getInstance();
	private static final DeleteOper deleteOper = DeleteOper.getInstance();
	
	public DBHelper(DataSource dataSource){
		this.dataSource = dataSource;
	}
	
	public <T> T selectOne(String sqlOrID,Class<? extends T> returnClass,Object...params) throws Exception{
		Connection conn = ConnectionManager.getConnection(dataSource);

		SqlEntity sqlEntity = sqlHandler.handleRequest(sqlOrID,params);
		String sql = sqlEntity.getSql();
		Object[] params_obj = sqlEntity.getParams();
		
		return selectOper.selectOne(conn,sql,returnClass,params_obj);
	}
	public Map<String,Object> selectOne(String sqlOrID,Object...params) throws Exception{
		return this.selectOne(sqlOrID, null,params);
	}
	public <T> List<T> selectList(String sqlOrID,Class<? extends T> returnClass,Object...params) throws Exception{
		Connection conn = ConnectionManager.getConnection(dataSource);

		SqlEntity sqlEntity = sqlHandler.handleRequest(sqlOrID,params);
		String sql = sqlEntity.getSql();
		Object[] params_obj = sqlEntity.getParams();

		return selectOper.selectList(conn,sql,returnClass,params_obj);
	}
	public List<Map<String,Object>> selectList(String sqlOrID,Object...params) throws Exception{
		return this.selectList(sqlOrID, null, params);
	}
	/**
	 * 返回受影响的行数
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public int insert(String sqlOrID,Object...params) throws Exception{
		Connection conn = ConnectionManager.getConnection(dataSource);

		SqlEntity sqlEntity = sqlHandler.handleRequest(sqlOrID,params);
		String sql = sqlEntity.getSql();
		Object[] params_obj = sqlEntity.getParams();

		return insertOper.insert(conn, sql, params_obj);
	}
	/**
	 * 单条语句插入，返回一个主键
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public Object insertReturnKey(String sqlOrID,Object...params) throws Exception{
		Connection conn = ConnectionManager.getConnection(dataSource);

		SqlEntity sqlEntity = sqlHandler.handleRequest(sqlOrID,params);
		String sql = sqlEntity.getSql();
		Object[] params_obj = sqlEntity.getParams();

		return insertOper.insertRtnPKKey(conn, sql, params_obj);
	}

	public int update(String sqlOrID,Object...params) throws Exception{
		Connection conn = ConnectionManager.getConnection(dataSource);

		SqlEntity sqlEntity = sqlHandler.handleRequest(sqlOrID,params);
		String sql = sqlEntity.getSql();
		Object[] params_obj = sqlEntity.getParams();

		return updateOper.update(conn, sql, params_obj);
	}


	public int delete(String sqlOrID,Object...params) throws Exception{
		Connection conn = ConnectionManager.getConnection(dataSource);

		SqlEntity sqlEntity = sqlHandler.handleRequest(sqlOrID,params);
		String sql = sqlEntity.getSql();
		Object[] params_obj = sqlEntity.getParams();

		return deleteOper.delete(conn, sql, params_obj);
	}

	public void rollback() throws Exception{
		ConnectionManager.rollback(dataSource);
	}

	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
