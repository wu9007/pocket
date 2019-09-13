package org.hunter.demo.model;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.constant.JoinMethod;
import org.hunter.pocket.model.BaseEntity;

import java.math.BigDecimal;

/**
 * @author wujianchuan 2019/1/9
 */
@Entity(table = "TBL_RELEVANT_BILL_DETAIL", tableId = 2)
public class RelevantBillDetail extends BaseEntity {
    private static final long serialVersionUID = -6711578420837877371L;
    @Column(name = "NAME")
    private String name;
    @Column(name = "PRICE", businessName = "金额")
    private BigDecimal price;
    @Column(name = "TYPE")
    private String type;
    @Join(columnName = "TYPE", columnSurname = "TYPE_NAME", businessName = "订单支付方式", joinTable = "TBL_ORDER_TYPE", joinTableSurname = "T1", joinMethod = JoinMethod.LEFT, bridgeColumn = "UUID", destinationColumn = "NAME")
    private String typeName;

    @ManyToOne(columnName = "RELEVANT_BILL_UUID", clazz = RelevantBill.class, upBridgeField = "uuid")
    private String relevantBillUuid;

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
}
