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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "work_item_custom_field_values")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class WorkItemCustomFieldValueModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "work_item_id", nullable = false)
    private Long workItemId;

    @Column(name = "custom_field_id", nullable = false)
    private Long customFieldId;

    @Column(name = "custom_field_context_id", nullable = false)
    private Long customFieldContextId;

    @Column(name = "value_type", nullable = false)
    private String valueType;

    @Column(name = "text_value")
    private String textValue;

    @Column(name = "number_value")
    private BigDecimal numberValue;

    @Column(name = "date_value")
    private LocalDate dateValue;

    @Column(name = "datetime_value")
    private LocalDateTime datetimeValue;

    @Column(name = "user_value_id")
    private Long userValueId;

    @Column(name = "group_value_id")
    private String groupValueId;

    @Column(name = "option_value_id")
    private Long optionValueId;

    @Column(name = "json_value")
    private String jsonValue;

    @Column(name = "sort_order")
    private Integer sortOrder;
}
