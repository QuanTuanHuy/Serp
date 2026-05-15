/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.Vehicle;

import java.util.Collection;
import java.util.Set;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {
    boolean existsByTenantIdAndLicensePlateIgnoreCase(Long tenantId, String licensePlate);

    @Query("""
            select lower(trim(v.licensePlate))
            from Vehicle v
            where v.tenantId = :tenantId
                and lower(trim(v.licensePlate)) in :normalizedLicensePlates
            """)
    Set<String> findExistingLicensePlatesByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("normalizedLicensePlates") Collection<String> normalizedLicensePlates
    );
}

