package org.hv.demo.model;

import org.hv.pocket.annotation.Column;
import org.hv.pocket.annotation.Entity;
import org.hv.pocket.model.BaseEntity;

import java.time.LocalDate;

/**
 * @author leyan95 2019/3/23
 * @version 1.0
 */
@Entity(table = "T_HISTORY", businessName = "历史")
public class History extends BaseEntity {
    private static final long serialVersionUID = 1783568186627352468L;

    @Column(name = "OPERATE")
    private String operate;
    @Column(name = "OPERATE_TIME")
    private LocalDate operateTime;
    @Column(name = "OPERATOR")
    private String operator;
    @Column(name = "BUSINESS_UUID")
    private String businessUuid;
    @Column(name = "OPERATE_CONTENT")
    private String operateContent;

    public History() {
    }

    public History(String operate, LocalDate operateTime, String operator, String businessUuid, String operateContent) {
        this.operate = operate;
        this.operateTime = operateTime;
        this.operator = operator;
        this.businessUuid = businessUuid;
        this.operateContent = operateContent;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public LocalDate getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(LocalDate operateTime) {
        this.operateTime = operateTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getBusinessUuid() {
        return businessUuid;
    }

    public void setBusinessUuid(String businessUuid) {
        this.businessUuid = businessUuid;
    }

    public String getOperateContent() {
        return operateContent;
    }

    public void setOperateContent(String operateContent) {
        this.operateContent = operateContent;
    }
}
