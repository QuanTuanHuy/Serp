# UC-TEAM-001: Tạo Team Mới

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-TEAM-001 |
| **Use Case Name** | Tạo Team Mới (Create New Team) |
| **Actor** | Sales Manager, Administrator |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép tạo mới một Sales Team trong hệ thống để tổ chức và quản lý đội ngũ bán hàng.

---

## 3. User Stories

### US-TEAM-001-01
**As a** Sales Manager  
**I want to** create sales teams  
**So that** I can organize my sales force effectively

### US-TEAM-001-02
**As a** Administrator  
**I want to** assign team leaders  
**So that** teams have clear ownership

---

## 4. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Team name là bắt buộc | Mandatory |
| BR-002 | Team name phải unique | Validation |
| BR-003 | Leader phải là user hợp lệ trong hệ thống | Validation |
| BR-004 | Team có thể tạo không có members | Behavior |

---

## 5. Data Requirements

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | String | Yes | Tên team |
| description | String | No | Mô tả team |
| leaderId | Long | No | ID của team leader |
| notes | String | No | Ghi chú |

---

## 6. API Specification

### Endpoint
```
POST /crm/api/v1/teams
```

### Request Body
```json
{
  "name": "Enterprise Sales Team",
  "description": "Team handling enterprise accounts",
  "leaderId": 123,
  "notes": "Focus on large deals > 100M VND"
}
```

### Response (Success - 201 Created)
```json
{
  "code": 201,
  "message": "Team created successfully",
  "data": {
    "id": 1,
    "name": "Enterprise Sales Team",
    "description": "Team handling enterprise accounts",
    "leaderId": 123,
    "createdAt": "2025-12-10T10:30:00Z",
    "members": []
  }
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Create team với thông tin đầy đủ | Success |
| TC-002 | Create team thiếu name | Error |
| TC-003 | Create team với duplicate name | Error |
| TC-004 | Create team với invalid leaderId | Error |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
