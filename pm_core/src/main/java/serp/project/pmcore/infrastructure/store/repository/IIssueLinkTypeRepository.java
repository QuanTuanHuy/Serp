/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.IssueLinkTypeModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface IIssueLinkTypeRepository extends JpaRepository<IssueLinkTypeModel, Long> {

    Optional<IssueLinkTypeModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("""
            SELECT ilt
              FROM IssueLinkTypeModel ilt
             WHERE ilt.id = :id
               AND (ilt.tenantId = :tenantId OR ilt.tenantId = 0)
            """)
    Optional<IssueLinkTypeModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id,
                                                                   @Param("tenantId") Long tenantId);

    @Query("""
            SELECT ilt
              FROM IssueLinkTypeModel ilt
             WHERE ilt.tenantId = :tenantId
               AND LOWER(ilt.name) = :normalizedName
             ORDER BY ilt.id ASC
            """)
    Optional<IssueLinkTypeModel> findFirstByTenantIdAndNormalizedName(@Param("tenantId") Long tenantId,
                                                                      @Param("normalizedName") String normalizedName);

    List<IssueLinkTypeModel> findAllByTenantIdOrderByNameAsc(Long tenantId);

    @Query("""
            SELECT ilt
              FROM IssueLinkTypeModel ilt
             WHERE ilt.tenantId = :tenantId OR ilt.tenantId = 0
             ORDER BY ilt.name ASC
            """)
    List<IssueLinkTypeModel> findByTenantIdOrSystemTenantOrderByNameAsc(@Param("tenantId") Long tenantId);
}
