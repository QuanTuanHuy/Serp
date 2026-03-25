/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.exception;

import lombok.Getter;

/**
 * Central catalog of PM Core domain failures.
 *
 * <p>Codes are grouped by bounded context so related business rules stay close
 * together and new errors can be added without mixing concerns.
 */
@Getter
public enum DomainErrorCode {

    // ----------------------------------------------------------------
    // Shared Context & Cross-cutting Constraints
    // ----------------------------------------------------------------

    // Request / tenant context
    USER_NOT_FOUND("User not found"),
    TENANT_NOT_FOUND("Tenant not found"),

    // Concurrency / batch limits
    CONCURRENT_MODIFICATION("Resource was modified by another request — please retry"),
    BULK_LIMIT_EXCEEDED("Bulk operations are limited to 100 items per request"),

    // ----------------------------------------------------------------
    // Project Setup, Structure & Provisioning
    // ----------------------------------------------------------------

    // Project lifecycle & creation rules
    PROJECT_NOT_FOUND("Project not found"),
    PROJECT_KEY_ALREADY_EXISTS("Project key already exists in this tenant"),
    PROJECT_KEY_INVALID_FORMAT("Project key must be 2-10 uppercase alphanumeric chars starting with a letter"),
    PROJECT_TYPE_INVALID("Project type is not valid"),
    PROJECT_ARCHIVED("Cannot modify an archived project"),
    PROJECT_ALREADY_ARCHIVED("Project is already archived"),
    PROJECT_NOT_ARCHIVED("Project is not archived"),
    INVALID_PROVISIONING_MODE("Provisioning mode must be TEMPLATE_DEFAULT or SHARED_FROM_EXISTING"),
    BLUEPRINT_PROJECT_TYPE_MISMATCH("Blueprint project type does not match the requested project type"),

    // Project taxonomy & templates
    CATEGORY_NOT_FOUND("Project category not found"),
    CATEGORY_NAME_ALREADY_EXISTS("Category name already exists in this tenant"),
    CATEGORY_IN_USE("Cannot delete category that has projects assigned"),
    BLUEPRINT_NOT_FOUND("Project blueprint not found"),
    BLUEPRINT_IS_SYSTEM("Cannot modify a system blueprint"),

    // Project configuration catalogs
    COMPONENT_NOT_FOUND("Project component not found"),
    COMPONENT_NAME_ALREADY_EXISTS("Component name already exists in this project"),
    VERSION_NOT_FOUND("Project version not found"),
    VERSION_NAME_ALREADY_EXISTS("Version name already exists in this project"),
    VERSION_ALREADY_RELEASED("Version is already released"),
    VERSION_NOT_RELEASED("Version has not been released yet"),
    VERSION_ALREADY_ARCHIVED("Version is already archived"),

    // Project roles
    ROLE_NOT_FOUND("Project role not found"),
    ROLE_NAME_ALREADY_EXISTS("Role name already exists in this tenant"),
    ROLE_IS_SYSTEM("Cannot modify a system role"),
    ROLE_IN_USE_BY_PERMISSION("Cannot delete role that is used in permission schemes"),
    ROLE_ACTOR_NOT_FOUND("Project role actor not found"),
    ROLE_ACTOR_ALREADY_ASSIGNED("Actor is already assigned to this role in the project"),

    // Shared scheme provisioning
    SCHEME_NOT_FOUND("Scheme not found"),
    SCHEME_INCOMPATIBLE("Scheme is not compatible with the current configuration"),
    SCHEME_PROVISIONING_FAILED("Failed to provision project schemes"),

    // ----------------------------------------------------------------
    // Work Items, Taxonomy & Tracking
    // ----------------------------------------------------------------

    // Work item existence & hierarchy
    WORK_ITEM_NOT_FOUND("Work item not found"),
    INVALID_PARENT_HIERARCHY("Invalid parent-child hierarchy: child level must be lower than parent level"),
    PARENT_NOT_IN_SAME_PROJECT("Parent work item must belong to the same project"),

