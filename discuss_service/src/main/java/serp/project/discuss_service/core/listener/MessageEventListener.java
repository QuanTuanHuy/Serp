/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Transactional event listener for message events
 */

package serp.project.discuss_service.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import serp.project.discuss_service.core.domain.event.MessageDeletedInternalEvent;
import serp.project.discuss_service.core.domain.event.MessageReadInternalEvent;
import serp.project.discuss_service.core.domain.event.MessageSentInternalEvent;
import serp.project.discuss_service.core.domain.event.MessageUpdatedInternalEvent;
import serp.project.discuss_service.core.domain.event.ReactionAddedInternalEvent;
import serp.project.discuss_service.core.domain.event.ReactionRemovedInternalEvent;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;
import serp.project.discuss_service.core.service.impl.RealtimePayloadBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class MessageEventListener {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final IDiscussEventPublisher eventPublisher;
    private final IDiscussCacheService cacheService;
    private final RealtimePayloadBuilder realtimePayloadBuilder;
    private final ExecutorService messageAsyncExecutor;

    public MessageEventListener(
            IDiscussEventPublisher eventPublisher,
            IDiscussCacheService cacheService,
            RealtimePayloadBuilder realtimePayloadBuilder,
            @Qualifier("messageAsyncExecutor") ExecutorService messageAsyncExecutor) {
        this.eventPublisher = eventPublisher;
        this.cacheService = cacheService;
        this.realtimePayloadBuilder = realtimePayloadBuilder;
        this.messageAsyncExecutor = messageAsyncExecutor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSent(MessageSentInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for message sent: messageId={}, channelId={}", 
                        event.getMessage().getId(), event.getChannelId());

                eventPublisher.publishRealtimeEvent(
                        String.valueOf(event.getChannelId()),
                        realtimePayloadBuilder.buildMessageNew(event.getMessage()),
                        IDiscussEventPublisher.TOPIC_MESSAGE_EVENTS
                );

                cacheService.cacheMessage(event.getMessage());
                if (event.getMessage().getParentId() != null) {
                    cacheService.invalidateMessage(event.getMessage().getParentId());
                }
                boolean smartUpdated = cacheService.prependMessageToFirstPage(
                        event.getChannelId(), 
                        event.getMessage(), 
                        DEFAULT_PAGE_SIZE);

                if (smartUpdated) {
                    log.debug("Smart cache update succeeded for message {}", event.getMessage().getId());
                } else {
                    log.debug("Cache invalidated (no existing cache) for message {}", event.getMessage().getId());
                }

            } catch (Exception e) {
                log.error("Failed to process post-commit for message {}: {}", 
                        event.getMessage().getId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageUpdated(MessageUpdatedInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for message updated: messageId={}", 
                        event.getMessage().getId());

                eventPublisher.publishRealtimeEvent(
                        String.valueOf(event.getChannelId()),
                        realtimePayloadBuilder.buildMessageUpdated(event.getMessage()),
                        IDiscussEventPublisher.TOPIC_MESSAGE_EVENTS
                );

                cacheService.cacheMessage(event.getMessage());
                cacheService.invalidateChannelMessagesPageAsync(event.getChannelId());

                log.debug("Post-commit completed for message update {}", event.getMessage().getId());
            } catch (Exception e) {
                log.error("Failed to process post-commit for message update {}: {}", 
                        event.getMessage().getId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageDeleted(MessageDeletedInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for message deleted: messageId={}", 
                        event.getMessage().getId());

                eventPublisher.publishRealtimeEvent(
                        String.valueOf(event.getChannelId()),
                        realtimePayloadBuilder.buildMessageDeleted(event.getMessage()),
                        IDiscussEventPublisher.TOPIC_MESSAGE_EVENTS
                );
                
                cacheService.invalidateMessage(event.getMessage().getId());
                
                if (event.getParentId() != null) {
                    cacheService.invalidateMessage(event.getParentId());
                }
                
                cacheService.removeMessageFromFirstPage(event.getChannelId(), event.getMessage().getId());

                log.debug("Post-commit completed for message deletion {}", event.getMessage().getId());
            } catch (Exception e) {
                log.error("Failed to process post-commit for message deletion {}: {}", 
                        event.getMessage().getId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageRead(MessageReadInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for message read: messageId={}, userId={}",
                        event.getMessageId(), event.getUserId());

                eventPublisher.publishRealtimeEvent(
                        String.valueOf(event.getChannelId()),
                        realtimePayloadBuilder.buildMessageRead(
                                event.getChannelId(),
                                event.getMessageId(),
                                event.getUserId(),
                                event.getReadBy(),
                                event.getReadCount()
                        ),
                        IDiscussEventPublisher.TOPIC_MESSAGE_EVENTS
                );

                cacheService.invalidateMessage(event.getMessageId());
                cacheService.invalidateChannelMessagesPageAsync(event.getChannelId());
            } catch (Exception e) {
                log.error("Failed to process post-commit for message read {}: {}",
                        event.getMessageId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReactionAdded(ReactionAddedInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for reaction added: messageId={}, emoji={}", 
                        event.getMessageId(), event.getEmoji());

                eventPublisher.publishRealtimeEvent(
                        String.valueOf(event.getChannelId()),
                        realtimePayloadBuilder.buildReaction(
                                event.getChannelId(),
                                event.getMessageId(),
                                event.getUserId(),
                                event.getEmoji(),
                                true
                        ),
                        IDiscussEventPublisher.TOPIC_REACTION_EVENTS
                );

                cacheService.invalidateMessage(event.getMessageId());
                cacheService.invalidateChannelMessagesPageAsync(event.getChannelId());

                log.debug("Post-commit completed for reaction added on message {}", event.getMessageId());
            } catch (Exception e) {
                log.error("Failed to process post-commit for reaction added on message {}: {}", 
                        event.getMessageId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReactionRemoved(ReactionRemovedInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for reaction removed: messageId={}, emoji={}", 
                        event.getMessageId(), event.getEmoji());

                eventPublisher.publishRealtimeEvent(
                        String.valueOf(event.getChannelId()),
                        realtimePayloadBuilder.buildReaction(
                                event.getChannelId(),
                                event.getMessageId(),
                                event.getUserId(),
                                event.getEmoji(),
                                false
                        ),
                        IDiscussEventPublisher.TOPIC_REACTION_EVENTS
                );

                cacheService.invalidateMessage(event.getMessageId());
                cacheService.invalidateChannelMessagesPageAsync(event.getChannelId());

                log.debug("Post-commit completed for reaction removed on message {}", event.getMessageId());
            } catch (Exception e) {
                log.error("Failed to process post-commit for reaction removed on message {}: {}", 
                        event.getMessageId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }
}
