package org.homo.core.constant;

/**
 * @author wujianchuan 2018/12/27
 */
public enum TransactionOperateTypes {
    /**
     * 新增操作
     */
    OPEN("开启事务"),
    CLOSE("关闭事务");

    TransactionOperateTypes(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }}

