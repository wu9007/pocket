package org.hv.demo.model;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.Entity;
import org.hv.pocket.annotation.Identify;
import org.hv.pocket.annotation.OneToMany;
import org.hv.pocket.constant.EncryptType;
import org.hv.pocket.model.AbstractEntity;

import java.io.Serializable;
import java.util.List;

/**
 * @author wujianchuan
 */
@Entity(table = "TBL_RELEVANT_BILL", businessName = "关联单据")
public class RelevantBill extends AbstractEntity {
    private static final long serialVersionUID = -6343776766066672041L;
    @Identify
    @Column(name = "UUID")
    private String id;
    @Column(name = "CODE", businessName = "编号", encryptMode = EncryptType.DES)
    private String code;
    @Column(name = "AVAILABLE", businessName = "是否可用的")
    private Boolean available;
    @OneToMany(clazz = RelevantBillDetail.class, bridgeField = "relevantBillUuid", businessName = "明细信息")
    private List<RelevantBillDetail> details;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public List<RelevantBillDetail> getDetails() {
        return details;
    }

    public void setDetails(List<RelevantBillDetail> details) {
        this.details = details;
    }

    @Override
    public Serializable loadIdentify() {
        return this.id;
    }

    @Override
    public void putIdentify(Serializable identify) {
        this.id = (String) identify;
    }
}
