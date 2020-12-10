package org.hv.pocket.criteria;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.model.AbstractEntity;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.session.Session;
import org.hv.pocket.utils.EncryptUtil;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wujianchuan 2019/1/10
 */
abstract class AbstractCriteria {
    final Class<? extends AbstractEntity> clazz;
    final Session session;
    final Connection connection;
    final DatabaseNodeConfig databaseConfig;
    boolean showSqlLog;

    List<Restrictions> restrictionsList = new LinkedList<>();
    List<Restrictions> sortedRestrictionsList = new LinkedList<>();
    List<Modern> modernList = new LinkedList<>();
    List<Sort> orderList = new LinkedList<>();
    Map<String, Object> parameterMap = new HashMap<>();
    List<ParameterTranslator> parameters = new LinkedList<>();
    Set<String> specifyFieldNames = new HashSet<>();
    private Integer start;
    private Integer limit;
    StringBuilder completeSql = new StringBuilder();

    AbstractCriteria(Class<? extends AbstractEntity> clazz, Session session) {
        this.clazz = clazz;
        this.session = session;
        this.connection = this.session.getConnection();
        this.databaseConfig = this.session.getDatabaseNodeConfig();
        this.showSqlLog = this.databaseConfig.getShowSql();
    }

    public Session getSession() {
        return session;
    }

    public Class<? extends AbstractEntity> getClazz() {
        return clazz;
    }

    public List<Restrictions> getRestrictionsList() {
        return restrictionsList;
    }

    void cleanAll() {
        this.cleanWithoutRestrictions();
        this.cleanRestrictions();
    }

    void cleanWithoutRestrictions() {
        this.modernList = new LinkedList<>();
        this.orderList = new LinkedList<>();
        this.parameterMap = new HashMap<>(16);
        this.parameters = new LinkedList<>();
        this.start = null;
        this.limit = null;
        this.completeSql = new StringBuilder();
    }

    void cleanRestrictions() {
        this.sortedRestrictionsList = new LinkedList<>();
        this.restrictionsList = new LinkedList<>();
    }

    void setLimit(int start, int limit) {
        this.start = start;
        this.limit = limit;
    }

    boolean limited() {
        return this.start != null && this.limit != null;
    }

    Integer getStart() {
        return start;
    }

    Integer getLimit() {
        return limit;
    }

    void showLog(boolean showLog) {
        this.showSqlLog = showLog;
    }

    void setSpecifyFieldNames(String... fieldNames) {
        this.specifyFieldNames.addAll(Arrays.asList(fieldNames));
    }

    <T extends SqlBean> T encryptTarget(T sqlBean) {
        if (clazz != null) {
            // NOTE: 判断字段值是否需要加密
            if (sqlBean instanceof Restrictions) {
                Restrictions[] restrictions = ((Restrictions) sqlBean).getRestrictions();
                if (restrictions != null) {
                    String innerEncryptModel;
                    Object innerTarget;
                    for (Restrictions restriction : restrictions) {
                        innerEncryptModel = MapperFactory.getEncryptModel(clazz.getName(), restriction.getSource());
                        innerTarget = restriction.getTarget();
                        if (innerTarget != null && !StringUtils.isEmpty(innerEncryptModel) && !restriction.getEncrypted()) {
                            restriction.setEncrypted(true);
                            restriction.setTarget(EncryptUtil.encrypt(innerEncryptModel, innerTarget.toString()));
                        }
                    }
                }
            }
            String encryptModel = MapperFactory.getEncryptModel(clazz.getName(), sqlBean.getSource());
            Object target = sqlBean.getTarget();
            if (target != null && !StringUtils.isEmpty(encryptModel) && !sqlBean.getEncrypted()) {
                sqlBean.setEncrypted(true);
                sqlBean.setTarget(EncryptUtil.encrypt(encryptModel, target.toString()));
            }
        }
        return sqlBean;
    }
}
