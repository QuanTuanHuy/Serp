package serp.project.school_bus_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "school_bus_code_sequence",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_school_bus_code_sequence_tenant_key",
                columnNames = {"tenant_id", "sequence_key"}))
@Getter
@Setter
public class CodeSequenceEntity extends BaseModel {

    @Column(name = "sequence_key", nullable = false, length = 50)
    private String sequenceKey;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;
}
