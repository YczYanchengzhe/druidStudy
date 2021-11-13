package com.asdjl.pool;

import com.asdjl.pool.exception.InitException;

import javax.sql.DataSource;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;


/**
 * @author chengzhe yan
 * @description : 对于数据源公有操作的封装
 * @date 2021/11/13 5:58 下午
 */
public abstract class GaeaAbstractDataSources implements DataSource {


	protected void initDataSource() {
		throw new InitException("initDataSource error");
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// sql 版本不支持异常 , 没有使用 java.util.logging.Logger , 所以直接异常
		throw new SQLFeatureNotSupportedException();
	}

}
