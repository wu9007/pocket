package org.hunter.pocket.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wujianchuan
 */
public class DetailInductiveBox {
    private List<BaseEntity> update;
    private List<BaseEntity> newborn;
    private List<BaseEntity> moribund;
    private int count;

    private DetailInductiveBox(List<BaseEntity> newDetails, List<BaseEntity> olderDetails) {
        this.newborn = newDetails.parallelStream()
                .filter(detail -> detail.getUuid() == null)
                .collect(Collectors.toList());
        if (olderDetails != null) {
            List<String> newDetailUuidList = newDetails.stream()
                    .map(BaseEntity::getUuid)
                    .collect(Collectors.toList());
            this.moribund = olderDetails.parallelStream()
                    .filter(detail -> !newDetailUuidList.contains(detail.getUuid()))
                    .collect(Collectors.toList());
            this.update = newDetails.parallelStream()
                    .filter(detail -> {
                        boolean notNewborn = !this.newborn.contains(detail);
                        boolean notMoribund = !this.moribund.contains(detail);
                        boolean notEqual = !olderDetails.contains(detail);
                        return notNewborn && notMoribund && notEqual;
                    })
                    .collect(Collectors.toList());
        } else {
            this.moribund = new ArrayList<>();
            this.update = new ArrayList<>();
        }
        this.count = this.newborn.size() + this.moribund.size() + this.update.size();
    }

    public static DetailInductiveBox newInstance(Object newDetails, Object olderDetails) {
        return new DetailInductiveBox((List<BaseEntity>) newDetails, (List<BaseEntity>) olderDetails);
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
