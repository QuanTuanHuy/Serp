/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.RoleModel;

@Repository
public interface IRoleRepository extends IBaseRepository<RoleModel> {
    Optional<RoleModel> findByName(String name);

    List<RoleModel> findByIdIn(List<Long> ids);

    @Query("""
            SELECT r FROM RoleModel r
            WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            ORDER BY r.name ASC
            """)
    List<RoleModel> searchRoles(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT COUNT(r) FROM RoleModel r
            WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(COALESCE(r.description, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Long countSearchRoles(@Param("search") String search);
}
