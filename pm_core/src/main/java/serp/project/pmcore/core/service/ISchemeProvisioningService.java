/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.core.service;

import serp.project.pmcore.core.domain.entity.ProjectEntity;

import java.util.Map;

/**
 * Service responsible for provisioning project-owned scheme clones
 * by deep-cloning template schemes resolved from blueprint or system defaults.
 */
public interface ISchemeProvisioningService {

    /**
     * Provision all scheme bindings for a newly created project.
     * Deep-clones the resolved template schemes and sets the cloned IDs
     * on the project entity.
     *
     * @param project          the project entity (scheme ID fields will be mutated)
     * @param tenantId         tenant context
     * @param userId           user performing the action
     * @param blueprintId      optional blueprint ID (nullable)
     * @param schemeOverrides  explicit scheme overrides from request (scheme type name -> scheme ID)
     */
    void provisionSchemes(ProjectEntity project, Long tenantId, Long userId,
                          Long blueprintId, Map<String, Long> schemeOverrides);
}
