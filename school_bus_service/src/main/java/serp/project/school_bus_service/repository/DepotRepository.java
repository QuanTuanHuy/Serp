package serp.project.school_bus_service.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import serp.project.school_bus_service.entity.DepotEntity;
import serp.project.school_bus_service.repository.projection.DepotFleetSummaryProjection;
import serp.project.school_bus_service.shared.base.BaseRepository;

public interface DepotRepository extends BaseRepository<DepotEntity, Long> {

    @Query("""
            select count(d.id) as totalDepots,
                   coalesce(sum(case
                       when d.latitude is not null and d.longitude is not null then 1
                       else 0
                   end), 0) as depotsWithCoordinates
            from DepotEntity d
            where d.tenantId = :tenantId
              and d.isDeleted = false
            """)
    DepotFleetSummaryProjection getFleetSummary(@Param("tenantId") Long tenantId);
}
