/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.AccountTier;
import serp.project.crm.core.domain.enums.AccountType;
import serp.project.crm.core.domain.enums.PreferredTimeSlot;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {
    private Long id;
    
    private String name;
    private String industry;
    private String companySize;
    private String website;
    private String phone;
    private String email;
    
    private AddressResponse address;
    
    private String taxId;
    private BigDecimal creditLimit;
    private String paymentTerms;
    private ActiveStatus activeStatus;
    private AccountType accountType;
    private AccountTier tier;
    private List<PreferredTimeSlot> preferredTimeSlots;
    private List<DayOfWeek> preferredDays;
    private String language;
    private String timezone;
    
    private BigDecimal totalRevenue;
    private Integer totalOpportunities;
    private Integer wonOpportunities;
    
    private String notes;
    
    // Metadata
    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
