/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.customfield.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CustomFieldContextDefaultValueEntity extends BaseEntity {
    private Long contextId;
    private String valueType;
    private String textValue;
    private BigDecimal numberValue;
    private Long dateValue;
    private Long datetimeValue;
    private Long userValueId;
    private String groupValueId;
    private Long optionValueId;
    private String jsonValue;
    private Integer sortOrder;
}
