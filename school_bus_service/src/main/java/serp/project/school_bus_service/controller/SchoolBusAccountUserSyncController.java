package serp.project.school_bus_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import serp.project.school_bus_service.dto.response.AccountUserSyncResultResponse;
import serp.project.school_bus_service.dto.response.GeneralResponse;
import serp.project.school_bus_service.entity.SchoolBusSyncCheckpointEntity;
import serp.project.school_bus_service.repository.SchoolBusSyncCheckpointRepository;
import serp.project.school_bus_service.service.IAccountUserSyncService;
import serp.project.school_bus_service.shared.exception.AppErrorCode;
import serp.project.school_bus_service.shared.exception.AppException;

/**
 * Controller for managing manual synchronization of users from Core Account module.
 */
@RestController
@RequestMapping("/admin/account-users")
public class SchoolBusAccountUserSyncController {

    private final IAccountUserSyncService accountUserSyncService;
    private final SchoolBusSyncCheckpointRepository checkpointRepository;

    public SchoolBusAccountUserSyncController(IAccountUserSyncService accountUserSyncService,
                                              SchoolBusSyncCheckpointRepository checkpointRepository) {
        this.accountUserSyncService = accountUserSyncService;
        this.checkpointRepository = checkpointRepository;
    }

    /**
     * Trigger manual full/incremental sync of users from Account API.
     */
    @PostMapping("/sync")
    public GeneralResponse<AccountUserSyncResultResponse> syncAllUsers() {
        AccountUserSyncResultResponse result = accountUserSyncService.syncAllUsers();
        return GeneralResponse.success("Synchronization process completed", result);
    }

    /**
     * Trigger manual sync of a single user by their Account user ID.
     */
    @PostMapping("/sync/{accountUserId}")
    public GeneralResponse<AccountUserSyncResultResponse> syncSingleUser(@PathVariable Long accountUserId) {
        AccountUserSyncResultResponse result = accountUserSyncService.syncSingleUser(accountUserId);
        return GeneralResponse.success("Single user synchronization completed", result);
    }

    /**
     * Retrieve the current state of the sync checkpoint.
     */
    @GetMapping("/sync/checkpoint")
    public GeneralResponse<SchoolBusSyncCheckpointEntity> getSyncCheckpoint() {
        SchoolBusSyncCheckpointEntity checkpoint = checkpointRepository
                .findFirstBySyncCodeAndIsDeletedFalseOrderByUpdatedAtDescIdDesc("ACCOUNT_USER_SYNC")
                .orElseThrow(() -> new AppException(AppErrorCode.NOT_FOUND, "Sync checkpoint not found"));
        return GeneralResponse.success("Sync checkpoint retrieved successfully", checkpoint);
    }

}
