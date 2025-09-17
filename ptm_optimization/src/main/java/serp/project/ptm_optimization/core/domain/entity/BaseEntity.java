/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.ptm_optimization.core.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class BaseEntity {
    private Long id;
    private Long createdAt;
    private Long updatedAt;
}
