package com.yarda.zookeeper.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author xuezheng
 * @date 2022/3/24-16:05
 */
@Data
@Component
@ConfigurationProperties("curator")
public class CuratorProperties {
    private int retryCount;

    private int elapsedTimeMs;

    private String connectString;

    private int sessionTimeoutMs;

    private int connectionTimeoutMs;
}
