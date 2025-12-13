/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.ActiveStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CustomerFilterRequest extends BaseFilterRequest {

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

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
}
