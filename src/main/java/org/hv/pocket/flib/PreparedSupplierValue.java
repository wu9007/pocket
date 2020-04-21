package org.hv.pocket.flib;

import org.hv.pocket.criteria.Modern;
import org.hv.pocket.criteria.ParameterTranslator;
import org.hv.pocket.criteria.Restrictions;
import org.hv.pocket.criteria.SqlBean;

import java.sql.PreparedStatement;

/**
 * @author wujianchuan
 */
final class PreparedSupplierValue {
    private final PreparedStatement preparedStatement;
    private final Integer index;
    private Restrictions restrictions;
    private Modern modern;
    private ParameterTranslator parameterTranslator;


    PreparedSupplierValue(PreparedStatement preparedStatement, Integer index, Restrictions restrictions) {
        this.preparedStatement = preparedStatement;
        this.index = index;
        this.restrictions = restrictions;
    }

    PreparedSupplierValue(PreparedStatement preparedStatement, Integer index, ParameterTranslator parameterTranslator) {
        this.preparedStatement = preparedStatement;
        this.index = index;
        this.parameterTranslator = parameterTranslator;
    }

    PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    Integer getIndex() {
        return index;
    }

    SqlBean getSqlBean() {
        if (this.restrictions != null) {
            return restrictions;
        } else if (this.modern != null) {
            return this.modern;
        } else {
            return this.parameterTranslator;
        }
    }
}
