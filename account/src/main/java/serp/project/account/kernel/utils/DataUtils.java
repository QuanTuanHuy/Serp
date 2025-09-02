/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.utils;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DataUtils {
    public boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public boolean isNullOrEmpty(Object obj) {
        return Objects.isNull(obj);
    }
}
