/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.ImportHistory;

import java.util.Optional;

@Repository
public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    Optional<ImportHistory> findByIdAndTenantId(Long id, Long tenantId);
}
