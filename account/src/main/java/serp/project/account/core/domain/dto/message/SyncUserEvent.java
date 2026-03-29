/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncUserEvent {
    private Long userId;
    private Long organizationId;
    @JsonProperty("tid")
    private Long tenantId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private String roleName;
}
