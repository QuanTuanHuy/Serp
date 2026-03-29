/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.create.support.handler;

import serp.project.pmcore.application.workitem.command.create.model.CustomFieldResolutionContext;
import serp.project.pmcore.domain.customfield.entity.CustomFieldContextDefaultValueEntity;
import serp.project.pmcore.domain.customfield.entity.CustomFieldOptionEntity;
import serp.project.pmcore.domain.shared.exception.BusinessRuleViolationException;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.workitem.entity.WorkItemCustomFieldValueEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract class AbstractWorkItemCustomFieldValueHandler implements IWorkItemCustomFieldValueHandler {

    protected static final String VALUE_TYPE_TEXT = "TEXT";
    protected static final String VALUE_TYPE_NUMBER = "NUMBER";
    protected static final String VALUE_TYPE_DATE = "DATE";
    protected static final String VALUE_TYPE_DATETIME = "DATETIME";
    protected static final String VALUE_TYPE_USER = "USER";
    protected static final String VALUE_TYPE_GROUP = "GROUP";
    protected static final String VALUE_TYPE_OPTION = "OPTION";
    protected static final String VALUE_TYPE_JSON = "JSON";

    protected WorkItemCustomFieldValueEntity buildCustomFieldValue(CustomFieldResolutionContext context,
                                                                   String valueType,
                                                                   String textValue,
                                                                   BigDecimal numberValue,
                                                                   Long dateValue,
                                                                   Long datetimeValue,
                                                                   Long userValueId,
                                                                   String groupValueId,
                                                                   Long optionValueId,
                                                                   String jsonValue,
                                                                   Integer sortOrder) {
        return WorkItemCustomFieldValueEntity.builder()
                .customFieldId(context.customFieldId())
                .customFieldContextId(context.customFieldContextId())
                .valueType(valueType)
                .textValue(textValue)
                .numberValue(numberValue)
                .dateValue(dateValue)
                .datetimeValue(datetimeValue)
                .userValueId(userValueId)
                .groupValueId(groupValueId)
                .optionValueId(optionValueId)
                .jsonValue(jsonValue)
                .sortOrder(sortOrder)
                .build();
    }

    protected CustomFieldContextDefaultValueEntity requireSingleDefaultValue(List<CustomFieldContextDefaultValueEntity> defaultValues,
                                                                             String fieldKey) {
        if (defaultValues == null || defaultValues.isEmpty()) {
            return null;
        }

        if (defaultValues.size() > 1) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Multiple default values are configured for single-value field=" + fieldKey
            );
        }

        return defaultValues.getFirst();
    }

    protected String requireTextValue(Object rawValue, String fieldKey) {
        if (rawValue instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires text value: field=" + fieldKey
        );
    }

    protected BigDecimal requireNumberValue(Object rawValue, String fieldKey) {
        try {
            if (rawValue instanceof Number number) {
                return new BigDecimal(number.toString());
            }
            if (rawValue instanceof String text && !text.isBlank()) {
                return new BigDecimal(text.trim());
            }
        } catch (NumberFormatException exception) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Custom field requires numeric value: field=" + fieldKey
            );
        }

        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires numeric value: field=" + fieldKey
        );
    }

    protected Long requireUserValue(Object rawValue, String fieldKey) {
        try {
            if (rawValue instanceof Number number) {
                return number.longValue();
            }
            if (rawValue instanceof String text && !text.isBlank()) {
                return Long.parseLong(text.trim());
            }
        } catch (NumberFormatException exception) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Custom field requires user id value: field=" + fieldKey
            );
        }

        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires user id value: field=" + fieldKey
        );
    }

    protected String requireGroupValue(Object rawValue, String fieldKey) {
        if (rawValue instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires group value: field=" + fieldKey
        );
    }

    protected Long requireDateValue(Object rawValue, String fieldKey) {
        try {
            LocalDate localDate;
            if (rawValue instanceof Number number) {
                localDate = Instant.ofEpochMilli(number.longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
            } else if (rawValue instanceof String text && !text.isBlank()) {
                try {
                    localDate = LocalDate.parse(text.trim());
                } catch (DateTimeParseException exception) {
                    localDate = Instant.parse(text.trim()).atZone(ZoneId.systemDefault()).toLocalDate();
                }
            } else {
                throw new DateTimeParseException("Unsupported date format", String.valueOf(rawValue), 0);
            }
            return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException exception) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Custom field requires date value: field=" + fieldKey
            );
        }
    }

    protected Long requireDateTimeValue(Object rawValue, String fieldKey) {
        try {
            if (rawValue instanceof Number number) {
                return number.longValue();
            }
            if (rawValue instanceof String text && !text.isBlank()) {
                try {
                    return Instant.parse(text.trim()).toEpochMilli();
                } catch (DateTimeParseException exception) {
                    return LocalDateTime.parse(text.trim())
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli();
                }
            }
        } catch (DateTimeParseException exception) {
            throw new BusinessRuleViolationException(
                    DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                    "Custom field requires datetime value: field=" + fieldKey
            );
        }

        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field requires datetime value: field=" + fieldKey
        );
    }

    protected List<?> asList(Object rawValue) {
        if (rawValue == null) {
            return List.of();
        }
        if (rawValue instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        return List.of(rawValue);
    }

    protected CustomFieldOptionEntity resolveOption(Object rawValue,
                                                    List<CustomFieldOptionEntity> options,
                                                    String fieldKey) {
        String textCandidate = rawValue instanceof String text ? text.trim() : null;
        Long numericCandidate = null;

        if (rawValue instanceof Number number) {
            numericCandidate = number.longValue();
        } else if (textCandidate != null && !textCandidate.isBlank()) {
            try {
                numericCandidate = Long.parseLong(textCandidate);
            } catch (NumberFormatException ignored) {
                numericCandidate = null;
            }
        }

        for (CustomFieldOptionEntity option : options) {
            if (Boolean.TRUE.equals(option.getIsDisabled())) {
                continue;
            }
            if (textCandidate != null && !textCandidate.isBlank() && textCandidate.equals(option.getOptionKey())) {
                return option;
            }
            if (numericCandidate != null && numericCandidate.equals(option.getId())) {
                return option;
            }
        }

        throw new BusinessRuleViolationException(
                DomainErrorCode.CUSTOM_FIELD_VALUE_INVALID,
                "Custom field option is invalid: field=" + fieldKey + ", value=" + rawValue
        );
    }
}
