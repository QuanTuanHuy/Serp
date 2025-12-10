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
import serp.project.crm.infrastructure.store.model.CustomerModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerModel, Long>, JpaSpecificationExecutor<CustomerModel> {

    Optional<CustomerModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<CustomerModel> findByTenantId(Long tenantId, Pageable pageable);

    Page<CustomerModel> findByTenantIdAndActiveStatus(Long tenantId, String activeStatus, Pageable pageable);

    Optional<CustomerModel> findByEmailAndTenantId(String email, Long tenantId);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    List<CustomerModel> findByParentCustomerIdAndTenantId(Long parentCustomerId, Long tenantId);

    @Query("SELECT c FROM CustomerModel c WHERE c.tenantId = :tenantId " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<CustomerModel> searchByKeyword(@Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    Page<CustomerModel> findByTenantIdAndIndustry(Long tenantId, String industry, Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndActiveStatus(Long tenantId, String activeStatus);
}
