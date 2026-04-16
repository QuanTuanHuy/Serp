package serp.project.pmcore.domain.shared.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(DomainErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(DomainErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public static ResourceNotFoundException project(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.PROJECT_NOT_FOUND,
                "Project not found: id=" + id);
    }

    public static ResourceNotFoundException projectByKey(String key) {
        return new ResourceNotFoundException(DomainErrorCode.PROJECT_NOT_FOUND,
                "Project not found: key=" + key);
    }

    public static ResourceNotFoundException workItem(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.WORK_ITEM_NOT_FOUND,
                "Work item not found: id=" + id);
    }

    public static ResourceNotFoundException workItemByKey(String key) {
        return new ResourceNotFoundException(DomainErrorCode.WORK_ITEM_NOT_FOUND,
                "Work item not found: key=" + key);
    }

    public static ResourceNotFoundException scheme(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.SCHEME_NOT_FOUND,
                "Scheme not found: id=" + id);
    }

    public static ResourceNotFoundException workflow(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.WORKFLOW_NOT_FOUND,
                "Workflow not found: id=" + id);
    }

    public static ResourceNotFoundException workflowStep(Long workflowId) {
        return new ResourceNotFoundException(DomainErrorCode.WORKFLOW_STEP_NOT_FOUND,
                "Initial step not found for workflow: id=" + workflowId);
    }

    public static ResourceNotFoundException issueType(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.ISSUE_TYPE_NOT_FOUND,
                "Issue type not found: id=" + id);
    }

    public static ResourceNotFoundException priority(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.PRIORITY_NOT_FOUND,
                "Priority not found: id=" + id);
    }

    public static ResourceNotFoundException status(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.STATUS_NOT_FOUND,
                "Status not found: id=" + id);
    }

    public static ResourceNotFoundException sprint(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.SPRINT_NOT_FOUND,
                "Sprint not found: id=" + id);
    }

    public static ResourceNotFoundException version(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.VERSION_NOT_FOUND,
                "Version not found: id=" + id);
    }

    public static ResourceNotFoundException component(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.COMPONENT_NOT_FOUND,
                "Component not found: id=" + id);
    }

    public static ResourceNotFoundException user(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.USER_NOT_FOUND,
                "User not found: id=" + id);
    }

    public static ResourceNotFoundException blueprint(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.BLUEPRINT_NOT_FOUND,
                "Blueprint not found: id=" + id);
    }

    public static ResourceNotFoundException category(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.CATEGORY_NOT_FOUND,
                "Category not found: id=" + id);
    }

    public static ResourceNotFoundException role(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.ROLE_NOT_FOUND,
                "Project role not found: id=" + id);
    }

    public static ResourceNotFoundException worklog(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.WORKLOG_NOT_FOUND,
                "Worklog not found: id=" + id);
    }

    public static ResourceNotFoundException issueLink(Long id) {
        return new ResourceNotFoundException(DomainErrorCode.ISSUE_LINK_NOT_FOUND,
                "Issue link not found: id=" + id);
    }
}
