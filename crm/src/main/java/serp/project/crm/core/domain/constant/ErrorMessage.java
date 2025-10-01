/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.crm.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorMessage {
    // HTTP Status Messages
    public static final String OK = "OK";
    public static final String NOT_FOUND = "Not Found";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String FORBIDDEN = "Forbidden";
    public static final String BAD_REQUEST = "Bad Request";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String CONFLICT = "Conflict";
    public static final String TOO_MANY_REQUESTS = "Too Many Requests";
    public static final String UNKNOWN_ERROR = "Unknown Error";
    
    // ==================== CRM SPECIFIC ERROR MESSAGES ====================
    
    // Customer errors
    public static final String CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String CUSTOMER_ALREADY_EXISTS = "Customer with this email already exists";
    public static final String CUSTOMER_INACTIVE = "Customer is inactive";
    public static final String CUSTOMER_HAS_ACTIVE_OPPORTUNITIES = "Cannot delete customer with active opportunities";
    
    // Lead errors
    public static final String LEAD_NOT_FOUND = "Lead not found";
    public static final String LEAD_ALREADY_CONVERTED = "Lead has already been converted";
    public static final String LEAD_NOT_QUALIFIED = "Lead is not qualified for conversion";
    public static final String LEAD_ALREADY_QUALIFIED = "Lead is already qualified";
    public static final String LEAD_ALREADY_DISQUALIFIED = "Lead is already disqualified";
    public static final String LEAD_INVALID_STATUS = "Invalid lead status";
    
    // Opportunity errors
    public static final String OPPORTUNITY_NOT_FOUND = "Opportunity not found";
    public static final String OPPORTUNITY_ALREADY_CLOSED = "Opportunity is already closed";
    public static final String OPPORTUNITY_INVALID_STAGE = "Invalid opportunity stage";
    public static final String OPPORTUNITY_INVALID_STAGE_TRANSITION = "Cannot transition to this stage from current stage";
    public static final String OPPORTUNITY_MISSING_REQUIRED_FIELDS = "Missing required fields for opportunity";
    
    // Contact errors
    public static final String CONTACT_NOT_FOUND = "Contact not found";
    public static final String CONTACT_ALREADY_EXISTS = "Contact with this email already exists for this customer";
    public static final String CONTACT_CUSTOMER_REQUIRED = "Customer ID is required for contact";
    public static final String CONTACT_ALREADY_PRIMARY = "A primary contact already exists for this customer";
    
    // Activity errors
    public static final String ACTIVITY_NOT_FOUND = "Activity not found";
    public static final String ACTIVITY_ALREADY_COMPLETED = "Activity is already completed";
    public static final String ACTIVITY_ALREADY_CANCELLED = "Activity is already cancelled";
    public static final String ACTIVITY_INVALID_STATUS = "Invalid activity status";
    public static final String ACTIVITY_MISSING_ENTITY_REFERENCE = "Activity must be linked to a lead, customer, or opportunity";
    
    // Team errors
    public static final String TEAM_NOT_FOUND = "Team not found";
    public static final String TEAM_MEMBER_NOT_FOUND = "Team member not found";
    public static final String TEAM_MEMBER_ALREADY_EXISTS = "User is already a member of this team";
    public static final String TEAM_LEADER_NOT_FOUND = "Team leader not found";
    
    // Validation errors
    public static final String INVALID_EMAIL_FORMAT = "Invalid email format";
    public static final String INVALID_PHONE_FORMAT = "Invalid phone format";
    public static final String INVALID_DATE_RANGE = "Invalid date range";
    public static final String INVALID_PROBABILITY_VALUE = "Probability must be between 0 and 100";
    public static final String INVALID_AMOUNT_VALUE = "Amount must be greater than zero";
    public static final String REQUIRED_FIELD_MISSING = "Required field is missing: %s";
    
    // Permission errors
    public static final String NO_PERMISSION_TO_ACCESS = "You don't have permission to access this resource";
    public static final String NO_PERMISSION_TO_MODIFY = "You don't have permission to modify this resource";
    public static final String NO_PERMISSION_TO_DELETE = "You don't have permission to delete this resource";
    
    // Tenant errors
    public static final String TENANT_MISMATCH = "Resource does not belong to your tenant";
    public static final String TENANT_NOT_FOUND = "Tenant not found";

}
