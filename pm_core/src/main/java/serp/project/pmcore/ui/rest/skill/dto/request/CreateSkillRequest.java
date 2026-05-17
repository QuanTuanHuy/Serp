/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.ui.rest.skill.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSkillRequest {
    @NotBlank
    @Size(max = 100)
    private String code;

    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;
}
