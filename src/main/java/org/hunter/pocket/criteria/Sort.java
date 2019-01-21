package org.hunter.pocket.criteria;

import org.hunter.pocket.constant.SqlOperateTypes;

/**
 * @author wujianchuan 2019/1/21
 */
public class Sort {

    private String source;
    private String sortType;

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

    public String getSource() {
        return source;
    }

    public String getSortType() {
        return sortType;
    }
}
