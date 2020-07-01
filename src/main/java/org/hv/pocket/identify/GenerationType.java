package org.hv.pocket.identify;

/**
 * @author wujianchuan
 */
public interface GenerationType {

    /**
     * yyyyMMdd append six digit serial number as long number.
     */
    String INCREMENT = "increment";
    /**
     * yyyyMMdd append six digit serial number as string.
     */
    String STR_INCREMENT = "strIncrement";
}
