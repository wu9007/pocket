package org.hv.pocket.criteria;

import org.hv.pocket.constant.SqlOperateTypes;

/**
 * @author wujianchuan 2019/1/21
 */
public class Sort {

    private final String source;
    private final String sortType;

    private Sort(String source, String sortType) {
        this.source = source;
        this.sortType = sortType;
    }

    public static Sort asc(String source) {
        return new Sort(source, SqlOperateTypes.ASC);
    }

    public static Sort desc(String source) {
        return new Sort(source, SqlOperateTypes.DESC);
    }

    String getSource() {
        return source;
    }

    String getSortType() {
        return sortType;
    }
}
