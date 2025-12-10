# UC-OPP-002: Cập nhật Opportunity

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-OPP-002 |
| **Use Case Name** | Cập nhật Opportunity (Update Opportunity) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép cập nhật thông tin của một Opportunity hiện có, trừ những Opportunity đã closed (WON/LOST).

---

## 3. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Không thể update Opportunity đã CLOSED_WON | Constraint |
| BR-002 | Không thể update Opportunity đã CLOSED_LOST | Constraint |
| BR-003 | Khi update stage, phải tuân theo state machine | Validation |
| BR-004 | Probability được auto-update theo stage | Behavior |

---

## 4. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Opportunity Detail |
| 2 | User | - | Click "Edit" |
| 3 | - | System | Kiểm tra Opportunity chưa closed |
| 4 | - | System | Hiển thị form với data hiện tại |
| 5 | User | - | Chỉnh sửa thông tin |
| 6 | User | - | Click "Save" |
| 7 | - | System | Validate và cập nhật |
| 8 | - | System | Lưu và trả về kết quả |

---

## 5. API Specification

### Endpoint
```
PUT /crm/api/v1/opportunities/{opportunityId}
```

### Request Body
```json
{
  "name": "ABC Company - Enterprise License (Updated)",
  "estimatedValue": 120000000,
  "expectedCloseDate": "2026-04-15",
  "notes": "Client requested additional features"
}
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Opportunity updated successfully",
  "data": {
    "id": 789,
    "name": "ABC Company - Enterprise License (Updated)",
    "estimatedValue": 120000000,
    "expectedCloseDate": "2026-04-15",
    "updatedAt": "2025-12-10T14:30:00Z"
  }
}
```

---

## 6. Exception Flows

### EF-01: Opportunity Closed
```json
{
  "code": 400,
  "message": "Cannot update closed opportunities"
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Update Opportunity mở | Success |
| TC-002 | Update Opportunity CLOSED_WON | Error |
| TC-003 | Update Opportunity CLOSED_LOST | Error |
| TC-004 | Update không tồn tại | 404 Error |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
