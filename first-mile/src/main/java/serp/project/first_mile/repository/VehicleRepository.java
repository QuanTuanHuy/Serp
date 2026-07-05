/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.Vehicle;
import serp.project.first_mile.enums.VehicleStatus;
import serp.project.first_mile.enums.VehicleType;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @EntityGraph(attributePaths = "postOffice")
    @Query("""
            select v
            from Vehicle v
            where v.tenantId = :tenantId
                and (
                    :keyword is null
                    or :keyword = ''
                    or lower(v.licensePlate) like lower(concat('%', :keyword, '%'))
                )
                and (:vehicleType is null or v.vehicleType = :vehicleType)
                and (:status is null or v.status = :status)
                and (
                    :postOfficeKeyword is null
                    or :postOfficeKeyword = ''
                    or (
                        v.postOffice is not null
                        and (
                            lower(v.postOffice.code) like lower(concat('%', :postOfficeKeyword, '%'))
                            or lower(v.postOffice.name) like lower(concat('%', :postOfficeKeyword, '%'))
                        )
                    )
                )
                and (
                    :courierKeyword is null
                    or :courierKeyword = ''
                    or exists (
                        select 1
                        from PostOfficeStaff s
                        where s.id = v.postOfficeStaffId
                            and s.tenantId = :tenantId
                            and (
                                lower(s.code) like lower(concat('%', :courierKeyword, '%'))
                                or lower(s.fullName) like lower(concat('%', :courierKeyword, '%'))
                            )
                    )
                )
            """)
    Page<Vehicle> searchByTenantId(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("vehicleType") VehicleType vehicleType,
            @Param("status") VehicleStatus status,
            @Param("postOfficeKeyword") String postOfficeKeyword,
            @Param("courierKeyword") String courierKeyword,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "postOffice")
    @Query("""
            select v
            from Vehicle v
            where v.tenantId = :tenantId
                and (
                    (v.postOffice is not null and v.postOffice.id in :managedPostOfficeIds)
                    or exists (
                        select 1
                        from PostOfficeStaffAssignment a
                        where a.staff.id = v.postOfficeStaffId
                            and a.postOffice.id in :managedPostOfficeIds
                            and a.tenantId = :tenantId
                            and a.assignedFrom <= :today
                            and (a.assignedTo is null or a.assignedTo >= :today)
                    )
                )
                and (
                    :keyword is null
                    or :keyword = ''
                    or lower(v.licensePlate) like lower(concat('%', :keyword, '%'))
                )
                and (:vehicleType is null or v.vehicleType = :vehicleType)
                and (:status is null or v.status = :status)
                and (
                    :postOfficeKeyword is null
                    or :postOfficeKeyword = ''
                    or (
                        v.postOffice is not null
                        and (
                            lower(v.postOffice.code) like lower(concat('%', :postOfficeKeyword, '%'))
                            or lower(v.postOffice.name) like lower(concat('%', :postOfficeKeyword, '%'))
                        )
                    )
                )
                and (
                    :courierKeyword is null
                    or :courierKeyword = ''
                    or exists (
                        select 1
                        from PostOfficeStaff s
                        where s.id = v.postOfficeStaffId
                            and s.tenantId = :tenantId
                            and (
                                lower(s.code) like lower(concat('%', :courierKeyword, '%'))
                                or lower(s.fullName) like lower(concat('%', :courierKeyword, '%'))
                            )
                    )
                )
            """)
    Page<Vehicle> searchByTenantIdAndManagedPostOfficeIds(
            @Param("tenantId") Long tenantId,
            @Param("keyword") String keyword,
            @Param("vehicleType") VehicleType vehicleType,
            @Param("status") VehicleStatus status,
            @Param("postOfficeKeyword") String postOfficeKeyword,
            @Param("courierKeyword") String courierKeyword,
            @Param("managedPostOfficeIds") Collection<Long> managedPostOfficeIds,
            @Param("today") LocalDate today,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "postOffice")
    Optional<Vehicle> findByIdAndTenantId(Long id, Long tenantId);

    boolean existsByLicensePlateIgnoreCaseAndTenantId(String licensePlate, Long tenantId);

    boolean existsByLicensePlateIgnoreCaseAndTenantIdAndIdNot(String licensePlate, Long tenantId, Long id);

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

    List<Vehicle> findByTenantIdAndPostOffice_IdAndStatusIn(
            Long tenantId,
            Long postOfficeId,
            Collection<VehicleStatus> statuses
    );
}
