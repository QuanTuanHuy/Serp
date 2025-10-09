/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.mapper;

import java.lang.reflect.Method;
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

    public String convertEnumToDisplayString(Enum<?> enumValue, String methodName) {
        if (enumValue == null || methodName == null) {
            return null;
        }
        try {
            Method method = enumValue.getClass().getMethod(methodName);
            Object result = method.invoke(enumValue);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public <E extends Enum<E>> E convertStringToEnum(String value, Class<E> enumClass) {
        if (value == null || enumClass == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}