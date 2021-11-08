package com.study.dao;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/10/17 3:08 下午
 */
@Data
@Builder
@ToString
public class Foo {
	private Long id;
	private String bar;
}
