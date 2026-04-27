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
    
    // Account errors
    public static final String ACCOUNT_NOT_FOUND = "Account not found";
    public static final String ACCOUNT_ALREADY_EXISTS = "Account with this email already exists";
    public static final String ACCOUNT_INACTIVE = "Account is inactive";
    public static final String ACCOUNT_ALREADY_ACTIVE = "Account is already active";
    public static final String ACCOUNT_ALREADY_INACTIVE = "Account is already inactive";
    public static final String ACCOUNT_HAS_ACTIVE_OPPORTUNITIES = "Cannot delete Account with active opportunities";
    public static final String ACCOUNT_CANNOT_BE_OWN_PARENT = "Account cannot be its own parent";
    public static final String CANNOT_DELETE_ACCOUNT_WITH_CHILDREN = "Cannot delete Account with child Accounts";
    public static final String CREDIT_LIMIT_NEGATIVE = "Credit limit cannot be negative";
    
    // Lead errors
    public static final String LEAD_NOT_FOUND = "Lead not found";
    public static final String LEAD_ALREADY_CONVERTED = "Lead has already been converted";
    public static final String LEAD_ALREADY_EXISTS = "Lead with email %s already exists";
    public static final String LEAD_NOT_QUALIFIED = "Lead is not qualified for conversion";
    public static final String LEAD_ALREADY_QUALIFIED = "Lead is already qualified";
    public static final String LEAD_ALREADY_DISQUALIFIED = "Lead is already disqualified";
    public static final String LEAD_INVALID_STATUS = "Invalid lead status";
    public static final String LEAD_CANNOT_BE_CONVERTED = "Lead cannot be converted in its current status";
    public static final String CANNOT_DELETE_CONVERTED_LEAD = "Cannot delete converted lead";
    
    // Opportunity errors
    public static final String OPPORTUNITY_NOT_FOUND = "Opportunity not found";
    public static final String OPPORTUNITY_ALREADY_CLOSED = "Opportunity is already closed";
    public static final String OPPORTUNITY_INVALID_STAGE = "Invalid opportunity stage";
    public static final String OPPORTUNITY_INVALID_STAGE_TRANSITION = "Cannot transition to this stage from current stage";
    public static final String OPPORTUNITY_MISSING_REQUIRED_FIELDS = "Missing required fields for opportunity";
    public static final String OPPORTUNITY_ALREADY_EXISTS = "Opportunity with this name already exists for the Account";
    public static final String CANNOT_DELETE_WON_OPPORTUNITY = "Cannot delete an opportunity that has been won";
    
    // Contact errors
    public static final String CONTACT_NOT_FOUND = "Contact not found";
    public static final String CONTACT_ALREADY_EXISTS = "Contact with this email already exists for this Account";
    public static final String CONTACT_ACCOUNT_REQUIRED = "Account ID is required for contact";
    public static final String CONTACT_ALREADY_PRIMARY = "A primary contact already exists for this Account";
    public static final String CONTACT_CANNOT_BE_PRIMARY = "Contact cannot be set as primary without an Account";
    
    // Activity errors
    public static final String ACTIVITY_DUE_DATE_PAST = "Due date cannot be in the past";
    public static final String ACTIVITY_NOT_FOUND = "Activity not found";
    public static final String ACTIVITY_ALREADY_COMPLETED = "Activity is already completed";
    public static final String ACTIVITY_ALREADY_CANCELLED = "Activity is already cancelled";
    public static final String ACTIVITY_INVALID_STATUS = "Invalid activity status";
    public static final String ACTIVITY_MISSING_ENTITY_REFERENCE = "Activity must be linked to at least one of lead, contact, Account, or opportunity";
    public static final String ACTIVITY_TYPE_REQUIRED = "Activity type is required";
    public static final String ACTIVITY_SUBJECT_REQUIRED = "Activity subject is required";
    public static final String ACTIVITY_PROGRESS_INVALID = "Activity progressPercent must be between 0 and 100";
    public static final String ACTIVITY_DURATION_INVALID = "Activity durationMinutes must be greater than zero";
    public static final String ACTIVITY_DUE_DATE_REQUIRED_FOR_TASK = "Due date is required for task activities";
    
    // Team errors
    public static final String TEAM_NOT_FOUND = "Team not found";
    public static final String TEAM_MEMBER_NOT_FOUND = "Team member not found";
    public static final String TEAM_MEMBER_ALREADY_EXISTS = "User is already a member of this team";
    public static final String TEAM_LEADER_NOT_FOUND = "Team leader not found";
    public static final String TEAM_MANAGER_NOT_FOUND = "Team manager not found";
    public static final String TEAM_NAME_ALREADY_EXISTS = "Team with this name already exists";
    public static final String TEAM_INACTIVE = "Team is inactive";
    public static final String TEAM_MEMBER_DOES_NOT_BELONG_TO_TEAM = "Team member does not belong to the specified team";
    public static final String AUTH_CONTEXT_REQUIRED = "Authentication context is required";
    public static final String TEAM_MANAGER_MUST_BELONG_TO_TEAM = "New manager must be an active member of the specified team";
    public static final String TEAM_MANAGER_ROLE_CHANGE_INVALID = "Previous manager role must be SALES_REP or VIEWER";
    public static final String TERRITORY_NOT_FOUND = "Territory not found";
    public static final String TERRITORY_ALREADY_ASSIGNED = "Territory already assigned to another active team";

    // Team member errors
    public static final String MEMBER_NOT_BELONG_TO_ORGANIZATION = "Member does not belong to the organization";
    public static final String MEMBER_ALREADY_IN_TEAM = "Member is already in one of your teams";
    public static final String MEMBER_ALREADY_IN_ANOTHER_TEAM = "Member is already in another team";
    public static final String MEMBER_IS_NOT_ACTIVE = "Member is not active";
    public static final String MEMBER_NOT_HAS_CRM_ROLE = "Member does not have permission to be assigned to CRM";
    public static final String MEMBER_ALREADY_ACTIVE = "Member is already active";
    public static final String MEMBER_ALREADY_INACTIVE = "Member is already inactive";
    public static final String INVALID_TEAM_MEMBER_ROLE = "Invalid team member role. Must be MANAGER, SALES_REP, or VIEWER";
    public static final String INVALID_TERRITORY_CODE = "One or more territory codes do not exist";
    
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
