package org.homo.dbconnect.criteria;

import org.homo.core.model.BaseEntity;

import java.util.List;

/**
 * @author wujianchuan 2019/1/10
 */
public class CriteriaImpl extends AbstractCriteria implements Criteria {

    CriteriaImpl(Class clazz) {
        super(clazz);
    }

    @Override
    public void add(String restrictionsSql) {
        this.restrictionsSql.append(restrictionsSql);
    }

    @Override
    public List<BaseEntity> list() {
        if (this.restrictionsSql.length() > 0) {
            this.sql.append(" WHERE ").append(restrictionsSql);
        }
        return null;
    }

    @Override
    public BaseEntity unique() {
        if (this.restrictionsSql.length() > 0) {
            this.sql.append(" WHERE ").append(restrictionsSql);
        }
        return null;
    }
}
