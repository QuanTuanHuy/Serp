# UC-LEAD-003: Qualify Lead

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-LEAD-003 |
| **Use Case Name** | Qualify Lead (Đánh giá đủ điều kiện Lead) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép người dùng đánh giá và qualify một Lead, xác định Lead có đủ điều kiện để chuyển đổi thành Opportunity hay không. Quá trình qualify thường dựa trên các tiêu chí BANT (Budget, Authority, Need, Timeline).

---

## 3. User Stories

### US-LEAD-003-01
**As a** Sales Representative  
**I want to** qualify a lead based on BANT criteria  
**So that** I can focus on leads with high potential

### US-LEAD-003-02
**As a** Sales Manager  
**I want to** review qualified leads  
**So that** I can prioritize resources effectively

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Lead tồn tại trong hệ thống |
| PRE-02 | Lead chưa được QUALIFIED hoặc CONVERTED |
| PRE-03 | Lead có status: NEW, CONTACTED, hoặc NURTURING |
| PRE-04 | Người dùng có quyền QUALIFY_LEAD |

---

## 5. Postconditions

| ID | Điều kiện |
|----|-----------|
| POST-01 | Lead status được chuyển thành QUALIFIED |
| POST-02 | Notes được cập nhật với lý do qualify |
| POST-03 | Lead sẵn sàng để convert thành Opportunity |

---

## 6. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Chỉ có thể qualify Lead từ status NEW, CONTACTED, NURTURING | Constraint |
| BR-002 | Lead đã QUALIFIED không thể qualify lại | Constraint |
| BR-003 | Lead đã DISQUALIFIED không thể qualify | Constraint |
| BR-004 | Nên có estimatedValue > 0 trước khi qualify | Recommendation |
| BR-005 | Notes bắt buộc khi qualify | Mandatory |

### BANT Qualification Criteria (Reference)

| Criteria | Description | Score Weight |
|----------|-------------|--------------|
| **B**udget | Khách hàng có ngân sách phù hợp | 25% |
| **A**uthority | Người liên hệ có quyền quyết định | 25% |
| **N**eed | Có nhu cầu thực sự với sản phẩm/dịch vụ | 30% |
| **T**imeline | Có timeline rõ ràng để mua | 20% |

---

## 7. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Truy cập màn hình chi tiết Lead |
| 2 | User | - | Click nút "Qualify Lead" |
| 3 | - | System | Hiển thị dialog xác nhận qualify |
| 4 | User | - | Nhập notes/lý do qualify |
| 5 | User | - | Click "Confirm Qualify" |
| 6 | - | System | Kiểm tra Lead có thể qualify (chưa qualified/converted) |
| 7 | - | System | Cập nhật leadStatus = QUALIFIED |
| 8 | - | System | Ghi notes và updatedBy |
| 9 | - | System | Lưu thay đổi vào database |
| 10 | - | System | Publish event LEAD_QUALIFIED |
| 11 | - | System | Trả về Lead đã được qualify |

---

## 8. Alternative Flows

### AF-01: Disqualify Lead

| Step | Description |
|------|-------------|
| 2a | User click "Disqualify Lead" thay vì "Qualify Lead" |
| 3a | System hiển thị dialog yêu cầu lý do disqualify |
| 4a | User nhập lý do disqualify |
| 5a | System cập nhật leadStatus = DISQUALIFIED |
| 6a | Lead không thể tiếp tục trong pipeline |

---

## 9. Exception Flows

### EF-01: Lead Already Qualified

| Condition | Response |
|-----------|----------|
| Lead đã ở status QUALIFIED | HTTP 400 - "Lead is already qualified or converted" |

### EF-02: Invalid Status for Qualification

| Condition | Response |
|-----------|----------|
| Lead ở status DISQUALIFIED | HTTP 400 - "Cannot qualify a disqualified lead" |

---

## 10. API Specification

### Endpoint

```
POST /crm/api/v1/leads/{leadId}/qualify
```

### Request Body

```json
{
  "notes": "Qualified based on BANT: Budget confirmed $50K, Decision maker identified, Clear need for CRM solution, Timeline: Q1 2026"
}
```

### Response (Success - 200 OK)

```json
{
  "code": 200,
  "message": "Lead qualified successfully",
  "data": {
    "id": 456,
    "name": "Nguyễn Văn A",
    "leadStatus": "QUALIFIED",
    "notes": "Qualified based on BANT: Budget confirmed $50K...",
    "updatedAt": "2025-12-10T15:00:00Z"
  }
}
```

---

## 11. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Qualify Lead từ status NEW | Success, status = QUALIFIED |
| TC-002 | Qualify Lead từ status CONTACTED | Success |
| TC-003 | Qualify Lead đã QUALIFIED | Error - already qualified |
| TC-004 | Qualify Lead đã DISQUALIFIED | Error - cannot qualify |
| TC-005 | Qualify Lead không có notes | Validation error |

---

## 12. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
