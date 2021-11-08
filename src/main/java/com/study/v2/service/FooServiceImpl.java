package com.study.v2.service;

import com.study.v2.exception.RollBackException;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/10/17 4:03 下午
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@Component
public class FooServiceImpl implements FooService {

	@Autowired
	private FooService fooService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	@Transactional
	public void insertRecode() {
		jdbcTemplate.execute("INSERT INTO FOO  (BAR) VALUES('AAA')");
	}

	@Override
	@Transactional(rollbackFor = RollBackException.class)
	public void insertThenRollback() throws RollBackException {
		jdbcTemplate.execute("INSERT INTO FOO  (BAR) VALUES('BBB')");
		throw new RollBackException();
	}

	@Override
	public void invokeInsertThenRollBack() throws RollBackException {
		// 这种调用实际上是直接调用的方法 ,因为声明式事务是基于 AOP 的代理实现的
		// 那么如果在方法内部调用这个方法的话就执行不到事务的逻辑,导致没有成功使用事务
		insertThenRollback();
		// 1. 使用代理
		((FooService) AopContext.currentProxy()).insertThenRollback();
		// 2. 注入自己
		fooService.insertThenRollback();
		// 3. 再加一层事务(这里不演示了)

		// 4. 从 context get bean 之后调用
	}
}
