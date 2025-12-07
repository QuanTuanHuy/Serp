# Workflow Engine Components - Thiết Kế Chi Tiết

**Module:** CRM Workflow Automation - Core Components  
**Phiên bản:** 1.0  
**Ngày tạo:** 2025-12-07

---

## 1. Trigger System

### 1.1. Trigger Handler Interface

```java
package serp.project.crm.core.workflow.trigger;

import serp.project.crm.core.domain.dto.event.EntityChangeEvent;
import serp.project.crm.core.domain.entity.WorkflowRuleEntity;

public interface ITriggerHandler {
    boolean canHandle(WorkflowRuleEntity rule, EntityChangeEvent event);
    boolean shouldTrigger(WorkflowRuleEntity rule, EntityChangeEvent event);
    TriggerContext buildContext(WorkflowRuleEntity rule, EntityChangeEvent event);
}
```

### 1.2. TriggerContext

```java
@Data
@Builder
public class TriggerContext {
    private Long ruleId;
    private String entityType;
    private Long entityId;
    private String triggerType;
    private Map<String, Object> oldValues;
    private Map<String, Object> newValues;
    private Map<String, Object> changedFields;
    private Long tenantId;
    private Long triggeredBy;
    private LocalDateTime triggeredAt;
    private Map<String, Object> metadata;
}
```

### 1.3. TriggerHandlerRegistry

```java
@Component
public class TriggerHandlerRegistry {
    private final Map<TriggerType, ITriggerHandler> handlers = new EnumMap<>(TriggerType.class);
    
    public TriggerHandlerRegistry(List<ITriggerHandler> handlerList) {
        handlerList.forEach(h -> handlers.put(h.getSupportedType(), h));
    }
    
    public ITriggerHandler getHandler(TriggerType type) {
        return handlers.get(type);
    }
}
```

### 1.4. Trigger Implementations

```java
// OnCreateTriggerHandler
@Component
public class OnCreateTriggerHandler implements ITriggerHandler {
    @Override
    public boolean canHandle(WorkflowRuleEntity rule, EntityChangeEvent event) {
        return rule.getTriggerType() == TriggerType.ON_CREATE &&
               rule.getEntityType().equals(event.getEntityType()) &&
               "CREATE".equals(event.getChangeType());
    }
    
    @Override
    public boolean shouldTrigger(WorkflowRuleEntity rule, EntityChangeEvent event) {
        return canHandle(rule, event);
    }
    
    @Override
    public TriggerContext buildContext(WorkflowRuleEntity rule, EntityChangeEvent event) {
        return TriggerContext.builder()
            .ruleId(rule.getId())
            .entityType(event.getEntityType())
            .entityId(event.getEntityId())
            .triggerType(TriggerType.ON_CREATE.name())
            .newValues(event.getNewValues())
            .tenantId(event.getTenantId())
            .triggeredAt(LocalDateTime.now())
            .build();
    }
}

// OnFieldChangeTriggerHandler
@Component
public class OnFieldChangeTriggerHandler implements ITriggerHandler {
    @Override
    public boolean canHandle(WorkflowRuleEntity rule, EntityChangeEvent event) {
        return rule.getTriggerType() == TriggerType.ON_FIELD_CHANGE &&
               rule.getEntityType().equals(event.getEntityType()) &&
               "UPDATE".equals(event.getChangeType());
    }
    
    @Override
    public boolean shouldTrigger(WorkflowRuleEntity rule, EntityChangeEvent event) {
        if (!canHandle(rule, event)) return false;
        
        String watchField = rule.getTriggerField();
        if (watchField == null) return true;
        
        Object oldValue = event.getOldValues().get(watchField);
        Object newValue = event.getNewValues().get(watchField);
        return !Objects.equals(oldValue, newValue);
    }
    
    @Override
    public TriggerContext buildContext(WorkflowRuleEntity rule, EntityChangeEvent event) {
        Map<String, Object> changedFields = detectChangedFields(
            event.getOldValues(), event.getNewValues()
        );
        
        return TriggerContext.builder()
            .ruleId(rule.getId())
            .entityType(event.getEntityType())
            .entityId(event.getEntityId())
            .triggerType(TriggerType.ON_FIELD_CHANGE.name())
            .oldValues(event.getOldValues())
            .newValues(event.getNewValues())
            .changedFields(changedFields)
            .tenantId(event.getTenantId())
            .triggeredAt(LocalDateTime.now())
            .build();
    }
    
    private Map<String, Object> detectChangedFields(Map<String, Object> old, Map<String, Object> current) {
        Map<String, Object> changed = new HashMap<>();
        for (String key : current.keySet()) {
            if (!Objects.equals(old.get(key), current.get(key))) {
                changed.put(key, Map.of("old", old.get(key), "new", current.get(key)));
            }
        }
        return changed;
    }
}

// ScheduledTriggerHandler
@Component
public class ScheduledTriggerHandler {
    private final IWorkflowRuleStorePort ruleStore;
    private final WorkflowEngineService engineService;
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void processScheduledRules() {
        List<WorkflowRuleEntity> rules = ruleStore.findActiveScheduledRules();
        for (WorkflowRuleEntity rule : rules) {
            if (shouldRunNow(rule)) {
                List<Map<String, Object>> entities = findMatchingEntities(rule);
                for (Map<String, Object> entity : entities) {
                    EntityChangeEvent event = buildEventFromEntity(rule, entity);
                    engineService.processEvent(event);
                }
            }
        }
    }
    
    private boolean shouldRunNow(WorkflowRuleEntity rule) {
        CronExpression cron = CronExpression.parse(rule.getCronExpression());
        LocalDateTime lastRun = rule.getLastExecutedAt();
        LocalDateTime next = cron.next(lastRun != null ? lastRun : LocalDateTime.now().minusDays(1));
        return next != null && next.isBefore(LocalDateTime.now());
    }
}
```

