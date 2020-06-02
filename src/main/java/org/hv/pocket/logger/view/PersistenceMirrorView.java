package org.hv.pocket.logger.view;

import java.io.Serializable;
import java.util.List;

/**
 * 当数据库配置文件节点配置了{collectLog: true}时，
 * 系统将收集语句执行的前后镜像以及语句本身和执行效率。
 *
 * @author wujianchuan
 */
public class PersistenceMirrorView implements Serializable {
    private static final long serialVersionUID = -3682452431189320345L;
    private String sql;
    private List<?> beforeMirror;
    private List<?> afterMirror;
    private long milliseconds;

    public PersistenceMirrorView(String sql, List<?> beforeMirror, List<?> afterMirror, long milliseconds) {
        this.sql = sql;
        this.beforeMirror = beforeMirror;
        this.afterMirror = afterMirror;
        this.milliseconds = milliseconds;
    }

    public PersistenceMirrorView() {
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setBeforeMirror(List<?> beforeMirror) {
        this.beforeMirror = beforeMirror;
    }

    public void setAfterMirror(List<?> afterMirror) {
        this.afterMirror = afterMirror;
    }

    public String getSql() {
        return sql;
    }

    public List<?> getBeforeMirror() {
        return beforeMirror;
    }

    public List<?> getAfterMirror() {
        return afterMirror;
    }

    public long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(long milliseconds) {
        this.milliseconds = milliseconds;
    }
}
