package serp.project.school_bus_service.service.impl;

import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.client.AccountUserClient;
import serp.project.school_bus_service.dto.request.SchoolBusUserUpsertCommand;
import serp.project.school_bus_service.dto.response.AccountUserResponse;
import serp.project.school_bus_service.dto.response.AccountUserSyncResultResponse;
import serp.project.school_bus_service.entity.SchoolBusSyncCheckpointEntity;
import serp.project.school_bus_service.repository.SchoolBusSyncCheckpointRepository;
import serp.project.school_bus_service.service.IAccountUserSyncService;
import serp.project.school_bus_service.service.ISchoolBusUserService;
import serp.project.school_bus_service.shared.auth.TokenUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class AccountUserSyncServiceImpl implements IAccountUserSyncService {

    private static final String SYNC_CODE = "ACCOUNT_USER_SYNC";

    private final TokenUtils tokenUtils;
    private final AccountUserClient accountUserClient;
    private final ISchoolBusUserService schoolBusUserService;
    private final SchoolBusSyncCheckpointRepository checkpointRepository;
    private final ObjectMapper objectMapper;

    @Value("${school-bus.account-sync.initial-lookback-days:30}")
    private int initialLookbackDays;

    public AccountUserSyncServiceImpl(TokenUtils tokenUtils,
                                      AccountUserClient accountUserClient,
                                      ISchoolBusUserService schoolBusUserService,
                                      SchoolBusSyncCheckpointRepository checkpointRepository,
                                      ObjectMapper objectMapper) {
        this.tokenUtils = tokenUtils;
        this.accountUserClient = accountUserClient;
        this.schoolBusUserService = schoolBusUserService;
        this.checkpointRepository = checkpointRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AccountUserSyncResultResponse syncAllUsers() {
        LocalDateTime startedAt = LocalDateTime.now();
        log.info("Starting Account User Sync job. StartedAt={}", startedAt);

        // 1. Fetch or initialize checkpoint
        SchoolBusSyncCheckpointEntity checkpoint = checkpointRepository
                .findFirstBySyncCodeAndIsDeletedFalseOrderByUpdatedAtDescIdDesc(SYNC_CODE)
                .orElseGet(() -> {
                    SchoolBusSyncCheckpointEntity newCheckpoint = new SchoolBusSyncCheckpointEntity();
                    newCheckpoint.setSyncCode(SYNC_CODE);
                    newCheckpoint.setLastSyncedCount(0);
                    newCheckpoint.markCreated(0L, "SYSTEM");
                    newCheckpoint.setIsActive(true);
                    newCheckpoint.setIsDeleted(false);
                    return checkpointRepository.save(newCheckpoint);
                });

        checkpoint.setLastAttemptSyncAt(startedAt);

        // 2. Get service token
        Optional<String> tokenOpt = tokenUtils.getServiceToken();
        if (tokenOpt.isEmpty()) {
            String errorMsg = "Could not obtain Keycloak service credentials token";
            log.error(errorMsg);
            checkpoint.setLastStatus("FAILED");
            checkpoint.setLastErrorMessage(errorMsg);
            checkpointRepository.save(checkpoint);

            return AccountUserSyncResultResponse.builder()
                    .syncCode(SYNC_CODE)
                    .startedAt(startedAt)
                    .finishedAt(LocalDateTime.now())
                    .successCount(0)
                    .failedCount(0)
                    .skippedCount(0)
                    .message(errorMsg)
                    .errors(new ArrayList<>())
                    .build();
        }
        String token = tokenOpt.get();

        LocalDateTime lastSuccessSyncLimit = checkpoint.getLastSuccessSyncAt();
        if (lastSuccessSyncLimit == null) {
            lastSuccessSyncLimit = startedAt.minusDays(initialLookbackDays);
            log.info("No successful sync checkpoint found. Setting initial lookback limit to: {}", lastSuccessSyncLimit);
        }

        int page = 0;
        int pageSize = 50;
        int successCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        boolean stopSync = false;
        List<AccountUserSyncResultResponse.ErrorItem> errorItems = new ArrayList<>();

        // 3. Page loop
        while (!stopSync) {
            log.debug("Fetching user page {} from Account Service...", page);
            Optional<AccountUserClient.PagedResponse> pagedResponseOpt = accountUserClient.fetchUsersPage(page, pageSize, token);

            if (pagedResponseOpt.isEmpty()) {
                log.warn("Failed to fetch users page {} from Account API.", page);
                break;
            }

            AccountUserClient.PagedResponse pagedResponse = pagedResponseOpt.get();
            List<AccountUserResponse> users = pagedResponse.items();

            if (users == null || users.isEmpty()) {
                log.debug("No more users returned from API.");
                break;
            }

            for (AccountUserResponse user : users) {
                // Parse user updatedAt epoch timestamp
                LocalDateTime userUpdatedAt = startedAt.minusDays(365); // default fallback if updatedAt is null
                if (user.getUpdatedAt() != null) {
                    userUpdatedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(user.getUpdatedAt()), ZoneId.systemDefault());
                }

                // If user updatedAt is older than checkpoint success date, we can stop sync
                if (userUpdatedAt.isBefore(lastSuccessSyncLimit)) {
                    log.info("Reached user updated at {} which is older than sync limit {}. Stopping sync.", 
                            userUpdatedAt, lastSuccessSyncLimit);
                    stopSync = true;
                    break;
                }

                // Upsert user
                try {
                    boolean synced = upsertUser(user);
                    if (synced) {
                        successCount++;
                    } else {
                        skippedCount++;
                    }
                } catch (Exception e) {
                    failedCount++;
                    errorItems.add(AccountUserSyncResultResponse.ErrorItem.builder()
                            .accountUserId(user.getId())
                            .email(user.getEmail())
                            .reason(e.getMessage())
                            .build());
                }
            }

            if (page >= pagedResponse.totalPages() - 1) {
                log.debug("Reached final page ({} / {}).", page, pagedResponse.totalPages());
                break;
            }

            page++;
        }

        // 4. Update checkpoint
        LocalDateTime finishedAt = LocalDateTime.now();
        checkpoint.setLastAttemptSyncAt(startedAt);
        checkpoint.setLastSyncedCount(successCount);

        if (failedCount == 0) {
            checkpoint.setLastStatus("SUCCESS");
            checkpoint.setLastSuccessSyncAt(startedAt);
            checkpoint.setLastErrorMessage(null);
        } else {
            checkpoint.setLastStatus("PARTIAL_SUCCESS");
            checkpoint.setLastSuccessSyncAt(startedAt); // Still move forward to prevent duplicate failures
            checkpoint.setLastErrorMessage("Failed to sync " + failedCount + " users. Check errors in logs.");
        }
        checkpointRepository.save(checkpoint);

        log.info("Account User Sync job finished. Success={}, Failed={}, Skipped={}", successCount, failedCount, skippedCount);

        return AccountUserSyncResultResponse.builder()
                .syncCode(SYNC_CODE)
                .startedAt(startedAt)
                .finishedAt(finishedAt)
                .successCount(successCount)
                .failedCount(failedCount)
                .skippedCount(skippedCount)
                .message("Sync process completed. Errors: " + failedCount)
                .errors(errorItems)
                .build();
    }

    @Override
    @Transactional
    public AccountUserSyncResultResponse syncSingleUser(Long accountUserId) {
        LocalDateTime startedAt = LocalDateTime.now();
        log.info("Starting single user sync for ID: {}", accountUserId);

        Optional<String> tokenOpt = tokenUtils.getServiceToken();
        if (tokenOpt.isEmpty()) {
            return AccountUserSyncResultResponse.builder()
                    .syncCode("SINGLE_USER_SYNC")
                    .startedAt(startedAt)
                    .finishedAt(LocalDateTime.now())
                    .successCount(0)
                    .failedCount(1)
                    .message("Failed to retrieve service credentials token")
                    .build();
        }

        Optional<AccountUserResponse> userOpt = accountUserClient.fetchUserById(accountUserId, tokenOpt.get());
        if (userOpt.isEmpty()) {
            return AccountUserSyncResultResponse.builder()
                    .syncCode("SINGLE_USER_SYNC")
                    .startedAt(startedAt)
                    .finishedAt(LocalDateTime.now())
                    .successCount(0)
                    .failedCount(1)
                    .message("User not found in Account service")
                    .build();
        }

        List<AccountUserSyncResultResponse.ErrorItem> errorItems = new ArrayList<>();
        int successCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        try {
            boolean synced = upsertUser(userOpt.get());
            if (synced) {
                successCount++;
            } else {
                skippedCount++;
            }
        } catch (Exception e) {
            failedCount++;
            errorItems.add(AccountUserSyncResultResponse.ErrorItem.builder()
                    .accountUserId(accountUserId)
                    .email(userOpt.get().getEmail())
                    .reason(e.getMessage())
                    .build());
        }

        return AccountUserSyncResultResponse.builder()
                .syncCode("SINGLE_USER_SYNC")
                .startedAt(startedAt)
                .finishedAt(LocalDateTime.now())
                .successCount(successCount)
                .failedCount(failedCount)
                .skippedCount(skippedCount)
                .message(failedCount > 0 ? "Failed single sync" : "Successfully synced user")
                .errors(errorItems)
                .build();
    }

    private boolean upsertUser(AccountUserResponse user) throws Exception {
        if (user.getId() == null || user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Skipping user sync due to missing id or email: id={}, email={}", user.getId(), user.getEmail());
            return false;
        }

        // organizationId acts as tenantId
        Long tenantId = user.getOrganizationId();
        if (tenantId == null) {
            log.warn("Skipping user sync because organizationId is missing for user ID: {}", user.getId());
            return false;
        }

        SchoolBusUserUpsertCommand command = new SchoolBusUserUpsertCommand();
        command.setTenantId(tenantId);
        command.setAccountUserId(user.getId());
        command.setEmail(user.getEmail());
        command.setFirstName(user.getFirstName());
        command.setLastName(user.getLastName());
        command.setPhoneNumber(user.getPhoneNumber());
        command.setPrimaryOrganizationId(user.getOrganizationId());
        command.setSyncSource("ACCOUNT_API");
        command.setRawPayloadJson(objectMapper.writeValueAsString(user));
        
        // Account user ID is the only identity used by School Bus synchronization.
        command.setAvatarUrl(user.getAvatarUrl());
        command.setPreferredLanguage(user.getPreferredLanguage());
        command.setTimezone(user.getTimezone());
        command.setUserType(user.getUserType());
        command.setStatus(user.getStatus() != null ? user.getStatus() : "ACTIVE");
        command.setRoles(user.getRoles());

        schoolBusUserService.upsertFromAccountUser(command);
        return true;
    }

}
