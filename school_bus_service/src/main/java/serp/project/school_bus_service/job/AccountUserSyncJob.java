package serp.project.school_bus_service.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import serp.project.school_bus_service.service.IAccountUserSyncService;

/**
 * Scheduled sync job to run incrementally and synchronize
 * users from the Core Account API.
 */
@Component
@Slf4j
public class AccountUserSyncJob {

    private final IAccountUserSyncService accountUserSyncService;

    @Value("${school-bus.account-sync.enabled:true}")
    private boolean enabled;

    public AccountUserSyncJob(IAccountUserSyncService accountUserSyncService) {
        this.accountUserSyncService = accountUserSyncService;
    }

    /**
     * Periodically triggers the sync service based on cron expression.
     */
    @Scheduled(cron = "${school-bus.account-sync.cron:0 */30 * * * *}")
    public void runSync() {
        if (!enabled) {
            log.debug("Account User Sync Job is disabled via config.");
            return;
        }

        log.info("Triggering scheduled Account User Sync Job...");
        try {
            accountUserSyncService.syncAllUsers();
        } catch (Exception e) {
            log.error("Error occurred during scheduled Account User Sync Job: {}", e.getMessage(), e);
        }
    }
}
