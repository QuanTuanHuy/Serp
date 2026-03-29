/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.infrastructure.store.model.TenantSchemeMappingModel;

import java.util.Optional;

@Repository
public interface ITenantSchemeMappingRepository extends JpaRepository<TenantSchemeMappingModel, Long> {

    Optional<TenantSchemeMappingModel> findByTenantIdAndSchemeTypeAndSourceSchemeId(
            Long tenantId,
            SchemeType schemeType,
            Long sourceSchemeId
    );
}
