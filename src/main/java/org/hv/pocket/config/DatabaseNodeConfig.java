package org.hv.pocket.config;


/**
 * @author wujianchuan 2018/12/31
 */
public class DatabaseNodeConfig {

    private String url;
    private String nodeName;
    private String driverName;
    private Boolean showSql;
    private String user;
    private String password;

    private Integer poolMiniSize;
    private Integer poolMaxSize;
    private Long timeout;
    private Integer retry;
    private Integer cacheSize;

    private String session;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public Boolean getShowSql() {
        return showSql;
    }

    public void setShowSql(Boolean showSql) {
        this.showSql = showSql;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getPoolMiniSize() {
        return poolMiniSize;
    }

    public void setPoolMiniSize(Integer poolMiniSize) {
        this.poolMiniSize = poolMiniSize;
    }

    public Integer getPoolMaxSize() {
        return poolMaxSize;
    }

    public void setPoolMaxSize(Integer poolMaxSize) {
        this.poolMaxSize = poolMaxSize;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Integer getRetry() {
        return retry;
    }

    public void setRetry(Integer retry) {
        this.retry = retry;
    }

    public Integer getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Integer cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
