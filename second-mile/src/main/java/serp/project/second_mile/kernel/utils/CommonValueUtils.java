/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.kernel.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class CommonValueUtils {
    private static final double GRAMS_PER_KILOGRAM = 1000.0;

    private CommonValueUtils() {
    }

    public static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue.toUpperCase(Locale.ROOT);
    }

    public static List<String> normalizeCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String code : codes) {
            String normalizedCode = normalizeText(code);
            if (normalizedCode != null) {
                normalized.add(normalizedCode);
            }
        }
        return new ArrayList<>(normalized);
    }

    public static String idempotencyKey(Object... parts) {
        return Arrays.stream(parts)
                .filter(Objects::nonNull)
                .map(part -> part.toString().trim())
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(":"));
    }

    public static double safeDouble(Double value) {
        return value == null || value < 0 ? 0.0 : value;
    }

    public static int safeInt(Integer value) {
        return value == null || value < 0 ? 0 : value;
    }

    public static double positiveOrDefault(Double value, double fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }

    public static int positiveOrDefault(Integer value, int fallback) {
        if (value == null || value <= 0) {
            return fallback;
        }
        return value;
    }

    public static double nullSafeDouble(Double value) {
        return value == null ? Double.MAX_VALUE : value;
    }

    public static double gramsToKilograms(double grams) {
        return grams / GRAMS_PER_KILOGRAM;
    }
}
