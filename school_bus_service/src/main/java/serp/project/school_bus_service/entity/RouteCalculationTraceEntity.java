package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import serp.project.school_bus_service.enums.RouteCalculationStatus;
import serp.project.school_bus_service.enums.RouteCalculationType;

@Entity
@Table(name = "school_bus_route_calculation_trace")
@Getter
@Setter
public class RouteCalculationTraceEntity extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "route_plan_id")
    private RoutePlanEntity routePlan;

    @ManyToOne
    @JoinColumn(name = "planning_session_id")
    private RoutePlanningSessionEntity planningSession;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 50)
    private RouteCalculationType calculationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_status", nullable = false, length = 50)
    private RouteCalculationStatus calculationStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_json", columnDefinition = "jsonb")
    private String inputJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "matrix_json", columnDefinition = "jsonb")
    private String matrixJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "timeline_json", columnDefinition = "jsonb")
    private String timelineJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "issues_json", columnDefinition = "jsonb")
    private String issuesJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_snapshot_json", columnDefinition = "jsonb")
    private String configSnapshotJson;

    @Column(name = "source_summary", length = 255)
    private String sourceSummary;
}
