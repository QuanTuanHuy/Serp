/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "rep_time_blocks", indexes = {
        @Index(name = "idx_rep_time_blocks_member_time", columnList = "team_member_id, start_time, end_time"),
        @Index(name = "idx_rep_time_blocks_tenant_id", columnList = "tenant_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_rep_time_blocks_activity", columnNames = {"tenant_id", "activity_id"})
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class RepTimeBlockModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "team_member_id", nullable = false)
    private Long teamMemberId;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "start_time", nullable = false)
    private Long startTime;

    @Column(name = "end_time", nullable = false)
    private Long endTime;

    @Column(name = "block_type", nullable = false, length = 20)
    private String blockType;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
