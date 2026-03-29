/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncUserFirstMileEvent {
    private Long userId;
    private Long organizationId;
    @JsonProperty("tid")
    private Long tenantId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String fullName;
    private List<String> roleNames;
}
