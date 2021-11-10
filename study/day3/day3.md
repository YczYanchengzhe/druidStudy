## 一. 获取链接 ,SQL 预处理 , SQL 执行 , 链接关闭流程

- 获取链接 : 先执行获取链接的 filter , 在 filter 的最后会调用直接获取链接

```java
        if(filters.size()>0){
        FilterChainImpl filterChain=new FilterChainImpl(this);
        return filterChain.dataSource_connect(this,maxWaitMillis);
        }else{
        return getConnectionDirect(maxWaitMillis);
        }
```

![](getConnectionDirect.png)

- preparement , sql 预编译

![](prepareStatement.png)

- execute : sql 执行

![](execute.png)

- statement 关闭,释放资源
  ![](preparedStatement.close().png)

- connection 关闭,放回连接池
  ![](connection.close.png)

