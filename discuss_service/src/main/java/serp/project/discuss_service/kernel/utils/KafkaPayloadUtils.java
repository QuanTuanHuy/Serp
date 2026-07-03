/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka payload utility functions
 */

package serp.project.discuss_service.kernel.utils;

import java.util.List;
import java.util.Map;

public final class KafkaPayloadUtils {

    private KafkaPayloadUtils() {
    }

    public static Long getLong(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object val = map.get(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public static String getString(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object val = map.get(key);
        if (val instanceof String) {
            return (String) val;
        }
        if (val != null) {
            return val.toString();
        }
        return null;
    }

    public static Integer getInteger(Map<String, Object> map, String key) {
        Long value = getLong(map, key);
        return value == null ? null : value.intValue();
    }

    public static List<Long> getLongList(Map<String, Object> map, String key) {
        if (map == null) {
            return List.of();
        }
        Object value = map.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(item -> item instanceof Number)
                .map(item -> ((Number) item).longValue())
                .toList();
    }
}
