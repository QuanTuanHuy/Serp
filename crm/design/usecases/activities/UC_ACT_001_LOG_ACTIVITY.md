# UC-ACT-001: Ghi nhận Activity (Log Activity)

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-ACT-001 |
| **Use Case Name** | Ghi nhận Activity (Log Activity) |
| **Actor** | Sales Representative |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép ghi nhận các hoạt động tương tác với khách hàng như cuộc gọi, cuộc họp, email, và task. Activities có thể liên kết với Lead, Contact, Customer, hoặc Opportunity.

---

## 3. User Stories

### US-ACT-001-01
**As a** Sales Representative  
**I want to** log my customer interactions  
**So that** I can track engagement history

### US-ACT-001-02
**As a** Sales Manager  
**I want to** see activity history  
**So that** I can monitor team engagement

---

## 4. Activity Types

| Type | Description | Typical Fields |
|------|-------------|----------------|
| CALL | Cuộc gọi điện thoại | Duration, outcome, notes |
| MEETING | Cuộc họp trực tiếp/online | Location, attendees, agenda |
| EMAIL | Trao đổi email | Subject, content |
| TASK | Công việc cần làm | Due date, priority |

---

## 5. Activity Status Flow

```
PLANNED ────────────▶ COMPLETED
    │
    └────────────────▶ CANCELLED
```

---

## 6. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Activity phải liên kết với ít nhất 1 entity (Lead/Contact/Customer/Opportunity) | Validation |
| BR-002 | Subject là bắt buộc | Mandatory |
| BR-003 | Status mặc định = PLANNED | Default |
| BR-004 | Priority mặc định = MEDIUM | Default |
| BR-005 | progressPercent: 0-100 | Validation |
| BR-006 | durationMinutes phải > 0 (nếu có) | Validation |

---

## 7. Data Requirements

### Input Data

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| activityType | Enum | Yes | CALL, MEETING, EMAIL, TASK |
| subject | String | Yes | Tiêu đề |
| description | String | No | Mô tả chi tiết |
| leadId | Long | No* | Link với Lead |
| contactId | Long | No* | Link với Contact |
| customerId | Long | No* | Link với Customer |
| opportunityId | Long | No* | Link với Opportunity |
| activityDate | Long | No | Timestamp hoạt động |
| dueDate | Long | No | Deadline |
| reminderDate | Long | No | Thời điểm nhắc nhở |
| durationMinutes | Integer | No | Thời lượng (phút) |
| location | String | No | Địa điểm |
| priority | Enum | No | LOW, MEDIUM, HIGH |
| assignedTo | Long | No | Người phụ trách |
| attachments | List<String> | No | File đính kèm |

*Ít nhất một trong các ID phải được cung cấp

---

## 8. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở form tạo Activity (từ Lead/Contact/Opportunity) |
| 2 | User | - | Chọn Activity Type |
| 3 | User | - | Nhập subject và description |
| 4 | User | - | Set due date và priority |
| 5 | User | - | (Optional) Assign cho người khác |
| 6 | User | - | Click "Create" |
| 7 | - | System | Validate dữ liệu |
| 8 | - | System | Set defaults (status, priority) |
| 9 | - | System | Lưu vào database |
| 10 | - | System | Publish event ACTIVITY_CREATED |
| 11 | - | System | Trả về Activity đã tạo |

---

## 9. API Specification

### Endpoint
```
POST /crm/api/v1/activities
```

### Request Body
```json
{
  "activityType": "CALL",
  "subject": "Follow-up call with CEO",
  "description": "Discuss pricing and implementation timeline",
  "leadId": 456,
  "opportunityId": 789,
  "activityDate": 1702200000000,
  "dueDate": 1702300000000,
  "durationMinutes": 30,
  "priority": "HIGH",
  "assignedTo": 123
}
```

### Response (Success - 201 Created)
```json
{
  "code": 201,
  "message": "Activity created successfully",
  "data": {
    "id": 1001,
    "activityType": "CALL",
    "subject": "Follow-up call with CEO",
    "status": "PLANNED",
    "priority": "HIGH",
    "dueDate": 1702300000000,
    "assignedTo": 123,
    "leadId": 456,
    "opportunityId": 789,
    "createdAt": "2025-12-10T10:30:00Z"
  }
}
```

---

## 10. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Create activity với đầy đủ thông tin | Success |
| TC-002 | Create activity thiếu subject | Error |
| TC-003 | Create activity không link entity nào | Error |
| TC-004 | Create activity với invalid type | Error |

---

## 11. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
