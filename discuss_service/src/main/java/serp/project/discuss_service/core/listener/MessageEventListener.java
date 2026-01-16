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
import serp.project.discuss_service.core.domain.event.MessageSentInternalEvent;
import serp.project.discuss_service.core.domain.event.MessageUpdatedInternalEvent;
import serp.project.discuss_service.core.domain.event.ReactionAddedInternalEvent;
import serp.project.discuss_service.core.domain.event.ReactionRemovedInternalEvent;
import serp.project.discuss_service.core.service.IDiscussCacheService;
import serp.project.discuss_service.core.service.IDiscussEventPublisher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Listens for internal Spring events and performs post-commit operations ASYNCHRONOUSLY.
 * 
 * Key benefits of using @TransactionalEventListener with AFTER_COMMIT:
 * 1. Kafka events are only published if the DB transaction succeeds
 * 2. Prevents orphan events from failed/rolled-back transactions
 * 3. Decouples transactional logic from external system notifications
 * 4. Cache operations happen after data is persisted
 */
@Component
@Slf4j
public class MessageEventListener {

    private static final int DEFAULT_PAGE_SIZE = 50;

    private final IDiscussEventPublisher eventPublisher;
    private final IDiscussCacheService cacheService;
    private final ExecutorService messageAsyncExecutor;

    public MessageEventListener(
            IDiscussEventPublisher eventPublisher,
            IDiscussCacheService cacheService,
            @Qualifier("messageAsyncExecutor") ExecutorService messageAsyncExecutor) {
        this.eventPublisher = eventPublisher;
        this.cacheService = cacheService;
        this.messageAsyncExecutor = messageAsyncExecutor;
    }

    /**
     * Handle message sent event AFTER transaction commits.
     * Runs async: publishes Kafka event and updates cache (smart prepend).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageSent(MessageSentInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for message sent: messageId={}, channelId={}", 
                        event.getMessage().getId(), event.getChannelId());

                eventPublisher.publishMessageSent(event.getMessage());

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

    /**
     * Handle message updated event AFTER transaction commits.
     * Runs async: publishes Kafka event and invalidates cache.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageUpdated(MessageUpdatedInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for message updated: messageId={}", 
                        event.getMessage().getId());

                eventPublisher.publishMessageUpdated(event.getMessage());
                
                cacheService.invalidateChannelMessagesPageAsync(event.getChannelId());

                log.debug("Post-commit completed for message update {}", event.getMessage().getId());
            } catch (Exception e) {
                log.error("Failed to process post-commit for message update {}: {}", 
                        event.getMessage().getId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }

    /**
     * Handle message deleted event AFTER transaction commits.
     * Runs async: publishes Kafka event and removes from cache (smart removal).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageDeleted(MessageDeletedInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for message deleted: messageId={}", 
                        event.getMessage().getId());

                eventPublisher.publishMessageDeleted(event.getMessage());
                
                cacheService.removeMessageFromFirstPage(event.getChannelId(), event.getMessage().getId());

                log.debug("Post-commit completed for message deletion {}", event.getMessage().getId());
            } catch (Exception e) {
                log.error("Failed to process post-commit for message deletion {}: {}", 
                        event.getMessage().getId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }

    /**
     * Handle reaction added event AFTER transaction commits.
     * Runs async: publishes Kafka event only (reactions don't affect message list cache).
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReactionAdded(ReactionAddedInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for reaction added: messageId={}, emoji={}", 
                        event.getMessageId(), event.getEmoji());

                eventPublisher.publishReactionAdded(
                        event.getMessageId(), 
                        event.getChannelId(), 
                        event.getUserId(), 
                        event.getEmoji());

                log.debug("Post-commit completed for reaction added on message {}", event.getMessageId());
            } catch (Exception e) {
                log.error("Failed to process post-commit for reaction added on message {}: {}", 
                        event.getMessageId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }

    /**
     * Handle reaction removed event AFTER transaction commits.
     * Runs async: publishes Kafka event only.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReactionRemoved(ReactionRemovedInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing post-commit for reaction removed: messageId={}, emoji={}", 
                        event.getMessageId(), event.getEmoji());

                eventPublisher.publishReactionRemoved(
                        event.getMessageId(), 
                        event.getChannelId(), 
                        event.getUserId(), 
                        event.getEmoji());

                log.debug("Post-commit completed for reaction removed on message {}", event.getMessageId());
            } catch (Exception e) {
                log.error("Failed to process post-commit for reaction removed on message {}: {}", 
                        event.getMessageId(), e.getMessage(), e);
            }
        }, messageAsyncExecutor);
    }
}
