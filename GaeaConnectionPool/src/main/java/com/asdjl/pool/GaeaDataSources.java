package com.asdjl.pool;

import com.asdjl.pool.exception.InitException;
import com.asdjl.pool.utils.JdbcUtils;
import com.asdjl.pool.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author chengzhe yan
 * @description : 数据源的实现
 * @date 2021/11/13 5:58 下午
 */
public class GaeaDataSources extends GaeaAbstractDataSources {

	private static Logger logger = LoggerFactory.getLogger(GaeaDataSources.class);


	protected ReentrantLock lock = new ReentrantLock();


	@Override
	protected void initDataSource() {
		if (StringUtil.isEmpty(url)) {
			throw new InitException("url is null , please check url");
		}

		// 考虑并发初始化问题,需要加锁 , 加锁前后需要进行 double check
		if (isInit) {
			isInit = true;
			return;
		}
		final ReentrantLock lock = this.lock;
		try {
			try {
				lock.lockInterruptibly();
			} catch (InterruptedException e) {
				logger.error("InterruptedException : ", e);
				throw new InitException("InterruptedException", e);
			}
			// 确定数据库类型 : 这里考虑支持 h2 和 mysql , 需要根据 url 来判断数据库类型,选择数据库驱动
			if (StringUtil.isEmpty(dbType)) {
				dbType = JdbcUtils.getDbTypeByUrl(url);
			}
			// 根据 url 决定数据库驱动类
			if (Objects.isNull(driver)) {
				driver = JdbcUtils.getDriverByUrl(url);
			}

			// 初始化连接池数组
			connectionPool = new GaeaConnectionHolder[maxActive];
			activeConnections = new GaeaConnectionHolder[maxActive];
			abandonConnections = new GaeaConnectionHolder[maxActive];

			{
				// 创建连接
			}

			while (poolingCount < initialSize) {
				try {
					Connection connection = createConnection();
					GaeaConnectionHolder holder = new GaeaConnectionHolder(connection, this);
					connectionPool[poolingCount++] = holder;
				} catch (Exception e) {
					logger.error("create connection error : ", e);
				}
			}

			// 创建连接线程初始化 :  ,采用生产者消费者模型 ,不停创建链接

			// 销毁链接线程初始化  :  , 采用生产者消费者模型 , 不停销毁链接

			// 扩缩容策略  :
			//  当存活链接 > 最大连接使用 , 触发缩容 , 将连接放到需要销毁的连接池中,触发销毁操作
			//  当存活链接 < 最小可用链接时候 ,触发扩容 , 发送信号,创建连接.
			//  每次执行完 sql 之后都需要检测是否需要触发扩缩容策略
		} finally {
			lock.unlock();
		}
	}


	@Override
	public Connection getConnection() throws SQLException {
		if (!isInit) {
			initDataSource();
		}
		// TODO 获取链接
		return connectionPool[0].getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		if (!isInit) {
			initDataSource();
		}
		return connectionPool[0].getConnection();
	}

}
