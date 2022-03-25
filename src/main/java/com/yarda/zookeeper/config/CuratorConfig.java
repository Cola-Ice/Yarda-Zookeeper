package com.yarda.zookeeper.config;

import com.yarda.zookeeper.config.properties.CuratorProperties;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author xuezheng
 * @date 2022/3/24-16:10
 */
@Component
public class CuratorConfig {
    @Resource
    private CuratorProperties curatorProperties;

    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework(){
        return CuratorFrameworkFactory.newClient(
                curatorProperties.getConnectString(),
                curatorProperties.getSessionTimeoutMs(),
                curatorProperties.getConnectionTimeoutMs(),
                new RetryNTimes(curatorProperties.getRetryCount(), curatorProperties.getElapsedTimeMs())
        );
    }
}
