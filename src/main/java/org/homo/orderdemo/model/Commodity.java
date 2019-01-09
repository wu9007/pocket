package org.homo.orderdemo.model;

import org.homo.core.annotation.Column;
import org.homo.core.annotation.Entity;
import org.homo.core.model.BaseEntity;

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
}
