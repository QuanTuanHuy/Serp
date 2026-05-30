/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitiateOrderPaymentRequest {
    @Positive
    private Long amount;
}
