package org.hunter.pocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hunter.pocket.annotation.Column;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author wujianchuan 2018/12/26
 * 万物得其本者生，万事得其道者成
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = -8735555543925687138L;
    @Column(name = "UUID")
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        Field[] fields = MapperFactory.getRepositoryFields(this.getClass().getName());
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value != null) {
                    hashCode = 31 * hashCode + value.hashCode();
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        return hashCode;
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
                    Object otherValue = field.get(other);
                    Object ownValue = field.get(this);
                    if (ownValue == null) {
                        if (otherValue != null) {
                            return false;
                        }
                    } else {
                        if (ownValue instanceof Number) {
                            if (((Comparable) ownValue).compareTo(otherValue) != 0) {
                                return false;
                            }
                        } else {
                            if (!ownValue.equals(otherValue)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }
}
