package org.hunter.pocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hunter.pocket.annotation.Column;

/**
 * @author wujianchuan 2018/12/26
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class BaseEntity implements PocketEntity {
    private static final long serialVersionUID = -8735555543925687138L;
    @Column(name = "UUID")
    private Long uuid;

    public BaseEntity() {
    }

    public Long getUuid() {
        return uuid;
    }

    public void setUuid(Long uuid) {
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
            BaseEntity other = (BaseEntity) obj;
            if (this.uuid == null) {
                return other.uuid == null;
            } else {
                return this.uuid.equals(other.uuid);
            }
        }
    }
}
