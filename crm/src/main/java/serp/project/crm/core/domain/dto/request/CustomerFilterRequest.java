/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.enums.ActiveStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerFilterRequest {

    @Builder.Default
    private Integer page = 1;

    @Builder.Default
    private Integer size = 20;

    private String sortBy;

    @Builder.Default
    private String sortDirection = "DESC";

    private String keyword;
    private List<ActiveStatus> statuses;
    private List<String> industries;
    private List<String> companySizes;
    private Long parentCustomerId;
    private Boolean noParentOnly;

    private BigDecimal creditLimitMin;
    private BigDecimal creditLimitMax;

    private BigDecimal totalRevenueMin;
    private BigDecimal totalRevenueMax;

    private Integer totalOpportunitiesMin;
    private Integer totalOpportunitiesMax;

    private Integer wonOpportunitiesMin;
    private Integer wonOpportunitiesMax;

    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;

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
