package org.hv.pocket.flib;

import org.hv.pocket.criteria.ParameterTranslator;
import org.hv.pocket.criteria.Restrictions;
import org.hv.pocket.criteria.SqlBean;
import org.hv.pocket.model.MapperFactory;
import org.hv.pocket.utils.EncryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;

/**
 * @author wujianchuan
 */
final class PreparedSupplierValue {
    private static final Logger LOGGER = LoggerFactory.getLogger(PreparedSupplierValue.class);
    private final Class<?> clazz;
    private final PreparedStatement preparedStatement;
    private final Integer index;
    private Restrictions restrictions;
    private ParameterTranslator parameterTranslator;


    PreparedSupplierValue(Class<?> clazz, PreparedStatement preparedStatement, Integer index, Restrictions restrictions) {
        this.clazz = clazz;
        this.preparedStatement = preparedStatement;
        this.index = index;
        this.restrictions = this.encryptTarget(restrictions);
    }

    PreparedSupplierValue(Class<?> clazz, PreparedStatement preparedStatement, Integer index, ParameterTranslator parameterTranslator) {
        this.clazz = clazz;
        this.preparedStatement = preparedStatement;
        this.index = index;
        this.parameterTranslator = this.encryptTarget(parameterTranslator);
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
        } else {
            return this.parameterTranslator;
        }
    }

    private <T extends SqlBean> T encryptTarget(T sqlBean) {
        if (clazz != null) {
            String encryptModel = MapperFactory.getEncryptModel(clazz.getName(), sqlBean.getSource());
            Object target = sqlBean.getTarget();
            // NOTE: 判断字段值是否需要加密
            if (target != null && !StringUtils.isEmpty(encryptModel)) {
                sqlBean.setTarget(EncryptUtil.encrypt(encryptModel, "sward9007", target.toString()));
            }
        }
        return sqlBean;
    }
}
