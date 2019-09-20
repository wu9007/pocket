package org.hv.pocket.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wujianchuan
 */
public class DetailInductiveBox {
    /**
     * 更新明细
     */
    private List<BaseEntity> update;
    /**
     * 新增明细
     */
    private List<BaseEntity> newborn;
    /**
     * 删除明细
     */
    private List<BaseEntity> moribund;
    /**
     * 总行数
     */
    private int count;

    private DetailInductiveBox(List<BaseEntity> details, List<BaseEntity> olderDetails) {
        if (details != null && details.size() > 0) {
            this.newborn = details.parallelStream()
                    .filter(detail -> detail.getUuid() == null)
                    .collect(Collectors.toList());
        } else {
            this.newborn = new ArrayList<>();
        }
        if (olderDetails != null) {
            if (details != null && details.size() > 0) {
                List<String> newDetailUuidList = details.stream()
                        .map(BaseEntity::getUuid)
                        .collect(Collectors.toList());
                this.moribund = olderDetails.parallelStream()
                        .filter(detail -> !newDetailUuidList.contains(detail.getUuid()))
                        .collect(Collectors.toList());
                this.update = details.parallelStream()
                        .filter(detail -> {
                            boolean notNewborn = !this.newborn.contains(detail);
                            boolean notMoribund = !this.moribund.contains(detail);
                            boolean notEqual = !olderDetails.contains(detail);
                            return notNewborn && notMoribund && notEqual;
                        })
                        .collect(Collectors.toList());
            } else {
                this.moribund = olderDetails;
                this.update = new ArrayList<>();
            }
        } else {
            this.moribund = new ArrayList<>();
            this.update = new ArrayList<>();
        }
        this.count = this.newborn.size() + this.moribund.size() + this.update.size();
    }

    public static DetailInductiveBox newInstance(Object details, Object olderDetails) {
        return new DetailInductiveBox((List<BaseEntity>) details, (List<BaseEntity>) olderDetails);
    }

    public List<BaseEntity> getUpdate() {
        return update;
    }

    public List<BaseEntity> getNewborn() {
        return newborn;
    }

    public List<BaseEntity> getMoribund() {
        return moribund;
    }

    public int getCount() {
        return count;
    }
}
