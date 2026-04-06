package serp.project.school_bus_service.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "school_bus_school")
@Getter
@Setter
public class SchoolEntity extends BaseModel {

    @Column(nullable = false)
    private String name;

    private String code;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

}
