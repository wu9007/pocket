package org.hv.pocket.config;


import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.utils.EncryptUtil;

/**
 * @author wujianchuan 2018/12/31
 */
public class DatabaseNodeConfig {

    private String url;
    private String nodeName;
    private String driverName;
    private Boolean showSql;
    private Boolean collectLog;
    private Long warningLogTimeout;
    private String user;
    private String password;
    private String encryptedUser;
    private String encryptedPassword;

    private Integer poolMiniSize;
    private Integer poolMaxSize;
    private Long timeout;
    private Integer retry;
    private Integer cacheSize;
    /**
     * Length of time (in second) between maintenance connect availability (default 1800s)
     */
    private Integer availableInterval;
    /**
     * The length of time (in second) between maintaining the number of connect list (default 36000s)
     */
    private Integer miniInterval;

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

    public Boolean getCollectLog() {
        return collectLog;
    }

    public void setCollectLog(Boolean collectLog) {
        this.collectLog = collectLog;
    }

    public Long getWarningLogTimeout() {
        return warningLogTimeout;
    }

    public void setWarningLogTimeout(Long warningLogTimeout) {
        this.warningLogTimeout = warningLogTimeout;
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

    public String getEncryptedUser() {
        return encryptedUser;
    }

    public void setEncryptedUser(String encryptedUser) {
        this.encryptedUser = encryptedUser;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
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

    public Integer getAvailableInterval() {
        return availableInterval;
    }

    public void setAvailableInterval(Integer availableInterval) {
        this.availableInterval = availableInterval;
    }

    public Integer getMiniInterval() {
        return miniInterval;
    }

    public void setMiniInterval(Integer miniInterval) {
        this.miniInterval = miniInterval;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
