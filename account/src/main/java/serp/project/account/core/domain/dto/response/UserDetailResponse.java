package serp.project.account.core.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.account.core.domain.enums.UserStatus;
import serp.project.account.core.domain.enums.UserType;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDetailResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarUrl;
    private UserType userType;
    private UserStatus status;
    private Long lastLoginAt;
    private Long createdAt;
    private String timezone;
    private String preferredLanguage;
    private Long organizationId;
    private String organizationName;

    private List<RoleDetail> roles;
    private List<DepartmentDetail> departments;
    private List<ModuleAccessDetail> moduleAccesses;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class RoleDetail {
        private Long id;
        private String name;
        private String scope;
        private String description;
        private String moduleName;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class DepartmentDetail {
        private Long id;
        private String name;
        private Boolean isPrimary;
        private String jobTitle;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Builder
    public static class ModuleAccessDetail {
        private Long moduleId;
        private String moduleName;
        private String moduleCode;
        private Boolean isActive;
        private Long grantedAt;
    }
}
