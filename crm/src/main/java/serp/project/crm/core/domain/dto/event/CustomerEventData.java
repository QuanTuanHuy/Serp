/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.entity.AddressEntity;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerEventData {
    private Long customerId;
    private Long tenantId;
    private String name;
    private String email;
    private String phone;
    private String website;
    private String industry;
    private String companySize;
    private Long parentCustomerId;
    private String taxId;
    private BigDecimal creditLimit;
    private Integer totalOpportunities;
    private Integer wonOpportunities;
    private BigDecimal totalRevenue;
    private String activeStatus;
    private String notes;
    private AddressEntity address;
    private Long createdBy;
    private Long createdAt;
    private Long updatedBy;
    private Long updatedAt;
}
