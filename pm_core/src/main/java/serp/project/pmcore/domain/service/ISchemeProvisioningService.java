/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import serp.project.pmcore.domain.entity.project.ProjectEntity;
import serp.project.pmcore.domain.enums.SchemeType;

import java.util.Map;

/**
 * Service responsible for provisioning project scheme bindings.
 *
 * Shared association is the default mode. For system-sourced schemes,
 * implementations may create tenant-shared copies and bind projects to
 * those copies to preserve tenant isolation.
 */
public interface ISchemeProvisioningService {

    /**
     * Provision all scheme bindings for a newly created project.
     * Resolves effective scheme IDs and mutates scheme binding fields on project.
     *
     * @param project          the project entity (scheme ID fields will be mutated)
     * @param tenantId         tenant context
     * @param userId           user performing the action
     * @param blueprintId      optional blueprint ID (nullable)
     * @param schemeOverrides  explicit scheme overrides from request (scheme type name -> scheme ID)
     * @param associationMode  SHARED_ASSOCIATION or CLONE_ON_ASSOCIATE (nullable -> shared)
     */
    void provisionSchemes(ProjectEntity project, Long tenantId, Long userId,
                          Long blueprintId, Map<String, Long> schemeOverrides, String associationMode);

    /**
     * Resolve a source scheme ID to an effective tenant-owned scheme ID
     * under shared-association behavior.
     *
     * Implementations may materialize tenant-shared clones when source
     * schemes belong to system tenant.
     */
    Long resolveSharedSchemeBinding(SchemeType schemeType, Long sourceSchemeId, Long tenantId, Long userId);

    /**
     * Resolve a source scheme ID to a newly cloned tenant scheme ID
     * for project-isolated association.
     *
     * Unlike shared association, this mode always creates a new
     * scheme binding target and does not reuse tenant mapping rows.
     */
    Long resolveClonedSchemeBinding(SchemeType schemeType, Long sourceSchemeId, Long tenantId, Long userId);
}