    // Issue types & issue type schemes
    ISSUE_TYPE_NOT_FOUND("Issue type not found"),
    ISSUE_TYPE_NOT_IN_SCHEME("Issue type is not allowed in this project's scheme"),
    ISSUE_TYPE_KEY_ALREADY_EXISTS("Issue type key already exists in this tenant"),
    ISSUE_TYPE_IS_SYSTEM("Cannot modify a system issue type"),
    ISSUE_TYPE_IN_USE("Cannot delete issue type that has work items"),
    ISSUE_TYPE_SCHEME_NOT_FOUND("Issue type scheme not found"),
    ISSUE_TYPE_SCHEME_DEFAULT_NOT_IN_ITEMS(
            "Default issue type must be included in the scheme items"),
    ISSUE_TYPE_SCHEME_IN_USE("Cannot remove issue type from scheme: work items of this type exist"),

    // Priority management
    PRIORITY_NOT_FOUND("Priority not found"),
    PRIORITY_NOT_IN_SCHEME("Priority is not valid for this project's scheme"),
    DEFAULT_PRIORITY_NOT_CONFIGURED("Default priority is not configured for this project"),
    PRIORITY_IS_SYSTEM("Cannot modify a system priority"),
    PRIORITY_SCHEME_NOT_FOUND("Priority scheme not found"),
    PRIORITY_SCHEME_DEFAULT_NOT_IN_ITEMS(
            "Default priority must be included in the scheme items"),

    // Resolution rules
    RESOLUTION_NOT_FOUND("Resolution not found"),
    RESOLUTION_IS_SYSTEM("Cannot modify a system resolution"),
    RESOLUTION_REQUIRED("Resolution must be set before transitioning to done"),

    // Links, logs & counters
    SELF_LINK_NOT_ALLOWED("Cannot create a link from a work item to itself"),
    DUPLICATE_ISSUE_LINK("This link already exists between these work items"),
    ISSUE_LINK_TYPE_NOT_FOUND("Issue link type not found"),
    ISSUE_LINK_NOT_FOUND("Issue link not found"),
    WORKLOG_NOT_FOUND("Worklog not found"),
    WORKLOG_NOT_OWNER("Only the author or an admin can modify this worklog"),
    ISSUE_COUNTER_NOT_FOUND("Issue counter not found for this project"),

    // ----------------------------------------------------------------
    // Workflow, Status & Transition Engine
    // ----------------------------------------------------------------

    // Workflow lifecycle
    WORKFLOW_NOT_FOUND("Workflow not found"),
    WORKFLOW_IS_SYSTEM("Cannot modify a system workflow"),
    WORKFLOW_IN_USE("Cannot delete workflow that is referenced by a workflow scheme"),
    WORKFLOW_NOT_ACTIVE("Workflow has not been published/activated"),
    WORKFLOW_ALREADY_ACTIVE("Workflow is already active — create a new draft to edit"),
    CLONE_WORKFLOW_FAILED("Failed to clone workflow"),

    // Workflow structure validation
    WORKFLOW_STEP_NOT_FOUND("Workflow initial step not found"),
    WORKFLOW_STEP_DUPLICATE_STATUS("Status is already a step in this workflow"),
    WORKFLOW_NO_INITIAL_STEP("Workflow must have exactly one initial step"),
    WORKFLOW_MULTIPLE_INITIAL_STEPS("Workflow already has an initial step"),
    WORKFLOW_VALIDATION_FAILED("Workflow validation failed"),

    // Transition execution
    TRANSITION_NOT_FOUND("Workflow transition not found"),
    INVALID_TRANSITION("Transition is not valid from the current status"),
    TRANSITION_CONDITION_FAILED("Transition condition not met"),
    TRANSITION_VALIDATION_FAILED("Transition validator rejected the operation"),

    // Workflow schemes
    WORKFLOW_SCHEME_NOT_FOUND("Workflow scheme not found"),
    WORKFLOW_SCHEME_COVERAGE_MISSING("Workflow scheme does not cover all issue types"),

