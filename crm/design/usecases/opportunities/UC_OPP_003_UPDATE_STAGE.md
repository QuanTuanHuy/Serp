# UC-OPP-003: Cập nhật Stage (Update Pipeline Stage)

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-OPP-003 |
| **Use Case Name** | Cập nhật Stage (Update Pipeline Stage) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | Critical |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép di chuyển Opportunity qua các giai đoạn trong sales pipeline. Đây là chức năng core của CRM để track tiến độ deal.

---

## 3. Pipeline Stages

```
PROSPECTING ──▶ QUALIFICATION ──▶ PROPOSAL ──▶ NEGOTIATION ──▶ CLOSED_WON
      │               │              │              │              
      └───────────────┴──────────────┴──────────────┴─────────────▶ CLOSED_LOST
```

| Stage | Description | Probability |
|-------|-------------|-------------|
| PROSPECTING | Đang tìm hiểu nhu cầu | 10% |
| QUALIFICATION | Đánh giá đủ điều kiện | 25% |
| PROPOSAL | Đã gửi proposal/quote | 50% |
| NEGOTIATION | Đang đàm phán | 75% |
| CLOSED_WON | Đã chốt - Thành công | 100% |
| CLOSED_LOST | Đã chốt - Thất bại | 0% |

---

## 4. Business Rules - Stage Transitions

| From Stage | Allowed Transitions |
|------------|---------------------|
| PROSPECTING | QUALIFICATION, CLOSED_LOST |
| QUALIFICATION | PROPOSAL, PROSPECTING, CLOSED_LOST |
| PROPOSAL | NEGOTIATION, QUALIFICATION, CLOSED_LOST |
| NEGOTIATION | CLOSED_WON, PROPOSAL, CLOSED_LOST |
| CLOSED_WON | (no transitions - final state) |
| CLOSED_LOST | (no transitions - final state) |

---

## 5. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Mở Opportunity hoặc Pipeline view |
| 2 | User | - | Drag & drop hoặc click "Advance Stage" |
| 3 | - | System | Kiểm tra transition hợp lệ |
| 4 | - | System | Cập nhật stage và probability |
| 5 | - | System | Nếu closed, set actualCloseDate |
| 6 | - | System | Publish event OPPORTUNITY_STAGE_CHANGED |
| 7 | - | System | Trả về kết quả |

---

## 6. API Specification

### Endpoint
```
PUT /crm/api/v1/opportunities/{opportunityId}/stage
```

### Request Body
```json
{
  "stage": "PROPOSAL"
}
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Stage updated successfully",
  "data": {
    "id": 789,
    "name": "ABC Company - Enterprise License",
    "stage": "PROPOSAL",
    "probability": 50,
    "updatedAt": "2025-12-10T14:30:00Z"
  }
}
```

---

## 7. Exception Flows

### EF-01: Invalid Stage Transition
```json
{
  "code": 400,
  "message": "Cannot advance to the specified stage from the current stage"
}
```

### EF-02: Already Closed
```json
{
  "code": 400,
  "message": "Cannot change stage of closed opportunity"
}
```

---

## 8. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | PROSPECTING → QUALIFICATION | Success, probability = 25 |
| TC-002 | QUALIFICATION → PROPOSAL | Success, probability = 50 |
| TC-003 | PROPOSAL → NEGOTIATION | Success, probability = 75 |
| TC-004 | NEGOTIATION → CLOSED_WON | Success, probability = 100 |
| TC-005 | PROSPECTING → PROPOSAL (skip) | Error - invalid transition |
| TC-006 | CLOSED_WON → any | Error - already closed |

---

## 9. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
