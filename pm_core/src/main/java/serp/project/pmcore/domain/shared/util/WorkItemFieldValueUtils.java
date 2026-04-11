/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.shared.util;

public final class WorkItemFieldValueUtils {

    private WorkItemFieldValueUtils() {
    }

    public static String asNullableString(Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof String text) {
            return text;
        }
        throw new IllegalArgumentException("Expected string value");
    }

    public static Long asNullablePositiveLong(Object rawValue) {
        Long value = asNullableLong(rawValue);
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("Expected a positive number");
        }
        return value;
    }

    public static Long asNullableNonNegativeLong(Object rawValue) {
        Long value = asNullableLong(rawValue);
        if (value != null && value < 0) {
            throw new IllegalArgumentException("Expected a non-negative number");
        }
        return value;
    }

    public static Long asNullableLong(Object rawValue) {
        switch (rawValue) {
            case null -> {
                return null;
            }
            case Number number -> {
                return number.longValue();
            }
            case String text -> {
                if (text.isBlank()) {
                    return null;
                }
                return Long.valueOf(text.trim());
            }
            default -> throw new IllegalArgumentException(
                    "Expected long-compatible value but got: " + rawValue.getClass().getName());
        }
    }
}
