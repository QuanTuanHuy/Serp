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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.enums.LeadSource;
import serp.project.crm.core.domain.enums.LeadStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadFilterRequest {

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = 20;

    private String sortBy;

    @Builder.Default
    private String sortDirection = "DESC";

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

    public void normalize() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 20;
        }
        if (size > 200) {
            size = 200;
        }
        if (sortDirection == null ||
                (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection))) {
            sortDirection = "DESC";
        }
    }

    public PageRequest toPageRequest() {
        normalize();
        return PageRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .build();
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
