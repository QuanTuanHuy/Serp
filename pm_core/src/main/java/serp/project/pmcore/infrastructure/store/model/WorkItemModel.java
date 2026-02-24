/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_items")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class WorkItemModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    @Column(name = "issue_type_id", nullable = false)
    private Long issueTypeId;

    @Column(name = "issue_no", nullable = false)
    private Long issueNo;
    @Column(name = "key", nullable = false)
    private String key;
    @Column(name = "summary", nullable = false)
    private String summary;
    @Column(name = "description")
    private String description;

    @Column(name = "status_id", nullable = false)
    private Long statusId;
    @Column(name = "priority_id", nullable = false)
    private Long priorityId;
    @Column(name = "assignee_id")
    private Long assigneeId;
    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;
    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "due_date")
    private LocalDateTime dueDate;
    @Column(name = "rank")
    private String rank;

    @Column(name = "time_original_estimate")
    private Long timeOriginalEstimate;
    @Column(name = "time_remaining_estimate")
    private Long timeRemainingEstimate;
    @Column(name = "time_spent")
    private Long timeSpent;
}
