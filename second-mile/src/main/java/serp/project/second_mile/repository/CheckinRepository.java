/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.Checkin;
import serp.project.second_mile.enums.CheckinType;

import java.util.Optional;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    Optional<Checkin> findByTenantIdAndCheckinTypeAndBagDistributionManifestId(
            Long tenantId,
            CheckinType checkinType,
            Long bagDistributionManifestId
    );
}
