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
import serp.project.crm.core.domain.enums.ContactType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateContactRequest {
    private String name;
    private String email;
    private String phone;
    private String jobPosition;
    
    private Long customerId;
    private Boolean isPrimary;
    
    // Address
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    private ContactType contactType;
    private ActiveStatus activeStatus;
    
    private String linkedInUrl;
    private String twitterHandle;
    private String notes;
}
