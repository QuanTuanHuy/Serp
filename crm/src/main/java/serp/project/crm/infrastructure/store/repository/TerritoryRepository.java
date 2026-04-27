/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.crm.infrastructure.store.model.TerritoryModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface TerritoryRepository extends JpaRepository<TerritoryModel, Long> {
    Optional<TerritoryModel> findByTenantIdAndTerritoryCode(Long tenantId, String territoryCode);

    Optional<TerritoryModel> findByTenantIdAndTerritoryCodeAndActiveTrue(Long tenantId, String territoryCode);

    List<TerritoryModel> findByTenantIdAndTerritoryCodeInAndActiveTrue(Long tenantId, List<String> territoryCodes);

    List<TerritoryModel> findByTenantIdIn(List<Long> tenantIds);

    List<TerritoryModel> findByTenantIdInAndTerritoryCodeInAndActiveTrue(List<Long> tenantIds, List<String> territoryCodes);

    @Query("SELECT t FROM TerritoryModel t WHERE t.tenantId = :tenantId AND t.active = true AND (LOWER(t.territoryName) = LOWER(:state) OR LOWER(t.territoryName) = LOWER(:city) OR LOWER(t.territoryCode) = LOWER(:state) OR LOWER(t.territoryCode) = LOWER(:city))")
    Optional<TerritoryModel> findByStateOrCity(@Param("tenantId") Long tenantId,
            @Param("state") String state,
            @Param("city") String city);

    @Query("SELECT t FROM TerritoryModel t WHERE t.tenantId IN :tenantIds AND t.active = true AND (LOWER(t.territoryName) = LOWER(:state) OR LOWER(t.territoryName) = LOWER(:city) OR LOWER(t.territoryCode) = LOWER(:state) OR LOWER(t.territoryCode) = LOWER(:city)) ORDER BY CASE WHEN t.tenantId = :tenantId THEN 0 ELSE 1 END")
    List<TerritoryModel> findMergedByStateOrCity(@Param("tenantIds") List<Long> tenantIds,
            @Param("tenantId") Long tenantId,
            @Param("state") String state,
            @Param("city") String city);
}