---

## 2. Condition Evaluator System

### 2.1. ConditionEvaluator Interface

```java
public interface IConditionEvaluator {
    boolean evaluate(WorkflowConditionEntity condition, TriggerContext context);
    ConditionOperator getSupportedOperator();
}
```

### 2.2. ConditionEvaluatorRegistry

```java
@Component
public class ConditionEvaluatorRegistry {
    private final Map<ConditionOperator, IConditionEvaluator> evaluators;
    
    public ConditionEvaluatorRegistry() {
        evaluators = new EnumMap<>(ConditionOperator.class);
        evaluators.put(ConditionOperator.EQUALS, new EqualsEvaluator());
        evaluators.put(ConditionOperator.NOT_EQUALS, new NotEqualsEvaluator());
        evaluators.put(ConditionOperator.GREATER_THAN, new ComparisonEvaluator(">"));
        evaluators.put(ConditionOperator.LESS_THAN, new ComparisonEvaluator("<"));
        evaluators.put(ConditionOperator.CONTAINS, new ContainsEvaluator());
        evaluators.put(ConditionOperator.IN, new InListEvaluator());
        evaluators.put(ConditionOperator.IS_NULL, new NullCheckEvaluator(true));
        evaluators.put(ConditionOperator.IS_NOT_NULL, new NullCheckEvaluator(false));
        evaluators.put(ConditionOperator.CHANGED, new FieldChangedEvaluator());
        evaluators.put(ConditionOperator.CHANGED_TO, new ChangedToEvaluator());
        evaluators.put(ConditionOperator.BETWEEN, new BetweenEvaluator());
    }
    
    public IConditionEvaluator get(ConditionOperator op) {
        return evaluators.get(op);
    }
}
```

### 2.3. Condition Implementations

