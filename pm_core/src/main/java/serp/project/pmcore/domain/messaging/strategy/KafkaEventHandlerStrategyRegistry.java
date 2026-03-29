/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.messaging.strategy;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class KafkaEventHandlerStrategyRegistry {

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
