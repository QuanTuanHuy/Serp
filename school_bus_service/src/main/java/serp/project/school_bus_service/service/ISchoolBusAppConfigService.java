package serp.project.school_bus_service.service;

import java.math.BigDecimal;

public interface ISchoolBusAppConfigService {
    String getString(String configCode, String defaultValue);
    Integer getInteger(String configCode, Integer defaultValue);
    Long getLong(String configCode, Long defaultValue);
    BigDecimal getDecimal(String configCode, BigDecimal defaultValue);
    Boolean getBoolean(String configCode, Boolean defaultValue);
}
