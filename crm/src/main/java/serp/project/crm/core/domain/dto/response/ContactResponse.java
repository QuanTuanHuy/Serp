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
import serp.project.crm.core.domain.enums.ContactType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ContactResponse {
    private Long id;
    
    private String name;
    private String email;
    private String phone;
    private String jobPosition;
    
    private Long customerId;
    private Boolean isPrimary;
    
    private AddressResponse address;
    private ContactType contactType;
    private ActiveStatus activeStatus;
    
    private String linkedInUrl;
    private String twitterHandle;
    private String notes;
    
    // Metadata
    private Long tenantId;
    private Long createdAt;
    private Long updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
