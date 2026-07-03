/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Bounded realtime delivery service
 */

package serp.project.discuss_service.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import serp.project.discuss_service.core.port.client.IWebSocketHubPort;
import serp.project.discuss_service.core.service.IChannelMemberService;
import serp.project.discuss_service.core.service.IPresenceService;
import serp.project.discuss_service.core.service.IRealtimeDeliveryService;
import serp.project.discuss_service.kernel.property.RealtimeProperties;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RealtimeDeliveryService implements IRealtimeDeliveryService {

    private final IWebSocketHubPort webSocketHub;
    private final IChannelMemberService memberService;
    private final IPresenceService presenceService;
    private final RealtimeProperties realtimeProperties;
    private final ExecutorService messageAsyncExecutor;

    public RealtimeDeliveryService(
            IWebSocketHubPort webSocketHub,
            IChannelMemberService memberService,
            IPresenceService presenceService,
            RealtimeProperties realtimeProperties,
            @Qualifier("messageAsyncExecutor") ExecutorService messageAsyncExecutor) {
        this.webSocketHub = webSocketHub;
        this.memberService = memberService;
        this.presenceService = presenceService;
        this.realtimeProperties = realtimeProperties;
        this.messageAsyncExecutor = messageAsyncExecutor;
    }

    @Override
    public void deliverToUser(Long userId, Object payload) {
        if (userId == null || payload == null) {
            return;
        }
        deliverToUsers(Set.of(userId), payload);
    }

    @Override
    public void deliverToUsers(Set<Long> userIds, Object payload) {
        if (userIds == null || userIds.isEmpty() || payload == null) {
            return;
        }

        Set<Long> distinctUserIds = userIds.stream()
                .filter(userId -> userId != null)
                .collect(Collectors.toSet());
        if (distinctUserIds.isEmpty()) {
            return;
        }

        long startedAt = System.nanoTime();
        Semaphore semaphore = new Semaphore(resolveMaxConcurrency());
        CompletableFuture<?>[] futures = distinctUserIds.stream()
                .map(userId -> CompletableFuture.runAsync(
                        () -> sendWithPermit(semaphore, userId, payload),
                        messageAsyncExecutor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.debug("Delivered realtime event to {} users in {} ms", distinctUserIds.size(), durationMs);
    }

    @Override
    public void deliverToChannel(Long channelId, Object payload) {
        if (channelId == null || payload == null) {
            return;
        }
        Set<Long> memberIds = memberService.getMemberIds(channelId);
        Set<Long> onlineUserIds = presenceService.getOnlineUsers(memberIds);
        deliverToUsers(onlineUserIds, payload);
    }

    @Override
    public void deliverToChannelExcept(Long channelId, Long excludedUserId, Object payload) {
        if (channelId == null || excludedUserId == null || payload == null) {
            return;
        }
        Set<Long> memberIds = memberService.getMemberIds(channelId).stream()
                .filter(memberId -> !excludedUserId.equals(memberId))
                .collect(Collectors.toSet());
        Set<Long> onlineUserIds = presenceService.getOnlineUsers(memberIds);
        deliverToUsers(onlineUserIds, payload);
    }

    @Override
    public void deliverPresenceChange(Long changedUserId, Object payload) {
        if (changedUserId == null || payload == null) {
            return;
        }

        Set<Long> recipientIds = memberService.getUserChannels(changedUserId).stream()
                .map(member -> member.getChannelId())
                .filter(channelId -> channelId != null)
                .flatMap(channelId -> memberService.getMemberIds(channelId).stream())
                .filter(memberId -> !changedUserId.equals(memberId))
                .collect(Collectors.toCollection(HashSet::new));

        Set<Long> onlineRecipientIds = presenceService.getOnlineUsers(recipientIds);
        deliverToUsers(onlineRecipientIds, payload);
    }

    private void sendWithPermit(Semaphore semaphore, Long userId, Object payload) {
        boolean acquired = false;
        try {
            semaphore.acquire();
            acquired = true;
            webSocketHub.sendToUser(userId, payload);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Realtime delivery interrupted for user {}", userId);
        } catch (Exception e) {
            log.warn("Realtime delivery failed for user {}: {}", userId, e.getMessage());
        } finally {
            if (acquired) {
                semaphore.release();
            }
        }
    }

    private int resolveMaxConcurrency() {
        int configured = realtimeProperties.getDelivery().getMaxConcurrency();
        return Math.max(1, configured);
    }
}
