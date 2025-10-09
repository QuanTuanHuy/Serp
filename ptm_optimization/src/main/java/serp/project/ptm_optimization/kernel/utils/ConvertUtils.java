package serp.project.ptm_optimization.kernel.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ConvertUtils {
    public String convertEnumToString(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : null;
    }

    public String convertEnumToStringWithDefault(Enum<?> enumValue, String defaultValue) {
        return enumValue != null ? enumValue.name() : defaultValue;
    }

    public <E extends Enum<E>> E convertStringToEnum(String value, Class<E> enumClass) {
        if (value == null || enumClass == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert String to Enum: {}", e.getMessage());
            return null;
        }
    }

    public <E extends Enum<E>> E convertStringToEnumWithDefault(String value, Class<E> enumClass, E defaultEnum) {
        E enumValue = convertStringToEnum(value, enumClass);
        return enumValue != null ? enumValue : defaultEnum;
    }

    public <E extends Enum<E>> List<String> convertEnumListToStringList(List<E> enumList) {
        if (enumList == null) {
            return null;
        }
        List<String> stringList = new ArrayList<>();
        for (E enumValue : enumList) {
            stringList.add(convertEnumToString(enumValue));
        }
        return stringList;
    }
}
