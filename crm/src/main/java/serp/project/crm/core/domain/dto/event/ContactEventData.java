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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEventData {
    private Long contactId;
    private Long tenantId;
    private Long customerId;
    private String name;
    private String email;
    private String phone;
    private String jobPosition;
    private AddressEntity address;
    private String contactType;
    private Boolean isPrimary;
    private String linkedInUrl;
    private String twitterHandle;
    private String activeStatus;
    private String notes;
    private Long createdBy;
    private Long createdAt;
    private Long updatedBy;
    private Long updatedAt;
}