    // Status catalogs
    STATUS_NOT_FOUND("Status not found"),
    STATUS_KEY_ALREADY_EXISTS("Status key already exists in this tenant"),
    STATUS_IN_USE_BY_WORKFLOW("Cannot delete status that is used in a workflow step"),
    STATUS_IN_USE_BY_WORK_ITEMS("Cannot delete status that is assigned to work items"),
    STATUS_CATEGORY_NOT_FOUND("Status category not found"),
    STATUS_CATEGORY_KEY_ALREADY_EXISTS("Status category key already exists in this tenant"),
    STATUS_CATEGORY_IS_SYSTEM("Cannot modify a system status category"),

    // ----------------------------------------------------------------
    // Fields, Screens & Issue Presentation
    // ----------------------------------------------------------------

    // Custom fields & field configurations
    CUSTOM_FIELD_NOT_FOUND("Custom field not found"),
    CUSTOM_FIELD_TYPE_IMMUTABLE("Custom field type cannot be changed after creation"),
    FIELD_CONFIG_NOT_FOUND("Field configuration not found"),
    FIELD_CONFIG_SCHEME_NOT_FOUND("Field configuration scheme not found"),
    FIELD_CONFIG_SCHEME_COVERAGE_MISSING(
            "Field configuration scheme does not cover all issue types"),
    FIELD_CANNOT_BE_REQUIRED_AND_HIDDEN("A field cannot be both required and hidden"),
    SUMMARY_FIELD_CANNOT_BE_HIDDEN("The summary field cannot be hidden"),
    CLONE_FIELD_CONFIG_FAILED("Failed to clone field configuration"),

    // Screens & screen schemes
    SCREEN_NOT_FOUND("Screen not found"),
    SCREEN_MUST_HAVE_ONE_TAB("A screen must have at least one tab"),
    SCREEN_FIELD_DUPLICATE("A field can only appear once per screen"),
    SCREEN_SCHEME_NOT_FOUND("Screen scheme not found"),
    SCREEN_SCHEME_OPERATION_COVERAGE_MISSING(
            "Screen scheme must define CREATE, EDIT, and VIEW operations"),
    ISSUE_TYPE_SCREEN_SCHEME_NOT_FOUND("Issue type screen scheme not found"),
    ISSUE_TYPE_SCREEN_SCHEME_COVERAGE_MISSING(
            "Issue type screen scheme does not cover all issue types"),
    CLONE_SCREEN_SCHEME_FAILED("Failed to clone screen scheme"),

    // ----------------------------------------------------------------
    // Permissions, Notifications & Access Control
    // ----------------------------------------------------------------

    // Permission / notification schemes
    NOTIFICATION_SCHEME_NOT_FOUND("Notification scheme not found"),
    CLONE_NOTIFICATION_SCHEME_FAILED("Failed to clone notification scheme"),
    PERMISSION_SCHEME_NOT_FOUND("Permission scheme not found"),
    PERMISSION_KEY_NOT_FOUND("Permission definition not found"),
    PROJECT_PERMISSION_DENIED("Project permission denied"),
    DUPLICATE_PERMISSION_ENTRY("This permission grant already exists in the scheme"),

    // Issue security
    ISSUE_SECURITY_SCHEME_NOT_FOUND("Issue security scheme not found"),
    SECURITY_LEVEL_NOT_FOUND("Issue security level not found"),
    SECURITY_LEVEL_NOT_IN_SCHEME("Issue security level is not valid for this project's scheme"),
    SECURITY_LEVEL_IN_USE("Cannot delete security level that is assigned to work items"),
    SECURITY_LEVEL_DEFAULT_REQUIRED("Default security level must belong to the assigned scheme"),
    SECURITY_LEVEL_ACCESS_DENIED("Insufficient security level to access this work item"),
    DUPLICATE_SECURITY_MEMBER("This member already exists in the security level"),

    // ----------------------------------------------------------------
    // Agile Planning
    // ----------------------------------------------------------------

    // Sprint lifecycle
    SPRINT_NOT_FOUND("Sprint not found"),
    SPRINT_ALREADY_ACTIVE("Another sprint is already active on this board"),
    SPRINT_NOT_ACTIVE("Sprint is not currently active"),
    SPRINT_ALREADY_CLOSED("Sprint is already closed"),
    SPRINT_CANNOT_MOVE_TO_CLOSED("Cannot move work items to a closed sprint");

    private final String defaultMessage;

    DomainErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }
}
