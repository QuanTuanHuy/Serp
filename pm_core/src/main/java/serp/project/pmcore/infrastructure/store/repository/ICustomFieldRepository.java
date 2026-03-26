/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.CustomFieldModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICustomFieldRepository extends JpaRepository<CustomFieldModel, Long> {

    Optional<CustomFieldModel> findByIdAndTenantId(Long id, Long tenantId);

    @Query("SELECT f FROM CustomFieldModel f WHERE f.id = :id AND (f.tenantId = :tenantId OR f.tenantId = 0)")
    Optional<CustomFieldModel> findByIdAndTenantIdOrSystemTenant(@Param("id") Long id,
                                                                 @Param("tenantId") Long tenantId);

    Optional<CustomFieldModel> findFirstByTenantIdAndFieldKeyOrderByIdAsc(Long tenantId, String fieldKey);

    @Query("SELECT f FROM CustomFieldModel f WHERE f.fieldKey IN :fieldKeys AND (f.tenantId = :tenantId OR f.tenantId = 0) ORDER BY f.id ASC")
    List<CustomFieldModel> findAllByFieldKeyInAndTenantIdOrSystemTenant(@Param("fieldKeys") List<String> fieldKeys,
                                                                         @Param("tenantId") Long tenantId);
}
