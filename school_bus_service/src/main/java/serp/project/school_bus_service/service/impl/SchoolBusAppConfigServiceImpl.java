package serp.project.school_bus_service.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import serp.project.school_bus_service.entity.SchoolBusAppConfigEntity;
import serp.project.school_bus_service.repository.SchoolBusAppConfigRepository;
import serp.project.school_bus_service.service.ISchoolBusAppConfigService;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class SchoolBusAppConfigServiceImpl implements ISchoolBusAppConfigService {

    private final SchoolBusAppConfigRepository appConfigRepository;

    public SchoolBusAppConfigServiceImpl(SchoolBusAppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    @Override
    public String getString(String configCode, String defaultValue) {
        Optional<SchoolBusAppConfigEntity> config = appConfigRepository
                .findFirstByConfigCodeAndIsActiveTrueAndIsDeletedFalse(configCode);
        return config.map(SchoolBusAppConfigEntity::getConfigValue).orElse(defaultValue);
    }

    @Override
    public Integer getInteger(String configCode, Integer defaultValue) {
        Optional<SchoolBusAppConfigEntity> config = appConfigRepository
                .findFirstByConfigCodeAndIsActiveTrueAndIsDeletedFalse(configCode);
        if (config.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.get().getConfigValue());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse config {} as Integer, value: {}. Using default: {}", 
                    configCode, config.get().getConfigValue(), defaultValue);
            return defaultValue;
        }
    }

    @Override
    public Long getLong(String configCode, Long defaultValue) {
        Optional<SchoolBusAppConfigEntity> config = appConfigRepository
                .findFirstByConfigCodeAndIsActiveTrueAndIsDeletedFalse(configCode);
        if (config.isEmpty()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(config.get().getConfigValue());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse config {} as Long, value: {}. Using default: {}", 
                    configCode, config.get().getConfigValue(), defaultValue);
            return defaultValue;
        }
    }

    @Override
    public BigDecimal getDecimal(String configCode, BigDecimal defaultValue) {
        Optional<SchoolBusAppConfigEntity> config = appConfigRepository
                .findFirstByConfigCodeAndIsActiveTrueAndIsDeletedFalse(configCode);
        if (config.isEmpty()) {
            return defaultValue;
        }
        try {
            return new BigDecimal(config.get().getConfigValue());
        } catch (NumberFormatException | ArithmeticException e) {
            log.warn("Failed to parse config {} as BigDecimal, value: {}. Using default: {}", 
                    configCode, config.get().getConfigValue(), defaultValue);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBoolean(String configCode, Boolean defaultValue) {
        Optional<SchoolBusAppConfigEntity> config = appConfigRepository
                .findFirstByConfigCodeAndIsActiveTrueAndIsDeletedFalse(configCode);
        if (config.isEmpty()) {
            return defaultValue;
        }
        String val = config.get().getConfigValue();
        if ("true".equalsIgnoreCase(val) || "1".equals(val)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(val) || "0".equals(val)) {
            return Boolean.FALSE;
        } else {
            log.warn("Failed to parse config {} as Boolean, value: {}. Using default: {}", 
                    configCode, val, defaultValue);
            return defaultValue;
        }
    }
}
