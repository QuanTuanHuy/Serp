/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.crm.core.domain.enums.ActiveStatus;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateCustomerRequest {
    private String name;
    private String industry;
    private String companySize;
    private String website;
    private String phone;
    private String email;
    
    // Address
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    // Business details
    private String taxId;
    private BigDecimal creditLimit;
    private String paymentTerms;
    private ActiveStatus activeStatus;
    private String notes;
}