```java
// EqualsEvaluator
public class EqualsEvaluator implements IConditionEvaluator {
    @Override
    public boolean evaluate(WorkflowConditionEntity c, TriggerContext ctx) {
        Object actual = ctx.getNewValues().get(c.getFieldName());
        Object expected = parseValue(c.getValue(), c.getValueType());
        return Objects.equals(String.valueOf(actual), String.valueOf(expected));
    }
}

// ComparisonEvaluator
public class ComparisonEvaluator implements IConditionEvaluator {
    private final String operator;
    
    public ComparisonEvaluator(String operator) { this.operator = operator; }
    
    @Override
    public boolean evaluate(WorkflowConditionEntity c, TriggerContext ctx) {
        Object actual = ctx.getNewValues().get(c.getFieldName());
        if (actual == null) return false;
        
        BigDecimal a = new BigDecimal(actual.toString());
        BigDecimal b = new BigDecimal(c.getValue());
        int cmp = a.compareTo(b);
        
        return switch (operator) {
            case ">" -> cmp > 0;
            case ">=" -> cmp >= 0;
            case "<" -> cmp < 0;
            case "<=" -> cmp <= 0;
            default -> false;
        };
    }
}

// FieldChangedEvaluator
public class FieldChangedEvaluator implements IConditionEvaluator {
    @Override
    public boolean evaluate(WorkflowConditionEntity c, TriggerContext ctx) {
        if (ctx.getOldValues() == null) return false;
        Object oldVal = ctx.getOldValues().get(c.getFieldName());
        Object newVal = ctx.getNewValues().get(c.getFieldName());
        return !Objects.equals(oldVal, newVal);
    }
}

// ChangedToEvaluator
public class ChangedToEvaluator implements IConditionEvaluator {
    @Override
    public boolean evaluate(WorkflowConditionEntity c, TriggerContext ctx) {
        if (ctx.getOldValues() == null) return false;
        Object oldVal = ctx.getOldValues().get(c.getFieldName());
        Object newVal = ctx.getNewValues().get(c.getFieldName());
        Object expected = parseValue(c.getValue(), c.getValueType());
        
        return !Objects.equals(oldVal, newVal) && 
               Objects.equals(String.valueOf(newVal), String.valueOf(expected));
    }
}

// InListEvaluator
public class InListEvaluator implements IConditionEvaluator {
    @Override
    public boolean evaluate(WorkflowConditionEntity c, TriggerContext ctx) {
        Object actual = ctx.getNewValues().get(c.getFieldName());
        if (actual == null) return false;
        
        List<String> values = parseJsonArray(c.getValue());
        return values.contains(actual.toString());
    }
}

// BetweenEvaluator
public class BetweenEvaluator implements IConditionEvaluator {
    @Override
    public boolean evaluate(WorkflowConditionEntity c, TriggerContext ctx) {
        Object actual = ctx.getNewValues().get(c.getFieldName());
        if (actual == null) return false;
        
        String[] range = c.getValue().split(",");
        BigDecimal val = new BigDecimal(actual.toString());
        BigDecimal min = new BigDecimal(range[0].trim());
        BigDecimal max = new BigDecimal(range[1].trim());
        
        return val.compareTo(min) >= 0 && val.compareTo(max) <= 0;
    }
}
```

### 2.4. Condition Chain Evaluator

```java
@Service
public class ConditionChainEvaluator {
    private final ConditionEvaluatorRegistry registry;
    
    public boolean evaluateAll(List<WorkflowConditionEntity> conditions, TriggerContext ctx) {
        if (conditions == null || conditions.isEmpty()) return true;
        
        conditions.sort(Comparator.comparing(WorkflowConditionEntity::getConditionOrder));
        
        Boolean result = null;
        LogicalOperator pending = null;
        
        for (WorkflowConditionEntity c : conditions) {
            boolean current = registry.get(c.getOperator()).evaluate(c, ctx);
            
            if (result == null) {
                result = current;
            } else {
                result = pending == LogicalOperator.OR 
                    ? result || current 
                    : result && current;
            }
            pending = c.getLogicalOperator();
        }
        
        return result != null && result;
    }
}
```

---

## 3. Action Executor System

### 3.1. ActionHandler Interface

```java
public interface IActionHandler {
    ActionType getActionType();
    ActionResult execute(WorkflowActionEntity action, TriggerContext context);
    void validate(WorkflowActionEntity action);
}

@Data
@Builder
public class ActionResult {
    private boolean success;
    private String message;
    private Map<String, Object> output;
    private String errorCode;
    private Exception exception;
}
```

### 3.2. ActionHandlerRegistry

```java
@Component
public class ActionHandlerRegistry {
    private final Map<ActionType, IActionHandler> handlers;
    
    public ActionHandlerRegistry(List<IActionHandler> handlerList) {
        handlers = new EnumMap<>(ActionType.class);
        handlerList.forEach(h -> handlers.put(h.getActionType(), h));
    }
    
    public IActionHandler get(ActionType type) {
        return handlers.get(type);
    }
}
```

### 3.3. Action Implementations

