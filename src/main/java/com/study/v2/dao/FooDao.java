package com.study.v2.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/10/17 3:02 下午
 */
@Slf4j
@Repository
public class FooDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;


	public void insertData() {
		Arrays.asList("b", "c").forEach(bar -> jdbcTemplate.update("INSERT INTO FOO (BAR) VALUES (?)", bar));

		HashMap<String, String> row = new HashMap<>(8);
		// 字段名 -> 值
		row.put("BAR", "d");
	}

	public void listData() {
		log.info("count : {}", jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FOO", Long.class));

		List<String> list = jdbcTemplate.queryForList("SELECT BAR FROM FOO", String.class);
		list.forEach(v -> log.info("foo list : {}", v));

		List<Foo> fooList = jdbcTemplate.query("SELECT * FROM FOO",
				(resultSet, i) -> Foo.builder()
						.id(resultSet.getLong(1))
						.bar(resultSet.getString(2)).build());
		fooList.forEach(v -> log.info("fooList : {}", v));
	}



}
