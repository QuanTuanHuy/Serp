/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.HubPostOfficeMapping;

import java.util.Optional;

@Repository
public interface HubPostOfficeMappingRepository extends JpaRepository<HubPostOfficeMapping, Long> {

    Page<HubPostOfficeMapping> findByHub_IdAndTenantId(Long hubId, Long tenantId, Pageable pageable);

    Page<HubPostOfficeMapping> findByTenantId(Long tenantId, Pageable pageable);

    Optional<HubPostOfficeMapping> findByTenantIdAndPostOfficeCode(Long tenantId, String postOfficeCode);

    void deleteByTenantIdAndPostOfficeCode(Long tenantId, String postOfficeCode);

    void deleteByHub_IdAndPostOfficeCodeAndTenantId(Long hubId, String postOfficeCode, Long tenantId);
}
