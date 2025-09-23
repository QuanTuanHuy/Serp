/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.crm.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GeneralResponse<T> {
    private String status;
    private Integer code;
    private String message;
    private T data;
}
