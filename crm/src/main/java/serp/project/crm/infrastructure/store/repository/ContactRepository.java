/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.ContactModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<ContactModel, Long> {

    Optional<ContactModel> findByIdAndTenantId(Long id, Long tenantId);

    Page<ContactModel> findByTenantId(Long tenantId, Pageable pageable);

    List<ContactModel> findByTenantIdAndCustomerId(Long tenantId, Long customerId);

    Optional<ContactModel> findByTenantIdAndCustomerIdAndIsPrimary(Long tenantId, Long customerId, Boolean isPrimary);

    Page<ContactModel> findByTenantIdAndActiveStatus(Long tenantId, String activeStatus, Pageable pageable);

    Optional<ContactModel> findByEmailAndTenantId(String email, Long tenantId);

    boolean existsByEmailAndTenantId(String email, Long tenantId);

    @Query("SELECT c FROM ContactModel c WHERE c.tenantId = :tenantId " +
            "AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ContactModel> searchByKeyword(@Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            Pageable pageable);

    Page<ContactModel> findByTenantIdAndContactType(Long tenantId, String contactType, Pageable pageable);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndCustomerId(Long tenantId, Long customerId);

    long countByTenantIdAndActiveStatus(Long tenantId, String activeStatus);
}
