package org.hv.pocket.model;

import java.io.Serializable;
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
    private final List<AbstractEntity> update;
    /**
     * 新增明细
     */
    private final List<AbstractEntity> newborn;
    /**
     * 删除明细
     */
    private final List<AbstractEntity> moribund;
    /**
     * 总行数
     */
    private final int count;

    private DetailInductiveBox(List<AbstractEntity> details, List<AbstractEntity> olderDetails) {
        if (details != null && details.size() > 0) {
            this.newborn = details.parallelStream()
                    .filter(detail -> detail.getIdentify() == null)
                    .collect(Collectors.toList());
        } else {
            this.newborn = new ArrayList<>();
        }
        if (olderDetails != null) {
            if (details != null && details.size() > 0) {
                List<Serializable> newDetailUuidList = details.stream()
                        .map(AbstractEntity::getIdentify)
                        .collect(Collectors.toList());
                this.moribund = olderDetails.parallelStream()
                        .filter(detail -> !newDetailUuidList.contains(detail.getIdentify()))
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
        return new DetailInductiveBox((List<AbstractEntity>) details, (List<AbstractEntity>) olderDetails);
    }

    public List<AbstractEntity> getUpdate() {
        return update;
    }

    public List<AbstractEntity> getNewborn() {
        return newborn;
    }

    public List<AbstractEntity> getMoribund() {
        return moribund;
    }

    public int getCount() {
        return count;
    }
}
