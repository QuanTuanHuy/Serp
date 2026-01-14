/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Base mapper with timestamp conversion utilities
 */

package serp.project.discuss_service.infrastructure.store.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class BaseMapper {
    
    protected Long localDateTimeToLong(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    protected LocalDateTime longToLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}
