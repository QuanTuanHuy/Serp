/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Kafka payload utility functions
 */

package serp.project.discuss_service.kernel.utils;

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
}
