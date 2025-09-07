/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user_groups", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class UserGroupModel extends BaseModel {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;
}
