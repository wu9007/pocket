package org.hv.pocket.criteria;

import org.hv.pocket.logger.PersistenceLogSubject;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.session.Session;

import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan
 */
public class CriteriaLogProxy {
    private final CriteriaImpl target;
    private final Session session;
    private final Class<? extends AbstractEntity> clazz;
    private String sql;
    private List<?> beforeMirror;
    private List<?> afterMirror;

    public static CriteriaLogProxy newInstance(CriteriaImpl target) {
        return new CriteriaLogProxy(target);
    }

    public CriteriaLogProxy(CriteriaImpl target) {
        this.target = target;
        this.session = target.getSession();
        this.clazz = target.getClazz();
    }

    public int update() throws SQLException {
        this.beforeMirror = this.loadMirror();
        int result = target.doUpdate(this);
        if (result > 0) {
            this.afterMirror = this.loadMirror();
            PersistenceLogSubject.getInstance().pushLog(this.sql, this.beforeMirror, this.afterMirror);
        }
        return result;
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

    private List<?> loadMirror() {
        Criteria selectCriteria = session.createCriteria(clazz);
        target.getRestrictionsList().forEach(selectCriteria::add);
        return selectCriteria.list();
    }

    void setSql(String preparedStatementSql) {
        this.sql = preparedStatementSql;
    }
}
