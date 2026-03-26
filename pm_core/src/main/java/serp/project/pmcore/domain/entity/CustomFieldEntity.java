/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CustomFieldEntity extends BaseEntity {
    private String fieldKey;
    private String name;
    private String description;
    private String typeKey;
    private String searchTemplate;
    private Boolean isSystem;
    private String schemaJson;
}
