/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.utils;

import org.springframework.lang.Nullable;

import java.util.Collection;

public class CollectionUtils {
    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
