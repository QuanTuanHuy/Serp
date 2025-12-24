/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class LeadFilterRequest extends BaseFilterRequest {

    private String keyword;
    private List<LeadStatus> statuses;
    private List<LeadSource> sources;
    private List<String> industries;
    private Long assignedTo;
    private Boolean unassignedOnly;

    private BigDecimal estimatedValueMin;
    private BigDecimal estimatedValueMax;

    private Integer probabilityMin;
    private Integer probabilityMax;

    private LocalDate expectedCloseDateFrom;
    private LocalDate expectedCloseDateTo;

    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;

    private Boolean qualifiedOnly;
    private Boolean convertedOnly;

    private String country;
    private String city;

    private Boolean hasEmail;
    private Boolean hasPhone;

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
