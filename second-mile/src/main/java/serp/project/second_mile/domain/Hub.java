/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.second_mile.enums.HubStatus;
import serp.project.second_mile.enums.HubType;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "hubs")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class Hub extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "hub_type")
    @Enumerated(EnumType.STRING)
    private HubType hubType;

    @Column(name = "province_code")
    private String provinceCode;

    @Column(name = "ward_code")
    private String wardCode;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "location", columnDefinition = "geography(Point, 4326)")
    private Point location;
    
    @Column(name = "operational_start_date")
    private LocalDate operationalStartDate;

    @Column(name = "operational_end_date")
    private LocalDate operationalEndDate;

    @Column(name = "working_start_time")
    private LocalDateTime workingStartTime;

    @Column(name = "working_end_time")
    private LocalDateTime workingEndTime;

    @Column(name = "daily_capacity")
    @Builder.Default
    private Integer dailyCapacity = 0;

    @Column(name = "current_load")
    @Builder.Default
    private Integer currentLoad = 0;

    @Column(name = "status")
    @Builder.Default
    private HubStatus status = HubStatus.ACTIVE;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "image_url")
    private String imageUrl;

    public boolean isActive() {
        return HubStatus.ACTIVE.equals(status);
    }

}
