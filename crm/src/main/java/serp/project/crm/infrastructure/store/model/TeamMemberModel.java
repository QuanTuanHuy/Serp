/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "team_members", indexes = {
        @Index(name = "idx_team_members_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_team_members_team_id", columnList = "team_id"),
        @Index(name = "idx_team_members_user_id", columnList = "user_id"),
        @Index(name = "idx_team_members_email", columnList = "email")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_team_members_tenant_user", columnNames = { "tenant_id", "user_id" })
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class TeamMemberModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role", length = 100)
    private String role;

    @Column(name = "status", nullable = false, length = 20)
    private String status;
}
