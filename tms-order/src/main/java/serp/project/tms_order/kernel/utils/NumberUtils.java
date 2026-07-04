/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.kernel.utils;

public final class NumberUtils {

    private NumberUtils() {
    }

    public static int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    public static long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    public static double safeDouble(Double value) {
        return value == null ? 0D : value;
    }
}
