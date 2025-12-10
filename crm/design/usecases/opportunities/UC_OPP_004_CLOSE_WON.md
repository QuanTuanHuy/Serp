# UC-OPP-004: Đóng Opportunity Won

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-OPP-004 |
| **Use Case Name** | Đóng Opportunity Won (Close Opportunity as Won) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | Critical |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép đánh dấu một Opportunity là thành công (Won). Đây là kết quả tích cực của sales process, đánh dấu deal đã được chốt thành công.

---

## 3. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Opportunity phải đang ở stage NEGOTIATION | Constraint |
| BR-002 | Opportunity chưa closed | Constraint |
| BR-003 | Set stage = CLOSED_WON | Behavior |
| BR-004 | Set probability = 100% | Behavior |
| BR-005 | Set actualCloseDate = today | Behavior |
| BR-006 | Update Customer totalRevenue và wonOpportunities | Behavior |

---

## 4. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Opportunity đang NEGOTIATION |
| 2 | User | - | Click "Mark as Won" |
| 3 | - | System | Hiển thị confirmation dialog |
| 4 | User | - | Confirm |
| 5 | - | System | Kiểm tra Opportunity chưa closed |
| 6 | - | System | Gọi closeAsWon() |
| 7 | - | System | Update Customer metrics |
| 8 | - | System | Publish event OPPORTUNITY_WON |
| 9 | - | System | Trả về kết quả |

---

## 5. API Specification

### Endpoint
```
PUT /crm/api/v1/opportunities/{opportunityId}/close-won
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Opportunity closed as won",
  "data": {
    "id": 789,
    "name": "ABC Company - Enterprise License",
    "stage": "CLOSED_WON",
    "probability": 100,
    "actualCloseDate": "2025-12-10",
    "estimatedValue": 100000000,
    "updatedAt": "2025-12-10T14:30:00Z"
  }
}
```

---

## 6. Exception Flows

### EF-01: Already Closed
```json
{
  "code": 400,
  "message": "Opportunity already closed"
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Close NEGOTIATION opportunity | Success, stage = CLOSED_WON |
| TC-002 | Close already closed | Error |
| TC-003 | Customer metrics updated | totalRevenue, wonOpportunities increased |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