```java
// SendEmailActionHandler
@Component
public class SendEmailActionHandler implements IActionHandler {
    private final IEmailService emailService;
    
    @Override
    public ActionType getActionType() { return ActionType.SEND_EMAIL; }
    
    @Override
    public ActionResult execute(WorkflowActionEntity action, TriggerContext ctx) {
        try {
            EmailActionConfig config = parseConfig(action.getConfiguration());
            
            Map<String, Object> variables = new HashMap<>(ctx.getNewValues());
            variables.put("entityType", ctx.getEntityType());
            variables.put("entityId", ctx.getEntityId());
            
            String toEmail = resolveEmail(config.getTo(), ctx);
            
            emailService.sendTemplateEmail(
                config.getTemplateId(),
                toEmail,
                variables,
                ctx.getTenantId()
            );
            
            return ActionResult.builder()
                .success(true)
                .message("Email sent to " + toEmail)
                .build();
                
        } catch (Exception e) {
            return ActionResult.builder()
                .success(false)
                .message(e.getMessage())
                .exception(e)
                .build();
        }
    }
    
    @Data
    public static class EmailActionConfig {
        private Long templateId;
        private String to;          // email or {{field}}
        private String cc;
        private String subject;
    }
}

// UpdateFieldActionHandler
@Component
public class UpdateFieldActionHandler implements IActionHandler {
    private final Map<String, Object> entityServices;
    
    @Override
    public ActionType getActionType() { return ActionType.UPDATE_FIELD; }
    
    @Override
    public ActionResult execute(WorkflowActionEntity action, TriggerContext ctx) {
        try {
            UpdateFieldConfig config = parseConfig(action.getConfiguration());
            
            Object newValue = resolveValue(config.getValue(), ctx);
            
            // Get entity service and update
            updateEntityField(
                ctx.getEntityType(),
                ctx.getEntityId(),
                config.getFieldName(),
                newValue,
                ctx.getTenantId()
            );
            
            return ActionResult.builder()
                .success(true)
                .message("Updated " + config.getFieldName() + " to " + newValue)
                .output(Map.of("field", config.getFieldName(), "value", newValue))
                .build();
                
        } catch (Exception e) {
            return ActionResult.builder().success(false).message(e.getMessage()).build();
        }
    }
    
    @Data
    public static class UpdateFieldConfig {
        private String fieldName;
        private String value;       // static or {{otherField}} or {{now}}
    }
}

// CreateTaskActionHandler
@Component
public class CreateTaskActionHandler implements IActionHandler {
    private final IActivityService activityService;
    
    @Override
    public ActionType getActionType() { return ActionType.CREATE_TASK; }
    
    @Override
    public ActionResult execute(WorkflowActionEntity action, TriggerContext ctx) {
        try {
            TaskConfig config = parseConfig(action.getConfiguration());
            
            ActivityEntity task = ActivityEntity.builder()
                .activityType(ActivityType.TASK)
                .subject(resolveTemplate(config.getSubject(), ctx))
                .description(resolveTemplate(config.getDescription(), ctx))
                .assignedTo(resolveAssignee(config.getAssignTo(), ctx))
                .dueDate(calculateDueDate(config.getDueDays()))
                .priority(config.getPriority())
                .leadId(ctx.getEntityType().equals("LEAD") ? ctx.getEntityId() : null)
                .customerId(ctx.getEntityType().equals("CUSTOMER") ? ctx.getEntityId() : null)
                .opportunityId(ctx.getEntityType().equals("OPPORTUNITY") ? ctx.getEntityId() : null)
                .tenantId(ctx.getTenantId())
                .build();
            
            task = activityService.create(task);
            
            return ActionResult.builder()
                .success(true)
                .message("Task created: " + task.getSubject())
                .output(Map.of("taskId", task.getId()))
                .build();
                
        } catch (Exception e) {
            return ActionResult.builder().success(false).message(e.getMessage()).build();
        }
    }
    
    @Data
    public static class TaskConfig {
        private String subject;
        private String description;
        private String assignTo;    // userId, {{assignedTo}}, or "owner"
        private Integer dueDays;
        private TaskPriority priority;
    }
}

// WebhookActionHandler
@Component
public class WebhookActionHandler implements IActionHandler {
    private final RestTemplate restTemplate;
    
    @Override
    public ActionType getActionType() { return ActionType.CALL_WEBHOOK; }
    
    @Override
    public ActionResult execute(WorkflowActionEntity action, TriggerContext ctx) {
        try {
            WebhookConfig config = parseConfig(action.getConfiguration());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (config.getHeaders() != null) {
                config.getHeaders().forEach(headers::add);
            }
            
            Map<String, Object> payload = buildPayload(config, ctx);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                config.getUrl(),
                HttpMethod.valueOf(config.getMethod()),
                request,
                String.class
            );
            
            return ActionResult.builder()
                .success(response.getStatusCode().is2xxSuccessful())
                .message("Webhook responded: " + response.getStatusCode())
                .output(Map.of("status", response.getStatusCode().value()))
                .build();
                
        } catch (Exception e) {
            return ActionResult.builder().success(false).message(e.getMessage()).build();
        }
    }
    
    @Data
    public static class WebhookConfig {
        private String url;
        private String method;
        private Map<String, String> headers;
        private Map<String, Object> body;
    }
}

// AssignToActionHandler
@Component
public class AssignToActionHandler implements IActionHandler {
    @Override
    public ActionType getActionType() { return ActionType.ASSIGN_TO; }
    
    @Override  
    public ActionResult execute(WorkflowActionEntity action, TriggerContext ctx) {
        try {
            AssignConfig config = parseConfig(action.getConfiguration());
            Long assigneeId = resolveAssignee(config, ctx);
            
            updateEntityField(ctx.getEntityType(), ctx.getEntityId(), 
                "assignedTo", assigneeId, ctx.getTenantId());
            
            return ActionResult.builder()
                .success(true)
                .message("Assigned to user " + assigneeId)
                .build();
        } catch (Exception e) {
            return ActionResult.builder().success(false).message(e.getMessage()).build();
        }
    }
    
    @Data
    public static class AssignConfig {
        private Long userId;
        private Long teamId;
        private String strategy;    // ROUND_ROBIN, LEAST_ASSIGNED, RANDOM
    }
}
```

