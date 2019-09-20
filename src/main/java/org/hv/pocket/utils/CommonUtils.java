package org.hv.pocket.utils;

import java.util.Arrays;

/**
 * @author wujianchuan
 */
public class CommonUtils {
    /**
     * 数组合并
     *
     * @param head 头数组
     * @param tail 尾数组
     * @return 合并后的数组
     */
    public static Object[] combinedArray(Object[] head, Object[] tail) {
        int headLength = head.length;
        int tailLength = tail.length;
        head = Arrays.copyOf(head, headLength + tailLength);
        System.arraycopy(tail, 0, head, headLength, tailLength);
        return head;
    }
}
