package org.hunter.demo.model;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.constant.JoinMethod;
import org.hunter.pocket.model.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author wujianchuan 2018/12/26
 */
@Entity(table = "TBL_ORDER", tableId = 0, businessName = "单据")
public class Order extends BaseEntity {
    private static final long serialVersionUID = 2560385391551524826L;

    @Column(name = "RELEVANT_BILL_UUID", businessName = "关联单据的数据标识")
    private String relevantBillUuid;
    @Column(name = "CODE", businessName = "编号")
    private String code;
    @Column(name = "PRICE", businessName = "单价")
    private BigDecimal price;
    @Column(name = "DAY", businessName = "日期")
    private LocalDate day;
    @Column(name = "TIME", businessName = "时间")
    private LocalDateTime time;
    @Column(name = "STATE", businessName = "状态")
    private Boolean state;
    @Column(name = "SORT", businessName = "排序码")
    private int sort;
    @Column(name = "TYPE", businessName = "类型", flagBusiness = true)
    private String type;
    @Join(columnName = "TYPE", columnSurname = "TYPE_NAME", businessName = "订单支付方式", joinTable = "TBL_ORDER_TYPE", joinTableSurname = "T2", joinMethod = JoinMethod.LEFT, bridgeColumn = "UUID", destinationColumn = "NAME")
    private String typeName;

    public static Order newInstance(String code, BigDecimal price) {
        Order order = new Order();
        order.setCode(code);
        order.setPrice(price);
        return order;
    }

    public String getRelevantBillUuid() {
        return relevantBillUuid;
    }

    public void setRelevantBillUuid(String relevantBillUuid) {
        this.relevantBillUuid = relevantBillUuid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public Boolean getState() {
        return state;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
