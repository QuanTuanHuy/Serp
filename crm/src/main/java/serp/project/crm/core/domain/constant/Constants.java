/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final String SERVICE_NAME = "crm";
    
    @UtilityClass
    public static class ServiceNames {
        public static final String ACCOUNT_SERVICE = "account-service";
    }


    @UtilityClass
    public static class HttpStatus {
        public static final String ERROR = "error";
        public static final String SUCCESS = "success";
    }

    @UtilityClass
    public static class HttpStatusCode {
        public static final int SUCCESS = 200;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    @UtilityClass
    public static class Security {
        public static final String ROLE_PREFIX = "ROLE_";
        public static final String SERP_SERVICES_ROLE = "SERP_SERVICES";
    }

    // ==================== CRM SPECIFIC CONSTANTS ====================

    @UtilityClass
    public static class Validation {
        // String lengths
        public static final int MAX_NAME_LENGTH = 255;
        public static final int MAX_EMAIL_LENGTH = 255;
        public static final int MAX_PHONE_LENGTH = 50;
        public static final int MAX_WEBSITE_LENGTH = 255;
        public static final int MAX_NOTES_LENGTH = 5000;
        public static final int MAX_DESCRIPTION_LENGTH = 2000;

        // Number ranges
        public static final int MIN_PROBABILITY = 0;
        public static final int MAX_PROBABILITY = 100;
        public static final int MIN_PROGRESS_PERCENT = 0;
        public static final int MAX_PROGRESS_PERCENT = 100;

        // Email regex
        public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

        // Phone regex (basic international format)
        public static final String PHONE_REGEX = "^[+]?[0-9]{10,15}$";
    }

    @UtilityClass
    public static class Pagination {
        public static final int DEFAULT_PAGE = 1;
        public static final int DEFAULT_SIZE = 20;
        public static final int MAX_SIZE = 100;
        public static final int MIN_SIZE = 1;
    }

    @UtilityClass
    public static class CacheKey {
        public static final String CUSTOMER_PREFIX = "crm:customer:";
        public static final String LEAD_PREFIX = "crm:lead:";
        public static final String OPPORTUNITY_PREFIX = "crm:opportunity:";
        public static final String CONTACT_PREFIX = "crm:contact:";
        public static final String ACTIVITY_PREFIX = "crm:activity:";
        public static final String TEAM_PREFIX = "crm:team:";

        public static final long DEFAULT_TTL = 3600;
    }

    @UtilityClass
    public static class KafkaTopic {
        public static final String CUSTOMER = "crm.customer";
        public static final String LEAD = "crm.lead";
        public static final String OPPORTUNITY = "crm.opportunity";
        public static final String CONTACT = "crm.contact";
        public static final String ACTIVITY = "crm.activity";
        public static final String TEAM = "crm.team";

        public static final String CUSTOMER_DLQ = "crm.customer.dlq";
        public static final String LEAD_DLQ = "crm.lead.dlq";
        public static final String OPPORTUNITY_DLQ = "crm.opportunity.dlq";
    }

    @UtilityClass
    public static class KafkaCommand {
        // Customer commands
        public static final String CUSTOMER_CREATED = "CUSTOMER_CREATED";
        public static final String CUSTOMER_UPDATED = "CUSTOMER_UPDATED";
        public static final String CUSTOMER_DELETED = "CUSTOMER_DELETED";
        public static final String CUSTOMER_ACTIVATED = "CUSTOMER_ACTIVATED";
        public static final String CUSTOMER_DEACTIVATED = "CUSTOMER_DEACTIVATED";

        // Lead commands
        public static final String LEAD_CREATED = "LEAD_CREATED";
        public static final String LEAD_UPDATED = "LEAD_UPDATED";
        public static final String LEAD_DELETED = "LEAD_DELETED";
        public static final String LEAD_QUALIFIED = "LEAD_QUALIFIED";
        public static final String LEAD_DISQUALIFIED = "LEAD_DISQUALIFIED";
        public static final String LEAD_CONVERTED = "LEAD_CONVERTED";

        // Opportunity commands
        public static final String OPPORTUNITY_CREATED = "OPPORTUNITY_CREATED";
        public static final String OPPORTUNITY_UPDATED = "OPPORTUNITY_UPDATED";
        public static final String OPPORTUNITY_DELETED = "OPPORTUNITY_DELETED";
        public static final String OPPORTUNITY_STAGE_CHANGED = "OPPORTUNITY_STAGE_CHANGED";
        public static final String OPPORTUNITY_WON = "OPPORTUNITY_WON";
        public static final String OPPORTUNITY_LOST = "OPPORTUNITY_LOST";

        // Contact commands
        public static final String CONTACT_CREATED = "CONTACT_CREATED";
        public static final String CONTACT_UPDATED = "CONTACT_UPDATED";
        public static final String CONTACT_DELETED = "CONTACT_DELETED";

        // Activity commands
        public static final String ACTIVITY_CREATED = "ACTIVITY_CREATED";
        public static final String ACTIVITY_UPDATED = "ACTIVITY_UPDATED";
        public static final String ACTIVITY_DELETED = "ACTIVITY_DELETED";
        public static final String ACTIVITY_COMPLETED = "ACTIVITY_COMPLETED";
        public static final String ACTIVITY_CANCELLED = "ACTIVITY_CANCELLED";
        public static final String ACTIVITY_OVERDUE = "ACTIVITY_OVERDUE";

        // Team commands
        public static final String TEAM_CREATED = "TEAM_CREATED";
        public static final String TEAM_UPDATED = "TEAM_UPDATED";
        public static final String TEAM_DELETED = "TEAM_DELETED";
        public static final String TEAM_MEMBER_ADDED = "TEAM_MEMBER_ADDED";
        public static final String TEAM_MEMBER_REMOVED = "TEAM_MEMBER_REMOVED";
    }

    @UtilityClass
    public static class KafkaErrorCode {
        public static final String SUCCESS = "00";
        public static final String FAILURE = "01";
        public static final String VALIDATION_ERROR = "02";
        public static final String NOT_FOUND = "03";
        public static final String DUPLICATE = "04";
    }

    @UtilityClass
    public static class DateFormat {
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String TIME_FORMAT = "HH:mm:ss";
    }

    @UtilityClass
    public static class SuccessMessage {
        // Customer messages
        public static final String CUSTOMER_CREATED = "Customer created successfully";
        public static final String CUSTOMER_UPDATED = "Customer updated successfully";
        public static final String CUSTOMER_DELETED = "Customer deleted successfully";
        public static final String CUSTOMER_ACTIVATED = "Customer activated successfully";
        public static final String CUSTOMER_DEACTIVATED = "Customer deactivated successfully";

        // Lead messages
        public static final String LEAD_CREATED = "Lead created successfully";
        public static final String LEAD_UPDATED = "Lead updated successfully";
        public static final String LEAD_DELETED = "Lead deleted successfully";
        public static final String LEAD_QUALIFIED = "Lead qualified successfully";
        public static final String LEAD_DISQUALIFIED = "Lead disqualified successfully";
        public static final String LEAD_CONVERTED = "Lead converted successfully";

        // Opportunity messages
        public static final String OPPORTUNITY_CREATED = "Opportunity created successfully";
        public static final String OPPORTUNITY_UPDATED = "Opportunity updated successfully";
        public static final String OPPORTUNITY_DELETED = "Opportunity deleted successfully";
        public static final String OPPORTUNITY_STAGE_UPDATED = "Opportunity stage updated successfully";
        public static final String OPPORTUNITY_CLOSED_WON = "Opportunity closed as won";
        public static final String OPPORTUNITY_CLOSED_LOST = "Opportunity closed as lost";

        // Contact messages
        public static final String CONTACT_CREATED = "Contact created successfully";
        public static final String CONTACT_UPDATED = "Contact updated successfully";
        public static final String CONTACT_DELETED = "Contact deleted successfully";

        // Activity messages
        public static final String ACTIVITY_CREATED = "Activity created successfully";
        public static final String ACTIVITY_UPDATED = "Activity updated successfully";
        public static final String ACTIVITY_DELETED = "Activity deleted successfully";
        public static final String ACTIVITY_COMPLETED = "Activity completed successfully";

        // Team messages
        public static final String TEAM_CREATED = "Team created successfully";
        public static final String TEAM_UPDATED = "Team updated successfully";
        public static final String TEAM_DELETED = "Team deleted successfully";
        public static final String TEAM_MEMBER_ADDED = "Team member added successfully";
        public static final String TEAM_MEMBER_REMOVED = "Team member removed successfully";

        // General
        public static final String OPERATION_SUCCESSFUL = "Operation completed successfully";
    }

}
