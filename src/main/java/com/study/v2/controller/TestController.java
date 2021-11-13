package com.study.v2.controller;

import com.study.v2.dao.FooDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/11/13 5:27 下午
 */
@RestController
public class TestController {

	@Autowired
	private FooDao fooDao;

	@RequestMapping(value = "/app",method = RequestMethod.GET)
	public String demo(){
		fooDao.insertData();
		fooDao.listData();
		return  "aa";
	}
}
