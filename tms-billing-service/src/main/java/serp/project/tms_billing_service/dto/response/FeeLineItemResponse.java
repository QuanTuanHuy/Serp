/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FeeLineItemResponse {
    String code;
    String name;
    String category;
    Long amount;
}
