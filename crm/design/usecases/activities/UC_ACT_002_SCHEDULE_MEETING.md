# UC-ACT-002: Lên lịch Meeting

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-ACT-002 |
| **Use Case Name** | Lên lịch Meeting (Schedule Meeting) |
| **Actor** | Sales Representative |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép lên lịch cuộc họp với khách hàng. Đây là extension của UC-ACT-001 với activity type = MEETING.

---

## 3. User Stories

### US-ACT-002-01
**As a** Sales Representative  
**I want to** schedule meetings with customers  
**So that** I can discuss business opportunities face-to-face

---

## 4. Meeting-specific Fields

| Field | Description |
|-------|-------------|
| location | Địa điểm họp (address hoặc meeting link) |
| durationMinutes | Thời lượng cuộc họp |
| activityDate | Thời điểm bắt đầu |
| reminderDate | Thời điểm nhắc nhở |

---

## 5. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | activityType = MEETING | Constraint |
| BR-002 | activityDate nên là thời điểm tương lai | Warning |
| BR-003 | durationMinutes mặc định = 60 | Default |
| BR-004 | Có thể set reminder trước cuộc họp | Behavior |

---

## 6. API Specification

### Endpoint
```
POST /crm/api/v1/activities
```

### Request Body
```json
{
  "activityType": "MEETING",
  "subject": "Product Demo Meeting",
  "description": "Demo enterprise features to ABC team",
  "customerId": 101,
  "opportunityId": 789,
  "activityDate": 1702400000000,
  "durationMinutes": 90,
  "location": "ABC Company Office - 123 Nguyen Hue, HCM",
  "reminderDate": 1702396400000,
  "priority": "HIGH",
  "assignedTo": 123
}
```

### Response (Success - 201 Created)
```json
{
  "code": 201,
  "message": "Meeting scheduled successfully",
  "data": {
    "id": 1002,
    "activityType": "MEETING",
    "subject": "Product Demo Meeting",
    "status": "PLANNED",
    "location": "ABC Company Office - 123 Nguyen Hue, HCM",
    "activityDate": 1702400000000,
    "durationMinutes": 90,
    "reminderDate": 1702396400000,
    "createdAt": "2025-12-10T10:30:00Z"
  }
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Schedule meeting với location | Success |
| TC-002 | Schedule meeting với reminder | Success |
| TC-003 | Schedule meeting trong quá khứ | Warning nhưng vẫn cho phép |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
