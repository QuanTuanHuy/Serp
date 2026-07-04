/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_billing_service.domain.Tariff;
import serp.project.tms_billing_service.enums.RouteType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    Optional<Tariff> findByServiceCodeAndRouteTypeCodeAndEffectiveDate(
            String serviceCode,
            RouteType routeTypeCode,
            LocalDate effectiveDate
    );

    Optional<Tariff> findFirstByServiceCodeAndRouteTypeCodeAndEffectiveDateLessThanEqualAndExpirationDateIsNullOrderByEffectiveDateDesc(
            String serviceCode,
            RouteType routeTypeCode,
            LocalDate pricingDate
    );

    Optional<Tariff> findFirstByServiceCodeAndRouteTypeCodeAndEffectiveDateLessThanEqualAndExpirationDateGreaterThanEqualOrderByEffectiveDateDesc(
            String serviceCode,
            RouteType routeTypeCode,
            LocalDate effectiveDate,
            LocalDate expirationDate
    );

    List<Tariff> findAllByServiceCodeOrderByRouteTypeCodeAscEffectiveDateDesc(String serviceCode);

    List<Tariff> findAllByOrderByServiceCodeAscRouteTypeCodeAscEffectiveDateDesc();
}
