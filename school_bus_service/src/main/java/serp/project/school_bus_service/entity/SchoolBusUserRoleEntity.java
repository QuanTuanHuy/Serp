package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "school_bus_user_role")
@Getter
@Setter
public class SchoolBusUserRoleEntity extends BaseModel {

    @Column(name = "user_id", nullable = false)
    private Long schoolBusUserId;

    @Column(name = "role_name", nullable = false, length = 100)
    private String roleName;
}
