package org.homo.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

/**
 * @author wujianchuan 2018/12/26
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = -8735555543925687138L;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @JsonIgnore
    public String getDescribe() {
        return "uuid: " + this.getUuid();
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
