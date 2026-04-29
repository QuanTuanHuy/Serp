/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.second_mile.enums.HubStatus;
import serp.project.second_mile.enums.HubType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HubFilterRequest {
    private String keyword;
    private String code;
    private String name;
    private HubType hubType;
    private String provinceCode;
    private String wardCode;
    private HubStatus status;
    private Boolean hasLocation;

    private Integer minDailyCapacity;
    private Integer maxDailyCapacity;

    private Integer minCurrentLoad;
    private Integer maxCurrentLoad;
}
