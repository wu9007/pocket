package org.hv.pocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wujianchuan 2019/1/16
 */
@ConfigurationProperties(prefix = "pocket.datasource")
public class DatabaseConfig {

    private Map<String, DatabaseNodeConfig> nodeConfigMap;

    private List<DatabaseNodeConfig> node;

    public List<DatabaseNodeConfig> getNode() {
        return node;
    }

    public DatabaseNodeConfig getNode(String nodeName) {
        return nodeConfigMap.get(nodeName);
    }

    public void setNode(List<DatabaseNodeConfig> node) {
        this.node = node;
        this.nodeConfigMap = this.node.stream().collect(Collectors.toMap(DatabaseNodeConfig::getNodeName, item -> item));
    }
}
