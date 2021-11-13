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
        // 创建数据源
        DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);
        // 获取链接
        Connection connection = dataSource.getConnection();
        // sql 预编译
        PreparedStatement preparedStatement = connection.prepareStatement("CREATE TABLE FOO (ID INT IDENTITY, BAR VARCHAR(64))");
        // 执行 sql
        preparedStatement.execute();
        preparedStatement = connection.prepareStatement("CREATE TABLE FOO (ID INT IDENTITY, BAR VARCHAR(64))");
        // 执行 sql
        preparedStatement.execute();

        // CREATE TABLE FOO (ID INT IDENTITY, BAR VARCHAR(64))
        // CREATE TABLE Table2 (ID INT IDENTITY, User VARCHAR(64),Name VARCHAR(64))
        // CREATE TABLE Table3 (ID INT IDENTITY, age bigint(20),sex VARCHAR(15))


        preparedStatement = connection.prepareStatement("INSERT INTO FOO (ID, BAR) VALUES (1, 'aaa')");
        int updateCount = preparedStatement.executeUpdate();
        preparedStatement = connection.prepareStatement("SELECT * FROM FOO");
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            String id = resultSet.getString("id");
            String value = resultSet.getString("bar");
            log.info("select result info is : " + id + " - " + value);
        }
        // 释放 statement 资源
        preparedStatement.close();
        // 释放连接资源
        connection.close();
    }


}
