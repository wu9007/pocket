package org.hv.demo.model;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.Entity;
import org.hv.pocket.annotation.Join;
import org.hv.pocket.annotation.OneToOne;
import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.constant.JoinMethod;
import org.hv.pocket.model.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author wujianchuan 2018/12/26
 */
@Entity(table = "TBL_ORDER", businessName = "单据")
public class Order extends BaseEntity {
    private static final long serialVersionUID = 2560385391551524826L;

    @Column(businessName = "关联单据的数据标识")
    private String relevantBillUuid;
    @Column(businessName = "编号", encryptMode = EncryptType.DES)
    private String code;
    @Column(businessName = "单价")
    private BigDecimal price;
    @Column(businessName = "日期")
    private LocalDate day;
    @Column(businessName = "时间")
    private LocalDateTime time;
    @Column(businessName = "状态")
    private Boolean state;
    @Column(businessName = "排序码")
    private int sort;
    @Column(businessName = "类型", flagBusiness = true)
    private String type;
    @OneToOne(ownField = "type", relatedField = "uuid")
    private OrderType orderType;
    @Join(columnName = "TYPE", columnSurname = "TYPE_NAME", businessName = "订单支付方式", joinTable = "TBL_ORDER_TYPE", joinTableSurname = "T2", joinMethod = JoinMethod.LEFT, bridgeColumn = "UUID", destinationColumn = "NAME", encryptMode = EncryptType.DES)
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

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }
}
