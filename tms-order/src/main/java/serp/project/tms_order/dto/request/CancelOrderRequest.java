/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {
    @JsonProperty("cancel_reason")
    @JsonAlias("cancelReason")
    private String cancelReason;
}
