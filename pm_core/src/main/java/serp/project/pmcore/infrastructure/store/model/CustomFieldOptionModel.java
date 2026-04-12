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

@Entity
@Table(name = "custom_field_options")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class CustomFieldOptionModel extends BaseModel {

    @Column(name = "custom_field_context_id", nullable = false)
    private Long customFieldContextId;

    @Column(name = "option_key", nullable = false)
    private String optionKey;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "sequence")
    private Integer sequence;

    @Column(name = "parent_option_id")
    private Long parentOptionId;

    @Column(name = "is_disabled")
    private Boolean isDisabled;
}
