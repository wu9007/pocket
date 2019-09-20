package org.hv.pocket.constant;

/**
 * @author wujianchuan
 */
public enum JoinMethod {
    /**
     * 关联方式
     */
    LEFT(" LEFT JOIN "), RIGHT(" RIGHT JOIN "), INNER(" INNER JOIN ");
    private final String id;

    JoinMethod(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }}
