package com.asdjl.pool;


import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;


/**
 * @author chengzhe yan
 * @description
 * @date 2021/11/13 10:36 下午
 */
public class GaeaDataSourcesTest {





	@Test
	public void testUse() throws SQLException {
		Properties properties = new Properties();
		properties.put(GaeaDataSourcesFactory.MAX_ACTIVE, 10);
		properties.put(GaeaDataSourcesFactory.INIT_SIZE, 10);
		properties.put(GaeaDataSourcesFactory.MIN_ACTIVE, 10);
		properties.put(GaeaDataSourcesFactory.NEED_INIT, true);
		properties.put(GaeaDataSourcesFactory.PASSWORD, "");
		properties.put(GaeaDataSourcesFactory.URL, "jdbc:h2:mem:foo");
		properties.put(GaeaDataSourcesFactory.USER_NAME, "");
		DataSource dataSource = GaeaDataSourcesFactory.createDateSources(properties);
		Connection connection = dataSource.getConnection();
		// sql 预编译
		PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE FOO (ID INT IDENTITY, BAR VARCHAR(64))");
		// 执行 sql
		preparedStatement.execute();
		preparedStatement = connection.prepareStatement("INSERT INTO FOO (ID, BAR) VALUES (1, 'aaa')");
		int updateCount = preparedStatement.executeUpdate();
		preparedStatement = connection.prepareStatement("SELECT * FROM FOO");
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			String id = resultSet.getString("id");
			String value = resultSet.getString("bar");
			System.out.println("id is : " + id + ", bar is : " + value);
		}
		// 释放 statement 资源
		preparedStatement.close();
		// 释放连接资源
		connection.close();


	}

}