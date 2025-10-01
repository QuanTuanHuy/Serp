/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import serp.project.crm.core.domain.entity.AddressEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

public abstract class BaseMapper {

    protected static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    protected Long toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    protected LocalDateTime toLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    protected <E extends Enum<E>> String enumToString(E enumValue) {
        return enumValue != null ? enumValue.name() : null;
    }

    protected <E extends Enum<E>> E stringToEnum(String value, Class<E> enumType) {
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    protected AddressEntity buildAddress(String street, String city, String state, String zipCode, String country) {
        if (street == null && city == null && state == null && zipCode == null && country == null) {
            return null;
        }
        return AddressEntity.builder()
                .street(street)
                .city(city)
                .state(state)
                .zipCode(zipCode)
                .country(country)
                .build();
    }

    protected List<String> parseJsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return JSON_MAPPER.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    protected String serializeListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return JSON_MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            return null;
        }
    }
}
