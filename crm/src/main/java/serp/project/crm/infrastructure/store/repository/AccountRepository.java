/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.AccountModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountModel, Long>, JpaSpecificationExecutor<AccountModel> {

    Optional<AccountModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<AccountModel> findByTenantId(Long tenantId, Pageable pageable);

    Page<AccountModel> findByTenantIdAndActiveStatus(Long tenantId, String activeStatus, Pageable pageable);

    Optional<AccountModel> findByEmailAndTenantId(String email, Long tenantId);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    List<AccountModel> findByParentAccountIdAndTenantId(Long parentAccountId, Long tenantId);

    @Query("SELECT c FROM AccountModel c WHERE c.tenantId = :tenantId " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AccountModel> searchByKeyword(@Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    Page<AccountModel> findByTenantIdAndIndustry(Long tenantId, String industry, Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndActiveStatus(Long tenantId, String activeStatus);
}
