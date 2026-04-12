/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import serp.project.first_mile.enums.PostOfficeStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostOfficeFilterRequest {
    private String keyword;
    private String code;
    private String name;
    private String provinceCode;
    private String wardCode;
    private PostOfficeStatus status;
    private Boolean hasLocation;

    private Integer minServiceRadiusM;
    private Integer maxServiceRadiusM;

    private Integer minDailyCapacity;
    private Integer maxDailyCapacity;

    private Integer minCurrentLoad;
    private Integer maxCurrentLoad;

    private Integer minPriority;
    private Integer maxPriority;
}
