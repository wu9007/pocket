package org.hv.pocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.Identify;

import java.io.Serializable;

/**
 * @author wujianchuan 2018/12/26
 * cun qi jue jiang, er qu qi fen ji.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseEntity extends AbstractEntity {
    private static final long serialVersionUID = -8735555543925687138L;
    @Identify
    @Column(name = "UUID")
    private String uuid;

    @Override
    public Serializable loadIdentify() {
        return this.getUuid();
    }

    @Override
    public void putIdentify(Serializable identify) {
        this.setUuid((String) identify);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
