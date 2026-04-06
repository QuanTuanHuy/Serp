package serp.project.school_bus_service.infrastructure.store.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "school_bus_request_student")
@Getter
@Setter
public class RequestStudentEntity extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "request_id")
    private TransportRequestEntity request;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @ManyToOne
    @JoinColumn(name = "pickup_point_id")
    private PickupPointEntity pickupPoint;
}
