/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.workitem.command.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import serp.project.pmcore.domain.notification.entity.NotificationEventEntity;
import serp.project.pmcore.domain.notification.entity.NotificationSchemeEntity;
import serp.project.pmcore.domain.notification.entity.NotificationSchemeEntryEntity;
import serp.project.pmcore.domain.notification.port.INotificationEventPort;
import serp.project.pmcore.domain.notification.port.INotificationSchemeEntryPort;
import serp.project.pmcore.domain.notification.port.INotificationSchemePort;
import serp.project.pmcore.domain.notification.service.impl.WorkItemNotificationOutboxPublisher;
import serp.project.pmcore.domain.project.entity.ProjectComponentEntity;
import serp.project.pmcore.domain.project.entity.ProjectEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleActorEntity;
import serp.project.pmcore.domain.project.entity.ProjectRoleEntity;
import serp.project.pmcore.domain.project.service.IProjectRoleActorService;
import serp.project.pmcore.domain.project.service.IProjectRoleService;
import serp.project.pmcore.domain.shared.constant.EventConstants;
import serp.project.pmcore.domain.shared.constant.NotificationKafkaConstants;
import serp.project.pmcore.domain.shared.dto.message.BaseKafkaMessage;
import serp.project.pmcore.domain.shared.dto.message.NotificationCreateRequest;
import serp.project.pmcore.domain.shared.entity.OutboxEventEntity;
import serp.project.pmcore.domain.shared.enums.OutboxEventStatus;
import serp.project.pmcore.domain.shared.service.IOutboxEventService;
import serp.project.pmcore.domain.workitem.entity.WorkItemEntity;
import serp.project.pmcore.domain.workitem.port.read.IWorkItemReadPort;
import serp.project.pmcore.kernel.utils.JsonUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkItemNotificationOutboxPublisherTest {

    private static final Long TENANT_ID = 1L;
    private static final Long ACTOR_ID = 99L;
    private static final Long PROJECT_ID = 10L;
    private static final Long WORK_ITEM_ID = 20L;
    private static final Long SCHEME_ID = 850L;
    private static final Long EVENT_ID = 840L;
    private static final Long SOURCE_EVENT_ID = 7000L;

    @Mock
    private INotificationSchemePort notificationSchemePort;
    @Mock
    private INotificationSchemeEntryPort notificationSchemeEntryPort;
    @Mock
    private INotificationEventPort notificationEventPort;
    @Mock
    private IProjectRoleService projectRoleService;
    @Mock
    private IProjectRoleActorService projectRoleActorService;
    @Mock
    private IWorkItemReadPort workItemReadPort;
    @Mock
    private IOutboxEventService outboxEventService;
    @Mock
    private JsonUtils jsonUtils;

    private WorkItemNotificationOutboxPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new WorkItemNotificationOutboxPublisher(
                notificationSchemePort,
                notificationSchemeEntryPort,
                notificationEventPort,
                projectRoleService,
                projectRoleActorService,
                workItemReadPort,
                outboxEventService,
                jsonUtils
        );
    }

    @Test
    void publishWorkItemCreatedNotificationsShouldResolveUniqueRecipientsAndWriteKafkaEnvelopeOutbox() {
        ProjectEntity project = ProjectEntity.builder()
                .id(PROJECT_ID)
                .tenantId(TENANT_ID)
                .key("SERP")
                .name("SERP Project")
                .leadUserId(11L)
                .notificationSchemeId(SCHEME_ID)
                .build();
        WorkItemEntity workItem = WorkItemEntity.builder()
                .id(WORK_ITEM_ID)
                .tenantId(TENANT_ID)
                .projectId(PROJECT_ID)
                .key("SERP-1")
                .summary("Build notification integration")
                .assigneeId(22L)
                .reporterId(33L)
                .build();

        when(notificationSchemePort.getNotificationSchemeByIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(Optional.of(NotificationSchemeEntity.builder().id(SCHEME_ID).build()));
        when(notificationEventPort.getNotificationEventByEventKeyIncludingSystem(
                NotificationKafkaConstants.WORK_ITEM_CREATED_EVENT_KEY,
                TENANT_ID
        )).thenReturn(Optional.of(NotificationEventEntity.builder()
                .id(EVENT_ID)
                .eventKey(NotificationKafkaConstants.WORK_ITEM_CREATED_EVENT_KEY)
                .build()));
        when(notificationSchemeEntryPort.getNotificationSchemeEntriesBySchemeIdIncludingSystem(SCHEME_ID, TENANT_ID))
                .thenReturn(List.of(
                        entry("PROJECT_LEAD", null),
                        entry("ASSIGNEE", null),
                        entry("REPORTER", null),
                        entry("USER", "44"),
                        entry("COMPONENT_LEAD", null),
                        entry("PROJECT_ROLE", "Developers"),
                        entry("ASSIGNEE", null),
                        entry("WATCHERS", null)
                ));
        when(workItemReadPort.getActiveComponentsByWorkItemId(WORK_ITEM_ID, TENANT_ID))
                .thenReturn(List.of(
                        ProjectComponentEntity.builder().id(501L).leadUserId(66L).build(),
                        ProjectComponentEntity.builder().id(502L).leadUserId(22L).build()
                ));
        when(projectRoleService.getProjectRolesByNameIncludingSystem("Developers", TENANT_ID))
                .thenReturn(List.of(ProjectRoleEntity.builder().id(900L).name("Developers").build()));
        when(projectRoleActorService.getActorsByProjectAndRole(PROJECT_ID, 900L, TENANT_ID))
                .thenReturn(List.of(
                        ProjectRoleActorEntity.builder().subjectType("USER").subjectId("55").build(),
                        ProjectRoleActorEntity.builder().subjectType("GROUP").subjectId("dev-team").build(),
                        ProjectRoleActorEntity.builder().subjectType("USER").subjectId("22").build()
                ));
        when(jsonUtils.toJson(any())).thenReturn("serialized-envelope");
        when(outboxEventService.saveEvent(any())).thenAnswer(invocation -> invocation.getArgument(0));

        publisher.publishWorkItemCreatedNotifications(project, workItem, TENANT_ID, ACTOR_ID, SOURCE_EVENT_ID);

        ArgumentCaptor<OutboxEventEntity> outboxCaptor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventService, times(6)).saveEvent(outboxCaptor.capture());
        List<OutboxEventEntity> outboxEvents = outboxCaptor.getAllValues();
        assertEquals(List.of("11", "22", "33", "44", "66", "55"),
                outboxEvents.stream().map(OutboxEventEntity::getPartitionKey).toList());
        outboxEvents.forEach(event -> {
            assertEquals(NotificationKafkaConstants.TOPIC, event.getTopic());
            assertEquals(NotificationKafkaConstants.EVENT_NOTIFICATION_CREATE_REQUESTED, event.getEventType());
            assertEquals(EventConstants.WorkItem.AGGREGATE, event.getAggregateType());
            assertEquals(WORK_ITEM_ID, event.getAggregateId());
            assertEquals("serialized-envelope", event.getPayload());
            assertEquals(OutboxEventStatus.PENDING, event.getStatus());
        });

        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);
        verify(jsonUtils, times(6)).toJson(messageCaptor.capture());
        @SuppressWarnings("unchecked")
        BaseKafkaMessage<NotificationCreateRequest> firstMessage =
                (BaseKafkaMessage<NotificationCreateRequest>) messageCaptor.getAllValues().getFirst();

        assertNotNull(firstMessage.getMeta().getEventId());
        assertEquals(NotificationKafkaConstants.SOURCE, firstMessage.getMeta().getSource());
        assertEquals(NotificationKafkaConstants.EVENT_NOTIFICATION_CREATE_REQUESTED,
                firstMessage.getMeta().getEventType());
        assertEquals(TENANT_ID, firstMessage.getMeta().getTenantId());
        assertEquals(ACTOR_ID, firstMessage.getMeta().getActorId());
        assertEquals(EventConstants.WorkItem.AGGREGATE, firstMessage.getMeta().getAggregateType());
        assertEquals(String.valueOf(WORK_ITEM_ID), firstMessage.getMeta().getAggregateId());

        NotificationCreateRequest request = firstMessage.getData();
        assertEquals(11L, request.userId());
        assertEquals(TENANT_ID, request.tenantId());
        assertEquals(NotificationKafkaConstants.DEFAULT_CATEGORY, request.category());
        assertEquals(NotificationKafkaConstants.DEFAULT_TYPE, request.type());
        assertEquals(NotificationKafkaConstants.DEFAULT_PRIORITY, request.priority());
        assertEquals(NotificationKafkaConstants.SOURCE, request.sourceService());
        assertEquals(String.valueOf(SOURCE_EVENT_ID), request.sourceEventId());
        assertEquals("WORK_ITEM", request.entityType());
        assertEquals(WORK_ITEM_ID, request.entityId());
        assertEquals(List.of(NotificationKafkaConstants.DEFAULT_DELIVERY_CHANNEL), request.deliveryChannels());
        assertEquals(NotificationKafkaConstants.WORK_ITEM_CREATED_EVENT_KEY,
                request.metadata().get("notificationEventKey"));
        assertEquals(String.valueOf(SOURCE_EVENT_ID), request.metadata().get("sourceEventId"));
    }

    private NotificationSchemeEntryEntity entry(String recipientType, String recipientRef) {
        return NotificationSchemeEntryEntity.builder()
                .schemeId(SCHEME_ID)
                .eventId(EVENT_ID)
                .recipientType(recipientType)
                .recipientRef(recipientRef)
                .isEnabled(true)
                .build();
    }
}
