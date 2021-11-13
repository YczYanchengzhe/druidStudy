# 连接池结构设计

## 初始化配置

```java
public class GaeaDataSourcesFactory {

	public static final String URL = "url";
	public static final String USER_NAME = "username";
	public static final String PASSWORD = "password";
	public static final String NEED_INIT = "needInit";

	public static final String INIT_SIZE = "initSize";
	public static final String MAX_ACTIVE = "maxActive";
	public static final String MIN_ACTIVE = "minActive";


	public static DataSource createDateSources(Properties properties) {
		GaeaDataSources dataSource = new GaeaDataSources();
		// 初始化配置
		dataSource.setUrl(properties.getProperty(URL, ""));
		dataSource.setUsername(properties.getProperty(USER_NAME, ""));
		dataSource.setPassword(properties.getProperty(PASSWORD, ""));

		dataSource.setInitialSize(Integer.parseInt(properties.getProperty(INIT_SIZE, "10")));
		dataSource.setMaxActive(Integer.parseInt(properties.getProperty(MAX_ACTIVE, "10")));
		dataSource.setMinIdle(Integer.parseInt(properties.getProperty(MIN_ACTIVE, "10")));

		// 创建连接池
		if (Boolean.parseBoolean(properties.getProperty(NEED_INIT))) {
			dataSource.initDataSource();
		}
		return dataSource;
	}
}
```

## 数据源初始化
- 该步骤还未完成
- 这里考虑支持 h2 和 mysql
```java

public class GaeaDataSources extends GaeaAbstractDataSources {

	private static Logger logger = LoggerFactory.getLogger(GaeaDataSources.class);

	/**
	 * 可用连接池 : 所有没有被使用的链接
	 */
	private GaeaConnectionHolder[] connectionPool;
	/**
	 * 存活链接池 : 所有存活的链接,包括正在使用和未被使用
	 */
	private GaeaConnectionHolder[] activeConnections;
	/**
	 * 需要被回收的链接
	 */
	private GaeaConnectionHolder[] abandonConnections;

	private String url;
	private String username;
	private String password;

	private volatile boolean isInit = false;

	/**
	 * 初始化连接数
	 */
	private volatile int initialSize;
	/**
	 * 最大存活链接
	 */
	private volatile int maxActive;
	/**
	 * 最小空闲连接
	 */
	private volatile int minIdle;

	protected ReentrantLock lock;


	@Override
	protected void initDataSource() {
		if (StringUtil.isEmpty(url)) {
			throw new InitException("url is null , please check url");
		}

		// 考虑并发初始化问题,需要加锁 , 加锁前后需要进行 double check
		if (isInit) {
			return;
		}

		final ReentrantLock lock = this.lock;
		try {
			lock.lockInterruptibly();
		} catch (InterruptedException e) {
			logger.error("InterruptedException : ", e);
			throw new InitException("InterruptedException", e);
		}
		try {
			// 确定数据库类型 : 这里考虑支持 h2 和 mysql , 需要根据 url 来判断数据库类型,选择数据库驱动

			// 根据 url 决定数据库驱动类

			// 记录当前系统中存活链接数量

			// 参数合法性校验

			// 加载 jdbc 驱动类

			// 初始化连接池数组

			{
				// 创建连接
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
}
```