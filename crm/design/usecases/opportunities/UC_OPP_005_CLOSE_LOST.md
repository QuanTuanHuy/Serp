# UC-OPP-005: Đóng Opportunity Lost

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-OPP-005 |
| **Use Case Name** | Đóng Opportunity Lost (Close Opportunity as Lost) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép đánh dấu một Opportunity là thất bại (Lost). Yêu cầu ghi nhận lý do thua để phân tích và cải thiện.

---

## 3. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Opportunity chưa closed | Constraint |
| BR-002 | Loss reason là bắt buộc | Mandatory |
| BR-003 | Set stage = CLOSED_LOST | Behavior |
| BR-004 | Set probability = 0% | Behavior |
| BR-005 | Set actualCloseDate = today | Behavior |
| BR-006 | Ghi nhận lossReason | Behavior |
| BR-007 | Update Customer totalOpportunities (không update wonOpportunities) | Behavior |

---

## 4. Common Loss Reasons

| Reason | Description |
|--------|-------------|
| PRICE | Giá không cạnh tranh |
| COMPETITOR | Mất cho đối thủ |
| NO_BUDGET | Khách không có ngân sách |
| NO_DECISION | Khách không ra quyết định |
| TIMING | Không đúng thời điểm |
| PRODUCT_FIT | Sản phẩm không phù hợp |
| OTHER | Lý do khác |

---

## 5. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Opportunity chưa closed |
| 2 | User | - | Click "Mark as Lost" |
| 3 | - | System | Hiển thị dialog nhập loss reason |
| 4 | User | - | Chọn/nhập loss reason |
| 5 | User | - | Click "Confirm" |
| 6 | - | System | Validate loss reason required |
| 7 | - | System | Gọi closeAsLost(reason) |
| 8 | - | System | Publish event OPPORTUNITY_LOST |
| 9 | - | System | Trả về kết quả |

---

## 6. API Specification

### Endpoint
```
PUT /crm/api/v1/opportunities/{opportunityId}/close-lost
```

### Request Body
```json
{
  "lossReason": "Lost to competitor - lower price offer"
}
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Opportunity closed as lost",
  "data": {
    "id": 789,
    "name": "ABC Company - Enterprise License",
    "stage": "CLOSED_LOST",
    "probability": 0,
    "actualCloseDate": "2025-12-10",
    "lossReason": "Lost to competitor - lower price offer",
    "updatedAt": "2025-12-10T14:30:00Z"
  }
}
```

---

## 7. Exception Flows

### EF-01: Missing Loss Reason
```json
{
  "code": 400,
  "message": "Loss reason is required"
}
```

### EF-02: Already Closed
```json
{
  "code": 400,
  "message": "Opportunity already closed"
}
```

---

## 8. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Close với loss reason | Success, stage = CLOSED_LOST |
| TC-002 | Close không có loss reason | Error - required |
| TC-003 | Close opportunity đã closed | Error |

---

## 9. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
