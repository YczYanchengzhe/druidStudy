package com.study.v2.service;

import com.study.v2.exception.RollBackException;

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
