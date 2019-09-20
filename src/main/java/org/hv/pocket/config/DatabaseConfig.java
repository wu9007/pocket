package org.hv.pocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wujianchuan 2019/1/16
 */
@Component
@ConfigurationProperties(prefix = "pocket.datasource")
public class DatabaseConfig {

    private Integer serverId;

    private List<DatabaseNodeConfig> node;

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public List<DatabaseNodeConfig> getNode() {
        return node;
    }

    public void setNode(List<DatabaseNodeConfig> node) {
        this.node = node;
    }
}
