/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.BagCapacitySettings;

import java.util.Optional;

@Repository
public interface BagCapacitySettingsRepository extends JpaRepository<BagCapacitySettings, Long> {
    Optional<BagCapacitySettings> findByTenantId(Long tenantId);
}
