
## 一. 获取链接 , SQL 预处理 , SQL 执行 , 链接关闭流程
- 获取链接 : 先执行获取链接的 filter , 在 filter 的最后会调用直接获取链接
```java
        if (filters.size() > 0) {
        FilterChainImpl filterChain = new FilterChainImpl(this);
        return filterChain.dataSource_connect(this, maxWaitMillis);
        } else {
        return getConnectionDirect(maxWaitMillis);
        }
```


## 二. 对于链接中的关键参数交互, 状态变更整理 , 连接池中的其他类作用以及功能


## 三. 连接池中的类结构理解