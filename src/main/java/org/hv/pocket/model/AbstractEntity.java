package org.hv.pocket.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * @author wujianchuan 2018/12/26
 * Abandon the ship or abandon hope
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class AbstractEntity implements Serializable, Cloneable {
    private static final long serialVersionUID = -8735555543925687138L;

    /**
     * 为避免在每次做查询操作时使用反射为
     * 故子类须重写该方法返回主键
     *
     * @return identify
     */
    public abstract Serializable loadIdentify();

    /**
     * 为避免在每次做持久化操作时使用反射为
     * 故子类须重写该方法为主键属性赋值
     *
     * @param identify 主键
     */
    public abstract void putIdentify(Serializable identify);

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (this.loadIdentify() == null) {
            return hashCode;
        }
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
            AbstractEntity other = (AbstractEntity) obj;
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

    @Override
    public Object clone() {
        Object o;
        Field[] fields = MapperFactory.getRepositoryFields(this.getClass().getName());
        try {
            o = super.clone();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(this);
                if (value instanceof AbstractEntity) {
                    field.set(o, ((AbstractEntity) value).clone());
                }
            }
        } catch (IllegalAccessException | CloneNotSupportedException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        return o;
    }
}
