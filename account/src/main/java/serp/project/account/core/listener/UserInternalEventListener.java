/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.domain.event.UserOnlineInternalEvent;
import serp.project.account.core.service.IUserService;

@Component
@Slf4j
public class UserInternalEventListener {

    private final IUserService userService;
    private final ExecutorService executorService;

    public UserInternalEventListener(
        IUserService userService,
        @Qualifier("virtualThreadExecutor") ExecutorService virtualThreadExecutor) {
        this.userService = userService;
        this.executorService = virtualThreadExecutor;
    }

    @TransactionalEventListener
    public void handleUserOnline(UserOnlineInternalEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                UserEntity user;
                if (event.userId() != null) {
                    user = userService.getUserById(event.userId());
                } else {
                    user = userService.getUserByEmail(event.email());
                }
                if (user == null) {
                    log.warn("User not found for online event: userId={}, email={}", event.userId(), event.email());
                    return;
                }
                user.setLastLoginAt(event.lastLoginAt().toEpochMilli());
                userService.updateUser(user.getId(), user);
            } catch (Exception e) {
                log.error("Error handling UserOnlineInternalEvent for userId={}, email={}: {}",
                        event.userId(), event.email(), e.getMessage(), e);
            }

        }, executorService);
    }
}
