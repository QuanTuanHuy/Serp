/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.specification;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private String field;
    private SearchOperation operation;
    private Object value;
    private List<Object> values;
    private Object fromValue;
    private Object toValue;
    
    public enum SearchOperation {
        EQUAL,
        NOT_EQUAL,
        LIKE,
        IN,
        NOT_IN,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        BETWEEN,
        IS_NULL,
        IS_NOT_NULL
    }
}
