package org.homo.dbconnect.criteria;

/**
 * @author wujianchuan 2019/1/12
 */
public class CriteriaFactory {
    private static CriteriaFactory ourInstance = new CriteriaFactory();

    public static CriteriaFactory getInstance() {
        return ourInstance;
    }

    private CriteriaFactory() {
    }
}
