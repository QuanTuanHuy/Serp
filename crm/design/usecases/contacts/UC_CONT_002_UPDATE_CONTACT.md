# UC-CONT-002: Cập nhật Contact

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CONT-002 |
| **Use Case Name** | Cập nhật Contact (Update Contact) |
| **Actor** | Sales Representative |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép cập nhật thông tin của một Contact hiện có.

---

## 3. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Contact phải tồn tại | Constraint |
| BR-002 | Name không được để trống | Validation |
| BR-003 | Email phải hợp lệ (nếu có) | Validation |
| BR-004 | Partial update được hỗ trợ | Behavior |

---

## 4. API Specification

### Endpoint
```
PUT /crm/api/v1/contacts/{contactId}
```

### Request Body
```json
{
  "name": "Nguyễn Văn A (Updated)",
  "phone": "+84901234568",
  "jobPosition": "Managing Director",
  "notes": "Promoted to MD"
}
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Contact updated successfully",
  "data": {
    "id": 202,
    "name": "Nguyễn Văn A (Updated)",
    "phone": "+84901234568",
    "jobPosition": "Managing Director",
    "updatedAt": "2025-12-10T14:30:00Z"
  }
}
```

---

## 5. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Update contact info | Success |
| TC-002 | Update non-existent contact | 404 Error |
| TC-003 | Partial update | Only specified fields updated |

---

## 6. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
