package org.hv.demo.model;

import org.hv.pocket.annotation.*;
import org.hv.pocket.constant.JoinMethod;
import org.hv.pocket.model.AbstractEntity;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author wujianchuan 2019/1/9
 */
@Entity(table = "TBL_RELEVANT_BILL_DETAIL")
public class RelevantBillDetail extends AbstractEntity {
    private static final long serialVersionUID = -6711578420837877371L;
    @Identify
    @Column(name = "UUID")
    private String uuid;
    @Column(name = "NAME", ignoreCompare = true)
    private String name;
    @Column(name = "PRICE", businessName = "金额")
    private BigDecimal price;
    @Column(name = "TYPE")
    private String type;
    @Join(columnName = "TYPE", columnSurname = "TYPE_NAME", businessName = "订单支付方式", joinTable = "TBL_ORDER_TYPE", joinTableSurname = "T1", joinMethod = JoinMethod.LEFT, bridgeColumn = "UUID", destinationColumn = "NAME")
    private String typeName;

    @ManyToOne(clazz = RelevantBill.class, upBridgeField = "id")
    private String relevantBillUuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getRelevantBillUuid() {
        return relevantBillUuid;
    }

    public void setRelevantBillUuid(String relevantBillUuid) {
        this.relevantBillUuid = relevantBillUuid;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Serializable loadIdentify() {
        return this.uuid;
    }

    @Override
    public void putIdentify(Serializable identify) {
        this.uuid = (String) identify;
    }
}
