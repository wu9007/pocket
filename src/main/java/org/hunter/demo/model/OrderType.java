package org.hunter.demo.model;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.model.BaseEntity;

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
