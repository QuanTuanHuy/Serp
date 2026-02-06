/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - WebSocket event payload helpers
 */

package serp.project.discuss_service.ui.messaging;

import java.util.Map;

public final class WsEventPayloadUtils {

    private WsEventPayloadUtils() {
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
