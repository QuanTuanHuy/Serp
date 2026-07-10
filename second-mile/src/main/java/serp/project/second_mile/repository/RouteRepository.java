/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.Route;
import serp.project.second_mile.enums.RouteDestinationType;
import serp.project.second_mile.enums.RouteEndpointType;
import serp.project.second_mile.enums.RouteStatus;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long>, JpaSpecificationExecutor<Route> {
    boolean existsByTenantIdAndRouteCodeIgnoreCase(Long tenantId, String routeCode);

    List<Route> findByTenantIdAndStatus(Long tenantId, RouteStatus status);

    List<Route> findByTenantIdAndStatusAndOriginTypeAndOriginHubIdAndDestinationTypeAndDestinationHubId(
            Long tenantId,
            RouteStatus status,
            RouteEndpointType originType,
            Long originHubId,
            RouteDestinationType destinationType,
            Long destinationHubId
    );

    List<Route> findByTenantIdAndStatusAndOriginTypeAndOriginHubIdAndDestinationTypeAndDestinationPostOfficeCodeIgnoreCase(
            Long tenantId,
            RouteStatus status,
            RouteEndpointType originType,
            Long originHubId,
            RouteDestinationType destinationType,
            String destinationPostOfficeCode
    );
}
