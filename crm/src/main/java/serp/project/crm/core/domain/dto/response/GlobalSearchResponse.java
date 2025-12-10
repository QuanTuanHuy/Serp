/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.crm.core.domain.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalSearchResponse {

    private GlobalSearchSection<LeadResponse> leads;
    private GlobalSearchSection<OpportunityResponse> opportunities;
    private GlobalSearchSection<CustomerResponse> customers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GlobalSearchSection<T> {
        private List<T> items;
        private Long total;
    }
}
