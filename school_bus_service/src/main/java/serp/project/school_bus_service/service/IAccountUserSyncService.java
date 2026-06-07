package serp.project.school_bus_service.service;

import serp.project.school_bus_service.dto.response.AccountUserSyncResultResponse;

/**
 * Service to orchestrate user synchronization from Account API.
 */
public interface IAccountUserSyncService {

    /**
     * Synchronize all users (incremental sync starting from last successful checkpoint).
     */
    AccountUserSyncResultResponse syncAllUsers();

    /**
     * Synchronize a single user by their Account user ID.
     */
    AccountUserSyncResultResponse syncSingleUser(Long accountUserId);

}
