/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.response.admin;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DeliveryServiceConfigAdminResponse {
    Long id;
    String serviceCode;
    String name;
    String description;
    Boolean active;
    Integer sortOrder;
}
