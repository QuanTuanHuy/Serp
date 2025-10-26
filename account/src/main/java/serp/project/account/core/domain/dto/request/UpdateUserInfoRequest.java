/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import serp.project.account.core.domain.entity.UserEntity;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserInfoRequest {
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 512)
    private String avatarUrl;

    @Size(max = 64)
    private String timezone;

    @Size(max = 32)
    private String preferredLanguage;

    @Size(max = 64)
    private String keycloakUserId;

    public UserEntity toEntity() {
        return UserEntity.builder()
                .firstName(this.firstName)
                .lastName(this.lastName)
                .phoneNumber(this.phoneNumber)
                .avatarUrl(this.avatarUrl)
                .timezone(this.timezone)
                .preferredLanguage(this.preferredLanguage)
                .keycloakId(this.keycloakUserId)
                .build();
    }
}
