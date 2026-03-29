/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.service;

import serp.project.pmcore.domain.project.dto.ProjectProvisioningRequest;
import serp.project.pmcore.domain.project.dto.ProjectProvisioningResult;
import serp.project.pmcore.domain.project.entity.ProjectEntity;

/**
 * Service responsible for resolving source schemes into effective
 * scheme bindings for a project creation or rebinding flow.
 */
public interface ISchemeProvisioningService {

    /**
     * Provision all scheme bindings for a project.
     * The project may already exist as a shell row so project-scoped
     * contexts can be materialized safely inside one transaction.
     */
    ProjectProvisioningResult provisionProjectSchemes(ProjectEntity project, ProjectProvisioningRequest request);
}
