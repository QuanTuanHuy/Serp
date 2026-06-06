/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.dto.request;

import lombok.Data;

@Data
public class SpecialCargoRequest {
    private boolean importantDocument;
    private boolean fragile;
    private boolean liquid;
}
