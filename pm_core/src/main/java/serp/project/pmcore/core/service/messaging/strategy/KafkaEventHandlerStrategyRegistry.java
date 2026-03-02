/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service.messaging.strategy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KafkaEventHandlerStrategyRegistry {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventHandlerStrategyRegistry.class);

    private final List<IKafkaEventHandlerStrategy> strategies;

    private Map<String, IKafkaEventHandlerStrategy> strategyMap = Collections.emptyMap();

    public KafkaEventHandlerStrategyRegistry(List<IKafkaEventHandlerStrategy> strategies) {
        this.strategies = strategies;
    }

    @PostConstruct
    public void init() {
        Map<String, IKafkaEventHandlerStrategy> map = new LinkedHashMap<>();
        for (IKafkaEventHandlerStrategy strategy : strategies) {
            String normalizedEventType = normalize(strategy.getEventType());
            if (normalizedEventType == null) {
                throw new IllegalStateException("Kafka strategy event type must not be blank");
            }
            IKafkaEventHandlerStrategy previous = map.putIfAbsent(normalizedEventType, strategy);
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicated Kafka strategy for event type: " + normalizedEventType);
            }
        }
        this.strategyMap = Collections.unmodifiableMap(map);
        log.info("Kafka strategy registry initialized with {} strategies", this.strategyMap.size());
    }

    public Optional<IKafkaEventHandlerStrategy> findByEventType(String eventType) {
        String normalized = normalize(eventType);
        if (normalized == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(strategyMap.get(normalized));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
