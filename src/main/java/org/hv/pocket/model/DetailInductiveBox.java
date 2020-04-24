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
    private final List<? extends AbstractEntity> update;
    /**
     * 新增明细
     */
    private final List<? extends AbstractEntity> newborn;
    /**
     * 删除明细
     */
    private final List<? extends AbstractEntity> moribund;
    /**
     * 总行数
     */
    private final int count;

    private DetailInductiveBox(List<? extends AbstractEntity> details, List<? extends AbstractEntity> olderDetails) {
        if (details != null && details.size() > 0) {
            this.newborn = details.parallelStream()
                    .filter(detail -> detail.loadIdentify() == null)
                    .collect(Collectors.toList());
        } else {
            this.newborn = new ArrayList<>();
        }
        if (olderDetails != null) {
            if (details != null && details.size() > 0) {
                List<Serializable> newDetailIdentifyList = details.stream()
                        .map(AbstractEntity::loadIdentify)
                        .collect(Collectors.toList());
                this.moribund = olderDetails.parallelStream()
                        .filter(detail -> !newDetailIdentifyList.contains(detail.loadIdentify()))
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

    public static DetailInductiveBox newInstance(List<? extends AbstractEntity> details, List<? extends AbstractEntity> olderDetails) {
        return new DetailInductiveBox(details, olderDetails);
    }

    public List<? extends AbstractEntity> getUpdate() {
        return update;
    }

    public List<? extends AbstractEntity> getNewborn() {
        return newborn;
    }

    public List<? extends AbstractEntity> getMoribund() {
        return moribund;
    }

    public int getCount() {
        return count;
    }
}
