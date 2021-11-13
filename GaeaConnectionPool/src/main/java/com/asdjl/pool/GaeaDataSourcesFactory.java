package com.asdjl.pool;

import java.io.PrintWriter;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Properties;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/11/13 10:37 下午
 */
public class GaeaDataSourcesFactory {

	public static final String URL = "url";
	public static final String USER_NAME = "username";
	public static final String PASSWORD = "password";
	public static final String NEED_INIT = "needInit";

	public static final String INIT_SIZE = "initSize";
	public static final String MAX_ACTIVE = "maxActive";
	public static final String MIN_ACTIVE = "minActive";


	public static DataSource createDateSources(Properties properties) {
		GaeaDataSources dataSource = new GaeaDataSources();
		// 初始化配置
		dataSource.setUrl(properties.getProperty(URL, ""));
		dataSource.setUsername(properties.getProperty(USER_NAME, ""));
		dataSource.setPassword(properties.getProperty(PASSWORD, ""));

		dataSource.setInitialSize(Integer.parseInt(properties.getProperty(INIT_SIZE, "10")));
		dataSource.setMaxActive(Integer.parseInt(properties.getProperty(MAX_ACTIVE, "10")));
		dataSource.setMinIdle(Integer.parseInt(properties.getProperty(MIN_ACTIVE, "10")));

		// 创建连接池
		if (Boolean.parseBoolean(properties.getProperty(NEED_INIT))) {
			dataSource.initDataSource();
		}
		return dataSource;
	}
}
