/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.optimization.service;

import serp.project.pmcore.domain.optimization.model.CapacityResolutionResult;
import serp.project.pmcore.domain.optimization.model.ResourceCapacitySlot;

import java.util.List;

/**
 * Service interface for querying and resolving resource capacities and workload allocations.
 * Resource capacity represents the working time available for project members, which is
 * used as a key constraint during project schedule optimization.
 */
public interface IResourceCapacityService {

    /**
     * Retrieves the working capacity slots (available time) for a list of users within a planning window.
     *
     * @param tenantId      the unique identifier of the tenant owning the resource data
     * @param userIds       the list of user identifiers whose capacities are queried
     * @param planningStart the start timestamp (in milliseconds) of the planning window
     * @param planningEnd   the end timestamp (in milliseconds) of the planning window
     * @return the list of available {@link ResourceCapacitySlot} objects
     */
    List<ResourceCapacitySlot> getCapacitySlots(Long tenantId,
                                                List<Long> userIds,
                                                Long planningStart,
                                                Long planningEnd);

    /**
     * Resolves and calculates the net capacity of resources after deducting existing workload allocations
     * (both same-project allocations outside optimization scope and cross-project allocations).
     *
     * @param tenantId             the unique identifier of the tenant
     * @param projectId            the current project identifier
     * @param userIds              the list of user identifiers to resolve capacity for
     * @param planningStart        the start timestamp of the planning window
     * @param planningEnd          the end timestamp of the planning window
     * @param excludedWorkItemIds  the list of work item IDs to exclude from workload calculations (typically the items being optimized)
     * @return the {@link CapacityResolutionResult} containing net capacity slots, deductions, and warnings
     */
    CapacityResolutionResult resolveCapacity(Long tenantId,
                                             Long projectId,
                                             List<Long> userIds,
                                             Long planningStart,
                                             Long planningEnd,
                                             List<Long> excludedWorkItemIds);
}
