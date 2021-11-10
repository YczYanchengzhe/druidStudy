package com.study.v2.filter;

import com.alibaba.druid.filter.AutoLoad;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.ConnectionProxy;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author chengzhe yan
 * @description
 * @date 2021/10/17 2:03 下午
 */
@Slf4j
@AutoLoad
public class ConnectionLogFilter extends FilterEventAdapter {
	@Override
	public void connection_connectBefore(FilterChain chain, Properties info) {
		log.info("Before connect !");
	}

	@Override
	public void connection_connectAfter(ConnectionProxy connection) {
		log.info("after connect !");
	}
}