### 3.4. Action Chain Executor

```java
@Service
public class ActionChainExecutor {
    private final ActionHandlerRegistry registry;
    private final IWorkflowExecutionLogStorePort logStore;
    
    @Async
    public ActionExecutionResult executeAll(List<WorkflowActionEntity> actions, 
                                            TriggerContext ctx, Long executionLogId) {
        actions.sort(Comparator.comparing(WorkflowActionEntity::getActionOrder));
        
        List<ActionResult> results = new ArrayList<>();
        int completed = 0, failed = 0;
        
        for (WorkflowActionEntity action : actions) {
            // Handle delay
            if (action.getDelaySeconds() != null && action.getDelaySeconds() > 0) {
                Thread.sleep(action.getDelaySeconds() * 1000L);
            }
            
            ActionResult result = executeWithRetry(action, ctx);
            results.add(result);
            
            if (result.isSuccess()) {
                completed++;
            } else {
                failed++;
                if (Boolean.TRUE.equals(action.getStopOnFailure())) {
                    break;
                }
            }
        }
        
        return ActionExecutionResult.builder()
            .totalActions(actions.size())
            .completedActions(completed)
            .failedActions(failed)
            .success(failed == 0)
            .partialSuccess(failed > 0 && completed > 0)
            .results(results)
            .build();
    }
    
    private ActionResult executeWithRetry(WorkflowActionEntity action, TriggerContext ctx) {
        int retries = action.getRetryCount() != null ? action.getRetryCount() : 0;
        
        for (int i = 0; i <= retries; i++) {
            ActionResult result = registry.get(action.getActionType()).execute(action, ctx);
            if (result.isSuccess()) return result;
            
            if (i < retries) {
                Thread.sleep(action.getRetryDelaySeconds() * 1000L);
            }
        }
        
        return ActionResult.builder().success(false).message("Max retries exceeded").build();
    }
}

@Data
@Builder
public class ActionExecutionResult {
    private int totalActions;
    private int completedActions;
    private int failedActions;
    private boolean success;
    private boolean partialSuccess;
    private List<ActionResult> results;
    private String errorMessage;
}
```

---

## 4. Action Configuration Examples

```json
// SEND_EMAIL
{"templateId": 1, "to": "{{email}}", "cc": "manager@company.com"}

// UPDATE_FIELD
{"fieldName": "leadStatus", "value": "QUALIFIED"}

// CREATE_TASK
{"subject": "Follow up with {{name}}", "dueDays": 3, "priority": "HIGH"}

// CALL_WEBHOOK
{"url": "https://api.slack.com/webhook", "method": "POST", 
 "body": {"text": "New lead: {{name}}"}}

// ASSIGN_TO
{"strategy": "ROUND_ROBIN", "teamId": 5}
```

---

**Document Version:** 1.0
