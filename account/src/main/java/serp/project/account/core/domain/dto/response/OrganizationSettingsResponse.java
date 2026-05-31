/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

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
public class OrganizationSettingsResponse {
    private Long id;
    private String name;
    private String code;
    private String email;
    private String phoneNumber;
    private String website;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String taxId;
    private String industry;
    private Integer employeeCount;
    private String description;
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
    private String secondaryColor;
    private String timezone;
    private String dateFormat;
    private String timeFormat;
    private String weekStartsOn;
    private String currency;
    private String language;
    private Long createdAt;
    private Long updatedAt;
    private OrganizationSettingsSummaryResponse summary;
}
