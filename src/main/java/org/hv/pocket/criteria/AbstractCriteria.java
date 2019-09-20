package org.hv.pocket.criteria;

import org.hv.pocket.config.DatabaseNodeConfig;
import org.hv.pocket.session.Session;
import org.hv.pocket.utils.FieldTypeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author wujianchuan 2019/1/10
 */
abstract class AbstractCriteria {

    private final Logger logger = LoggerFactory.getLogger(AbstractCriteria.class);
    final FieldTypeStrategy fieldTypeStrategy = FieldTypeStrategy.getInstance();

    final Class clazz;
    final Session session;
    final Connection connection;
    final DatabaseNodeConfig databaseConfig;

    List<Restrictions> restrictionsList = new LinkedList<>();
    List<Restrictions> sortedRestrictionsList = new LinkedList<>();
    List<Modern> modernList = new LinkedList<>();
    List<Sort> orderList = new LinkedList<>();
    Map<String, Object> parameterMap = new HashMap<>();
    List<ParameterTranslator> parameters = new LinkedList<>();
    private Integer start;
    private Integer limit;
    StringBuilder completeSql = new StringBuilder();

    AbstractCriteria(Class clazz, Session session) {
        this.clazz = clazz;
        this.session = session;
        this.connection = this.session.getConnection();
        this.databaseConfig = this.session.getDatabaseNodeConfig();
    }

    public Session getSession() {
        return session;
    }

    void cleanAll() {
        this.cleanWithoutRestrictions();
        this.cleanRestrictions();
    }

    void cleanWithoutRestrictions() {
        modernList = new LinkedList<>();
        orderList = new LinkedList<>();
        parameterMap = new HashMap<>(16);
        parameters = new LinkedList<>();
        start = null;
        limit = null;
        completeSql = new StringBuilder();
    }

    void cleanRestrictions() {
        sortedRestrictionsList = new LinkedList<>();
        this.restrictionsList = new LinkedList<>();
    }

    void showSql() {
        if (this.databaseConfig.getShowSql()) {
            this.logger.info("Pocket: {}", this.completeSql);
        }
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
}
