/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package io.github.serp.platform.core.context;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "serp.core")
public class SerpCoreProperties {
    private boolean correlationIdEnabled = true;
    private String correlationIdHeader = "X-Correlation-Id";

    public boolean isCorrelationIdEnabled() {
        return correlationIdEnabled;
    }

    public void setCorrelationIdEnabled(boolean correlationIdEnabled) {
        this.correlationIdEnabled = correlationIdEnabled;
    }

    public String getCorrelationIdHeader() {
        return correlationIdHeader;
    }

    public void setCorrelationIdHeader(String correlationIdHeader) {
        this.correlationIdHeader = correlationIdHeader;
    }
}
