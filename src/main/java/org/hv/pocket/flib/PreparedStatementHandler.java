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
    private PreparedStatement preparedStatement;

    private PreparedStatementHandler(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    public static PreparedStatementHandler newInstance(PreparedStatement preparedStatement) {
        return new PreparedStatementHandler(preparedStatement);
    }

    public void completionPreparedStatement(List<ParameterTranslator> parameters, List<Restrictions> restrictionsList) throws SQLException {
        this.completionPreparedStatement(parameters);
        for (int index = 0; index < restrictionsList.size(); index++) {
            Restrictions restrictions = restrictionsList.get(index);
            PreparedSupplierValue preparedSupplierValue = new PreparedSupplierValue(preparedStatement, parameters.size() + index + 1, restrictions);
            this.apply(preparedSupplierValue);
        }
    }

    public void completionPreparedStatement(List<ParameterTranslator> parameterTranslatorList) throws SQLException {
        for (int index = 0; index < parameterTranslatorList.size(); index++) {
            ParameterTranslator parameterTranslator = parameterTranslatorList.get(index);
            PreparedSupplierValue preparedSupplierValue = new PreparedSupplierValue(preparedStatement, index + 1, parameterTranslator);
            this.apply(preparedSupplierValue);
        }
    }

    private void apply(PreparedSupplierValue preparedSupplierValue) throws SQLException {
        SqlBean sqlBean = preparedSupplierValue.getSqlBean();
        if (sqlBean.getTarget() != null) {
            PreparedStatementConsumerLib.get(sqlBean.getTarget().getClass().getName()).accept(preparedSupplierValue);
        } else {
            try {
                preparedStatement.setObject(preparedSupplierValue.getIndex(), sqlBean.getTarget());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
