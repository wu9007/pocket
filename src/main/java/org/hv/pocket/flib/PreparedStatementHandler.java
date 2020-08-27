package org.hv.pocket.flib;

import org.hv.pocket.criteria.ParameterTranslator;
import org.hv.pocket.criteria.Restrictions;
import org.hv.pocket.criteria.SqlBean;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author wujianchuan
 */
public class PreparedStatementHandler {
    private final PreparedStatement preparedStatement;
    private final Class<?> clazz;

    private PreparedStatementHandler(Class<?> clazz, PreparedStatement preparedStatement) {
        this.clazz = clazz;
        this.preparedStatement = preparedStatement;
    }

    public static PreparedStatementHandler newInstance(Class<?> clazz, PreparedStatement preparedStatement) {
        return new PreparedStatementHandler(clazz, preparedStatement);
    }

    public void completionPreparedStatement(List<ParameterTranslator> parameters, List<Restrictions> restrictionsList) throws SQLException {
        this.completionPreparedStatement(parameters);
        for (int index = 0; index < restrictionsList.size(); index++) {
            Restrictions restrictions = restrictionsList.get(index);
            PreparedSupplierValue preparedSupplierValue = new PreparedSupplierValue(clazz, preparedStatement, parameters.size() + index + 1, restrictions);
            this.apply(preparedSupplierValue);
        }
    }

    public void completionPreparedStatement(List<ParameterTranslator> parameterTranslatorList) throws SQLException {
        for (int index = 0; index < parameterTranslatorList.size(); index++) {
            ParameterTranslator parameterTranslator = parameterTranslatorList.get(index);
            PreparedSupplierValue preparedSupplierValue = new PreparedSupplierValue(clazz, preparedStatement, index + 1, parameterTranslator);
            this.apply(preparedSupplierValue);
        }
    }

    /**
     * Sets the designated parameter to the given Java value.
     *
     * @param preparedSupplierValue box
     * @throws SQLException e
     */
    private void apply(PreparedSupplierValue preparedSupplierValue) throws SQLException {
        SqlBean sqlBean = preparedSupplierValue.getSqlBean();
        if (sqlBean.getTarget() != null) {
            String targetClassName = sqlBean.getTarget().getClass().getName();
            PreparedStatementConsumerLib.get(targetClassName).accept(preparedSupplierValue);
        } else {
            preparedStatement.setObject(preparedSupplierValue.getIndex(), null);
        }
    }
}
