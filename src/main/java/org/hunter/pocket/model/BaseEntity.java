package org.hunter.pocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hunter.pocket.annotation.Column;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author wujianchuan 2018/12/26
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = -8735555543925687138L;
    @Column(name = "UUID")
    private String uuid;

    public BaseEntity() {
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
            BaseEntity other = (BaseEntity) obj;
            Field[] fields = MapperFactory.getRepositoryFields(this.getClass().getName());
            try {
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object otherV = field.get(other);
                    Object ownV = field.get(this);
                    if (ownV == null) {
                        if (otherV != null) {
                            return false;
                        }
                    } else {
                        if (ownV instanceof Number) {
                            if (((Comparable) ownV).compareTo(otherV) != 0) {
                                return false;
                            }
                        } else {
                            if (!ownV.equals(otherV)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
