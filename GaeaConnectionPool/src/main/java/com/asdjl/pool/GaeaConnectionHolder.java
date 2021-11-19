package com.asdjl.pool;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author chengzhe yan
 * @description : 对于连接的封装
 * @date 2021/11/13 7:45 下午
 */
public class GaeaConnectionHolder {
	/**
	 * holder 封装的链接
	 */
	private Connection connection;
	/**
	 * 创建该链接的数据源
	 */
	private DataSource dataSource;

	public GaeaConnectionHolder(Connection connection, DataSource dataSource) {
		this.connection = connection;
		this.dataSource = dataSource;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
