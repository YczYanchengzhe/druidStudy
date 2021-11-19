package com.asdjl.pool;

import com.asdjl.pool.exception.InitException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;


/**
 * @author chengzhe yan
 * @description : 对于数据源公有操作的封装
 * @date 2021/11/13 5:58 下午
 */
public abstract class GaeaAbstractDataSources implements DataSource {

	/**
	 * 需要通过 驱动来创建链接,所以每个数据源都要有一个驱动
	 */
	protected Driver driver;

	protected String url;
	protected String username;
	protected String password;

	/**
	 * 可用连接池 : 所有没有被使用的链接
	 */
	protected GaeaConnectionHolder[] connectionPool;
	/**
	 * 存活链接池 : 所有存活的链接,包括正在使用和未被使用
	 */
	protected GaeaConnectionHolder[] activeConnections;
	/**
	 * 需要被回收的链接
	 */
	protected GaeaConnectionHolder[] abandonConnections;

	protected volatile boolean isInit = false;

	protected volatile String dbType = "";

	/**
	 * 初始化连接数
	 */
	protected volatile int initialSize;
	/**
	 * 最大存活链接
	 */
	protected volatile int maxActive;
	/**
	 * 最小空闲连接
	 */
	private volatile int minIdle;

	/**
	 * 池子里现有的连接数
	 */
	protected volatile int poolingCount = 0;


	protected void initDataSource() {
		throw new InitException("initDataSource error");
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// sql 版本不支持异常 , 没有使用 java.util.logging.Logger , 所以直接异常
		throw new SQLFeatureNotSupportedException();
	}

	public Connection createConnection() {
		// TODO 支持自定义连接配置
		try {
			Properties properties = new Properties();
			return driver.connect(url, properties);
		} catch (Exception exception) {
			throw new RuntimeException("create connection error : ", exception);
		}
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}


	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return null;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {

	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {

	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getInitialSize() {
		return initialSize;
	}

	public void setInitialSize(int initialSize) {
		this.initialSize = initialSize;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMinIdle() {
		return minIdle;
	}

	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}
}
