/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilterCriteria {
    
    private String field;
    
    private FilterOperator operator;
    
    private Object value;
    
    private List<Object> values; // For IN, NOT_IN operators
    
    public enum FilterOperator {
        EQUALS,
        NOT_EQUALS,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        LIKE,
        NOT_LIKE,
        IN,
        NOT_IN,
        IS_NULL,
        IS_NOT_NULL,
        BETWEEN,
        STARTS_WITH,
        ENDS_WITH,
        CONTAINS
    }
    
    public boolean requiresValue() {
        return operator != FilterOperator.IS_NULL && operator != FilterOperator.IS_NOT_NULL;
    }
    
    public boolean requiresMultipleValues() {
        return operator == FilterOperator.IN || 
               operator == FilterOperator.NOT_IN || 
               operator == FilterOperator.BETWEEN;
    }
}
