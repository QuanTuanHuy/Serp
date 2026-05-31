/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class UpdateOrganizationSettingsRequest {
    @Size(max = 255)
    private String name;

    @Email
    private String email;

    @Size(max = 50)
    private String phoneNumber;

    @Size(max = 500)
    private String website;

    @Size(max = 500)
    private String address;

    @Size(max = 255)
    private String city;

    @Size(max = 255)
    private String state;

    @Size(max = 255)
    private String country;

    @Size(max = 50)
    private String zipCode;

    @Size(max = 100)
    private String taxId;

    @Size(max = 255)
    private String industry;

    @Min(0)
    private Integer employeeCount;

    @Size(max = 1000)
    private String description;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 500)
    private String faviconUrl;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "primaryColor must be a hex color")
    private String primaryColor;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "secondaryColor must be a hex color")
    private String secondaryColor;

    @Size(max = 100)
    private String timezone;

    @Pattern(regexp = "^(MM/DD/YYYY|DD/MM/YYYY|YYYY-MM-DD)$", message = "dateFormat is invalid")
    private String dateFormat;

    @Pattern(regexp = "^(12h|24h)$", message = "timeFormat is invalid")
    private String timeFormat;

    @Pattern(regexp = "^(sunday|monday)$", message = "weekStartsOn is invalid")
    private String weekStartsOn;

    @Size(max = 10)
    private String currency;

    @Size(max = 10)
    private String language;
}
