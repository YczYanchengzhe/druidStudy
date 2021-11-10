package com.study.v1;


import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.stat.DruidDataSourceStatManager;
import lombok.extern.slf4j.Slf4j;

import javax.management.openmbean.TabularData;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/11/8 5:11 下午
 */
@Slf4j
public class Demo {

    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("url", "jdbc:h2:mem:foo");
        properties.setProperty("filters", "config,stat,slf4j,conn");
        // TODO 需要整理一下创建连接的流程
        DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);
        // TODO 需要过一遍获取链接的流程
        Connection connection = dataSource.getConnection();

        PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE FOO (ID INT IDENTITY, BAR VARCHAR(64))");
        preparedStatement.execute();
        preparedStatement = connection.prepareStatement("INSERT INTO FOO (ID, BAR) VALUES (1, 'aaa')");
        int updateCount = preparedStatement.executeUpdate();
        preparedStatement = connection.prepareStatement("SELECT * FROM FOO");
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            String id = resultSet.getString("id");
            String value = resultSet.getString("bar");
            log.info("select result info is : " + id + " - " + value);
        }
        preparedStatement.close();
        connection.close();
    }


}
