package org.hunter.pocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hunter.pocket.annotation.Column;

/**
 * @author wujianchuan 2019/2/22
 * @deprecated 未来版本将废弃使用
 */
@Deprecated
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class AbstractEntity implements PocketEntity {

    @Column(name = "UUID")
    private String uuid;

    public AbstractEntity() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            AbstractEntity other = (AbstractEntity) obj;
            if (this.uuid == null) {
                return other.uuid == null;
            } else {
                return this.uuid.equals(other.uuid);
            }
        }
    }
}
