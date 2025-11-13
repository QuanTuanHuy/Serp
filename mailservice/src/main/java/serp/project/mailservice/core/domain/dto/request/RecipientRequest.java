/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientRequest {
    @NotBlank
    @Email
    private String email;

    private String name;
    private Map<String, Object> variables;
}
