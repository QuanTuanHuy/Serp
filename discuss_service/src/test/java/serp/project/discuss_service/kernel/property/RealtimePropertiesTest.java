/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Unit tests for realtime properties
 */

package serp.project.discuss_service.kernel.property;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RealtimePropertiesTest {

    @Test
    void shouldBindRealtimeProperties() {
        ConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "discuss.realtime.delivery.max-concurrency", "32",
                "discuss.realtime.typing.debounce-ms", "1500",
                "discuss.realtime.payload-cache-ttl-seconds", "120"
        ));

        RealtimeProperties properties = new Binder(source)
                .bind("discuss.realtime", Bindable.of(RealtimeProperties.class))
                .orElseThrow(() -> new IllegalStateException("Binding failed"));

        assertEquals(32, properties.getDelivery().getMaxConcurrency());
        assertEquals(1500, properties.getTyping().getDebounceMs());
        assertEquals(120, properties.getPayloadCacheTtlSeconds());
    }

    @Test
    void shouldExposeConservativeDefaults() {
        RealtimeProperties properties = new RealtimeProperties();

        assertEquals(64, properties.getDelivery().getMaxConcurrency());
        assertEquals(2000, properties.getTyping().getDebounceMs());
        assertEquals(300, properties.getPayloadCacheTtlSeconds());
    }
}
