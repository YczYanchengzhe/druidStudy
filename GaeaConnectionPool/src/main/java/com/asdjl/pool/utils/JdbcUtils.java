package com.asdjl.pool.utils;

import java.sql.Driver;

/**
 * @author chengzhe yan
 * @description TODO 这里是否可以做成工厂 , 每种数据类型都有自己的操作
 * @date 2021/11/14 3:21 下午
 */
public class JdbcUtils {

	public static final String MYSQL = "jdbc:mysql:";
	public static final String H2 = "jdbc:h2:";

	public static String getDbTypeByUrl(String url) {
		if (url.startsWith(MYSQL)) {
			return "mysql";
		} else if (url.startsWith(H2)) {
			return "h2";
		} else {
			throw new RuntimeException("check db type error , no apply type [" + url + "]");
		}
	}

	public static Driver getDriverByUrl(String url) {
		try {
			if (url.startsWith(MYSQL)) {
				return getDriver("com.mysql.cj.jdbc.Driver");
			} else if (url.startsWith(H2)) {
				return getDriver("org.h2.Driver");
			} else {
				throw new RuntimeException("check db type error , no apply type [" + url + "]");
			}
		} catch (Exception e) {
			throw new RuntimeException("init driver error");
		}
	}

	private static Driver getDriver(String driverClass) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		return (Driver) Class.forName(driverClass).newInstance();
	}
}
