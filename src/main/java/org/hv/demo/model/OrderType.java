package org.hv.demo.model;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.Entity;
import org.hv.pocket.model.BaseEntity;

/**
 * @author wujianchuan
 */
@Entity(table = "TBL_ORDER_TYPE", tableId = 1)
public class OrderType extends BaseEntity {
    private static final long serialVersionUID = -4481356247007947438L;

    @Column(name = "NAME")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
