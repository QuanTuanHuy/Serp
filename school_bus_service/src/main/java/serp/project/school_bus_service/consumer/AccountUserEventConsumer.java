package serp.project.school_bus_service.consumer;

import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import serp.project.school_bus_service.dto.message.AccountUserEventMessage;
import serp.project.school_bus_service.dto.request.SchoolBusUserUpsertCommand;
import serp.project.school_bus_service.service.ISchoolBusUserService;

/**
 * Kafka Consumer to listen to Core Account user sync events and update
 * the school_bus_user shadow table.
 */
@Component
@Slf4j
public class AccountUserEventConsumer {

    private final ObjectMapper objectMapper;
    private final ISchoolBusUserService schoolBusUserService;

    public AccountUserEventConsumer(ObjectMapper objectMapper,
                                    ISchoolBusUserService schoolBusUserService) {
        this.objectMapper = objectMapper;
        this.schoolBusUserService = schoolBusUserService;
    }

    @KafkaListener(
            topics = "${school-bus.kafka.topics.account-user-events:SYNC_USER}",
            groupId = "${spring.kafka.consumer.group-id:school-bus-sync-user}"
    )
    public void consume(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key
    ) {
        log.info("Received Kafka sync user event: topic={}, key={}", topic, key);

        AccountUserEventMessage eventMessage;
        try {
            eventMessage = objectMapper.readValue(payload, AccountUserEventMessage.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON user sync event payload. Payload='{}'", payload, e);
            return; // Skip corrupt record gracefully to prevent consumer loop crash
        }

        // 1. Validate mandatory fields
        if (eventMessage.getUserId() == null || eventMessage.getEmail() == null || eventMessage.getEmail().isBlank()) {
            log.warn("Skipped user sync event due to missing required fields (userId/email): userId={}, email={}",
                    eventMessage.getUserId(), eventMessage.getEmail());
            return;
        }

        Long resolvedTenantId = eventMessage.getTenantId() != null ? eventMessage.getTenantId() : eventMessage.getOrganizationId();
        if (resolvedTenantId == null) {
            log.warn("Skipped user sync event because both tenantId and organizationId are missing for userId={}",
                    eventMessage.getUserId());
            return;
        }

        try {
            // 2. Map payload to SchoolBusUserUpsertCommand
            SchoolBusUserUpsertCommand command = new SchoolBusUserUpsertCommand();
            command.setTenantId(resolvedTenantId);
            command.setAccountUserId(eventMessage.getUserId());
            command.setEmail(eventMessage.getEmail());
            command.setFirstName(eventMessage.getFirstName());
            command.setLastName(eventMessage.getLastName());
            command.setPhoneNumber(eventMessage.getPhoneNumber());
            command.setPrimaryOrganizationId(eventMessage.getOrganizationId());
            command.setSyncSource("KAFKA");
            command.setRawPayloadJson(payload);

            // Default fallback values since they are absent in SyncUserEvent
            command.setKeycloakId(null); // KeycloakId is not in the event payload
            command.setAvatarUrl(null); // AvatarUrl is not in the event payload
            command.setPreferredLanguage(null);
            command.setTimezone(null);
            command.setUserType(null);
            command.setStatus("ACTIVE"); // Default status to ACTIVE

            // 3. Invoke Service Upsert logic
            schoolBusUserService.upsertFromAccountUser(command);
            log.info("Successfully synchronized shadow user: accountUserId={}, email={}, tenantId={}",
                    eventMessage.getUserId(), eventMessage.getEmail(), resolvedTenantId);

        } catch (Exception e) {
            log.error("Failed to upsert shadow user into database: accountUserId={}, email={}, tenantId={}",
                    eventMessage.getUserId(), eventMessage.getEmail(), resolvedTenantId, e);
            // Catching all db errors to prevent consumer loop crash
        }
    }

}
