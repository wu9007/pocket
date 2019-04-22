package org.hunter.pocket.constant;

import org.hunter.pocket.annotation.Column;
import org.hunter.pocket.annotation.Join;
import org.hunter.pocket.annotation.ManyToOne;
import org.hunter.pocket.annotation.OneToMany;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * @author wujianchuan
 */
public class StreamPredicates {
    private static final String SERIAL_VERSION_UID = "serialVersionUID";
    public static final Predicate<Field> COLUMN_MAPPING_PREDICATE =
            field -> !SERIAL_VERSION_UID.equals(field.getName())
                    &&
                    ((field.getAnnotation(Column.class) != null
                            || field.getAnnotation(ManyToOne.class) != null
                            || field.getAnnotation(Join.class) != null
                    ));
    public static final Predicate<Field> CHILDREN_MAPPING_PREDICATE =
            field -> !SERIAL_VERSION_UID.equals(field.getName())
                    && field.getAnnotation(OneToMany.class) != null;
}
