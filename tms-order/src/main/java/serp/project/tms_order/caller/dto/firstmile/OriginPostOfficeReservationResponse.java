/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.caller.dto.firstmile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OriginPostOfficeReservationResponse {
    private Long id;
    private String code;
    private String name;
    private Integer currentLoad;
    private Integer dailyCapacity;
}
