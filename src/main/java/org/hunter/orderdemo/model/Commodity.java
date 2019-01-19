package org.hunter.orderdemo.model;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.model.BaseEntity;

import java.math.BigDecimal;

/**
 * @author wujianchuan 2019/1/9
 */
@Entity(table = "TBL_COMMODITY")
public class Commodity extends BaseEntity {
    private static final long serialVersionUID = -6711578420837877371L;
    @Column(name = "NAME")
    private String name;
    @Column(name = "PRICE")
    private BigDecimal price;

    @ManyToOne(name = "ORDER_UUID")
    private Long order;

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

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }
}
