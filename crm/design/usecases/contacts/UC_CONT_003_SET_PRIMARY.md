# UC-CONT-003: Đặt Primary Contact

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CONT-003 |
| **Use Case Name** | Đặt Primary Contact (Set Primary Contact) |
| **Actor** | Sales Representative |
| **Priority** | Medium |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép đặt một Contact làm Primary Contact cho Customer. Mỗi Customer chỉ có thể có 1 Primary Contact tại một thời điểm.

---

## 3. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Contact phải thuộc một Customer | Constraint |
| BR-002 | Chỉ có 1 Primary Contact per Customer | Constraint |
| BR-003 | Primary Contact cũ tự động được unset | Behavior |
| BR-004 | Contact phải đang ACTIVE | Constraint |

---

## 4. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Xem danh sách Contacts của Customer |
| 2 | User | - | Click "Set as Primary" trên Contact |
| 3 | - | System | Tìm Primary Contact hiện tại (nếu có) |
| 4 | - | System | Unset isPrimary cho contact cũ |
| 5 | - | System | Set isPrimary = true cho contact mới |
| 6 | - | System | Lưu và trả về kết quả |

---

## 5. API Specification

### Endpoint
```
PUT /crm/api/v1/contacts/{contactId}/set-primary
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Primary contact set successfully",
  "data": {
    "id": 202,
    "name": "Nguyễn Văn A",
    "isPrimary": true,
    "customerId": 101,
    "updatedAt": "2025-12-10T14:30:00Z"
  }
}
```

---

## 6. Exception Flows

### EF-01: Contact Not Linked to Customer
```json
{
  "code": 400,
  "message": "Contact must be linked to a customer to be set as primary"
}
```

### EF-02: Contact Inactive
```json
{
  "code": 400,
  "message": "Cannot set inactive contact as primary"
}
```

---

## 7. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Set primary cho contact mới | Success, isPrimary = true |
| TC-002 | Set primary khi đã có primary | Old primary unset, new primary set |
| TC-003 | Set primary cho contact không có customer | Error |
| TC-004 | Set primary cho inactive contact | Error |

---

## 8. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
