/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.usecase.user.export;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.account.core.domain.dto.response.UserProfileResponse;
import serp.project.account.core.service.IUserService;
import serp.project.account.core.usecase.user.query.UserProfileAssembler;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserExportService {
    private final IUserService userService;
    private final UserProfileAssembler userProfileAssembler;

    public Object exportUsers(Long organizationId, String format) {
        var userProfiles = userProfileAssembler.assembleBasic(userService.getUsersByOrganizationId(organizationId));
        if ("csv".equalsIgnoreCase(format)) {
            return buildCsv(userProfiles);
        }
        return userProfiles;
    }

    private String buildCsv(List<UserProfileResponse> userProfiles) {
        var builder = new StringBuilder();
        builder.append("ID,Email,First Name,Last Name,User Type,Status,Roles,Last Login,Created At\n");

        for (var profile : userProfiles) {
            builder.append(profile.getId()).append(",");
            builder.append(escapeCsv(profile.getEmail())).append(",");
            builder.append(escapeCsv(profile.getFirstName())).append(",");
            builder.append(escapeCsv(profile.getLastName())).append(",");
            builder.append(profile.getUserType()).append(",");
            builder.append(profile.getStatus()).append(",");
            builder.append(escapeCsv(profile.getRoles() != null ? String.join(";", profile.getRoles()) : ""))
                    .append(",");
            builder.append(profile.getLastLoginAt() != null ? profile.getLastLoginAt() : "").append(",");
            builder.append(profile.getCreatedAt() != null ? profile.getCreatedAt() : "").append("\n");
        }

        return builder.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
