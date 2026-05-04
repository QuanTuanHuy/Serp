/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.ImportHistory;
import serp.project.second_mile.enums.ImportType;

import java.util.Optional;

@Repository
public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    Optional<ImportHistory> findByIdAndTenantId(Long id, Long tenantId);

    Page<ImportHistory> findAllByTenantId(Long tenantId, Pageable pageable);

    Page<ImportHistory> findAllByTenantIdAndType(Long tenantId, ImportType type, Pageable pageable);
}
