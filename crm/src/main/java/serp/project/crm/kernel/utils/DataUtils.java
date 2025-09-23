/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.kernel.utils;

import java.util.Objects;

public class DataUtils {
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrEmpty(Object obj) {
        return Objects.isNull(obj);
    }
}
