/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import serp.project.account.core.domain.enums.ModuleStatus;
import serp.project.account.core.domain.enums.ModuleType;
import serp.project.account.infrastructure.store.model.ModuleModel;

@Repository
public interface IModuleRepository extends IBaseRepository<ModuleModel> {
    Optional<ModuleModel> findByModuleName(String moduleName);

    Optional<ModuleModel> findByCode(String code);

    boolean existsByModuleName(String moduleName);

    boolean existsByCode(String code);

    Long countByStatus(ModuleStatus status);

    @Query("""
            SELECT m FROM ModuleModel m
            WHERE LOWER(m.moduleName) LIKE :search
               OR LOWER(m.code) LIKE :search
               OR LOWER(m.description) LIKE :search
            ORDER BY m.moduleName ASC
            """)
    List<ModuleModel> searchModules(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT COUNT(m) FROM ModuleModel m
            WHERE LOWER(m.moduleName) LIKE :search
               OR LOWER(m.code) LIKE :search
               OR LOWER(m.description) LIKE :search
            """)
    Long countSearchModules(@Param("search") String search);

    @Query("""
            SELECT m FROM ModuleModel m
            WHERE (:search IS NULL OR LOWER(m.moduleName) LIKE :search
               OR LOWER(m.code) LIKE :search
               OR LOWER(m.description) LIKE :search)
              AND (:status IS NULL OR m.status = :status)
              AND (:moduleType IS NULL OR m.moduleType = :moduleType)
            """)
    Page<ModuleModel> findAllPaginated(
            @Param("search") String search,
            @Param("status") ModuleStatus status,
            @Param("moduleType") ModuleType moduleType,
            Pageable pageable);
}
