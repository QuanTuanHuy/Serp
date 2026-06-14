/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import serp.project.account.core.domain.enums.SettingsGlobalSearchType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsGlobalSearchGroupResponse {
    private SettingsGlobalSearchType type;
    private String title;
    private Long total;
    private List<SettingsGlobalSearchItemResponse> items;
}
