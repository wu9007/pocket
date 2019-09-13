package org.hunter.demo.model;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Entity;
import org.hunter.pocket.annotation.OneToMany;
import org.hunter.pocket.model.BaseEntity;

import java.util.List;

/**
 * @author wujianchuan
 */
@Entity(table = "TBL_RELEVANT_BILL", tableId = 3, businessName = "关联单据")
public class RelevantBill extends BaseEntity {
    private static final long serialVersionUID = -6343776766066672041L;
    @Column(name = "CODE", businessName = "编号")
    private String code;
    @Column(name = "AVAILABLE", businessName = "是否可用的")
    private Boolean available;
    @OneToMany(clazz = RelevantBillDetail.class, bridgeField = "relevantBillUuid", businessName = "明细信息")
    private List<RelevantBillDetail> details;

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
}
