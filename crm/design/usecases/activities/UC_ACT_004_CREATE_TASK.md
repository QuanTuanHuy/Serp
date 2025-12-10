# UC-ACT-004: Tạo Task

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-ACT-004 |
| **Use Case Name** | Tạo Task (Create Task) |
| **Actor** | Sales Representative |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép tạo task/công việc cần thực hiện liên quan đến Lead, Customer, hoặc Opportunity.

---

## 3. User Stories

### US-ACT-004-01
**As a** Sales Representative  
**I want to** create tasks for follow-up actions  
**So that** I don't forget important activities

### US-ACT-004-02
**As a** Sales Manager  
**I want to** assign tasks to team members  
**So that** work is properly distributed

---

## 4. Task-specific Fields

| Field | Description |
|-------|-------------|
| dueDate | Deadline của task |
| priority | LOW, MEDIUM, HIGH |
| progressPercent | Tiến độ 0-100% |
| status | PLANNED, COMPLETED, CANCELLED |

---

## 5. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | activityType = TASK | Constraint |
| BR-002 | dueDate là bắt buộc cho task | Mandatory |
| BR-003 | progressPercent mặc định = 0 | Default |
| BR-004 | Khi complete, progressPercent = 100 | Behavior |

---

## 6. API Specification

### Endpoint
```
POST /crm/api/v1/activities
```

### Request Body
```json
{
  "activityType": "TASK",
  "subject": "Prepare proposal document",
  "description": "Create detailed proposal with pricing for ABC Company",
  "opportunityId": 789,
  "dueDate": 1702500000000,
  "priority": "HIGH",
  "assignedTo": 123,
  "progressPercent": 0
}
```

### Response (Success - 201 Created)
```json
{
  "code": 201,
  "message": "Task created successfully",
  "data": {
    "id": 1004,
    "activityType": "TASK",
    "subject": "Prepare proposal document",
    "status": "PLANNED",
    "priority": "HIGH",
    "dueDate": 1702500000000,
    "progressPercent": 0,
    "opportunityId": 789,
    "assignedTo": 123,
    "createdAt": "2025-12-10T10:30:00Z"
  }
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Create task với due date | Success |
| TC-002 | Create task thiếu due date | Error (required) |
| TC-003 | Create task với priority | Priority được set |
| TC-004 | Update progress | progressPercent được cập nhật |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
