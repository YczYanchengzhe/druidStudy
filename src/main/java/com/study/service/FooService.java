package com.study.service;

import com.study.exception.RollBackException;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/10/17 4:02 下午
 */
public interface FooService {
	void insertRecode();

	void insertThenRollback() throws RollBackException;

	void invokeInsertThenRollBack() throws RollBackException;
}
