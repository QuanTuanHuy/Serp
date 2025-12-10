# UC-LEAD-002: Cập nhật Lead

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-LEAD-002 |
| **Use Case Name** | Cập nhật Lead (Update Lead) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép người dùng cập nhật thông tin của một Lead hiện có trong hệ thống. Việc cập nhật có thể bao gồm thông tin liên hệ, thông tin công ty, giá trị ước tính, và các trường khác.

---

## 3. User Stories

### US-LEAD-002-01
**As a** Sales Representative  
**I want to** update lead contact information  
**So that** I can keep the lead data accurate and up-to-date

### US-LEAD-002-02
**As a** Sales Representative  
**I want to** update the estimated value and probability  
**So that** I can reflect the latest qualification status

### US-LEAD-002-03
**As a** Sales Manager  
**I want to** add notes to a lead  
**So that** I can share important information with the team

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Người dùng đã đăng nhập vào hệ thống |
| PRE-02 | Người dùng có quyền UPDATE_LEAD |
| PRE-03 | Lead tồn tại trong hệ thống |
| PRE-04 | Lead chưa bị CONVERTED hoặc DISQUALIFIED |

---

## 5. Postconditions

| ID | Điều kiện |
|----|-----------|
| POST-01 | Thông tin Lead được cập nhật trong database |
| POST-02 | updatedBy và updatedAt được ghi nhận |
| POST-03 | Event LEAD_UPDATED được publish lên Kafka |

---

## 6. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Không thể cập nhật Lead đã CONVERTED | Constraint |
| BR-002 | Không thể cập nhật Lead đã DISQUALIFIED (trừ notes) | Constraint |
| BR-003 | Chỉ owner hoặc manager mới được cập nhật | Authorization |
| BR-004 | Khi cập nhật status, phải tuân theo state machine | Validation |
| BR-005 | estimatedValue không được âm | Validation |
| BR-006 | probability phải trong khoảng 0-100 | Validation |

### Lead Status Transition Rules

```
NEW ──────────────▶ CONTACTED ──────────▶ NURTURING ──────────▶ QUALIFIED ──────────▶ CONVERTED
 │                      │                     │                     │
 │                      │                     │                     │
 └──────────────────────┴─────────────────────┴─────────────────────┴──────────▶ DISQUALIFIED
```

---

## 7. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Truy cập màn hình chi tiết Lead |
| 2 | User | - | Click nút "Edit" / "Chỉnh sửa" |
| 3 | - | System | Hiển thị form với dữ liệu hiện tại |
| 4 | User | - | Chỉnh sửa các trường cần thiết |
| 5 | User | - | Click nút "Save" / "Lưu" |
| 6 | - | System | Validate dữ liệu đầu vào |
| 7 | - | System | Kiểm tra quyền và business rules |
| 8 | - | System | Cập nhật entity với các giá trị mới |
| 9 | - | System | Lưu thay đổi vào database |
| 10 | - | System | Publish event LEAD_UPDATED |
| 11 | - | System | Trả về thông tin Lead đã cập nhật |

---

## 8. Alternative Flows

### AF-01: Status Change Required (Thay đổi trạng thái)

| Step | Description |
|------|-------------|
| 4a | User thay đổi leadStatus |
| 4b | System kiểm tra transition hợp lệ theo state machine |
| 4c | Nếu hợp lệ, cập nhật status và ghi log |
| 4d | Nếu không hợp lệ, trả về lỗi với message chi tiết |

### AF-02: Partial Update (Cập nhật một phần)

| Step | Description |
|------|-------------|
| 4a | User chỉ cập nhật một số trường |
| 4b | System chỉ cập nhật các trường có giá trị (non-null) |
| 4c | Các trường khác giữ nguyên |

---

## 9. Exception Flows

### EF-01: Lead Not Found

| Condition | Response |
|-----------|----------|
| Lead ID không tồn tại | HTTP 404 Not Found |

### EF-02: Lead Already Converted

| Condition | Response |
|-----------|----------|
| Lead đã được convert | HTTP 400 Bad Request - "Cannot update converted lead" |

### EF-03: Invalid Status Transition

| Condition | Response |
|-----------|----------|
| Chuyển status không hợp lệ | HTTP 400 Bad Request - "Invalid lead status transition: {from} -> {to}" |

---

## 10. API Specification

### Endpoint

```
PUT /crm/api/v1/leads/{leadId}
```

### Request Body

```json
{
  "name": "Nguyễn Văn A (Updated)",
  "email": "nguyenvana.new@example.com",
  "phone": "+84901234568",
  "company": "ABC Company Ltd",
  "estimatedValue": 75000000,
  "probability": 40,
  "notes": "Updated notes after second meeting"
}
```

### Response (Success - 200 OK)

```json
{
  "code": 200,
  "message": "Lead updated successfully",
  "data": {
    "id": 456,
    "name": "Nguyễn Văn A (Updated)",
    "email": "nguyenvana.new@example.com",
    "leadStatus": "CONTACTED",
    "estimatedValue": 75000000,
    "probability": 40,
    "updatedAt": "2025-12-10T14:30:00Z",
    "updatedBy": 789
  }
}
```

---

## 11. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Cập nhật thông tin cơ bản | Success |
| TC-002 | Cập nhật Lead không tồn tại | 404 Not Found |
| TC-003 | Cập nhật Lead đã converted | 400 Bad Request |
| TC-004 | Cập nhật status invalid transition | 400 Bad Request |
| TC-005 | Cập nhật với probability > 100 | Validation error |

---

## 12. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
