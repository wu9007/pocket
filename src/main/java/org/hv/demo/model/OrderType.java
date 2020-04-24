package org.hv.demo.model;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.Entity;
import org.hv.pocket.annotation.Identify;
import org.hv.pocket.model.AbstractEntity;

import java.io.Serializable;

/**
 * @author wujianchuan
 */
@Entity(table = "TBL_ORDER_TYPE", tableId = 1)
public class OrderType extends AbstractEntity {
    private static final long serialVersionUID = -4481356247007947438L;

    @Identify
    @Column(name = "UUID")
    private String uuid;
    @Column(name = "NAME")
    private String name;

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

    @Override
    public Serializable loadIdentify() {
        return this.uuid;
    }

    @Override
    public void putIdentify(Serializable identify) {
        this.uuid = (String) identify;
    }
}
