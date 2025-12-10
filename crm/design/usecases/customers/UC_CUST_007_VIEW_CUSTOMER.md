# UC-CUST-007: Xem chi tiết Customer

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CUST-007 |
| **Use Case Name** | Xem chi tiết Customer (View Customer Detail) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép xem thông tin chi tiết của một Customer bao gồm thông tin công ty, danh sách contacts, opportunities liên quan, và các metrics.

---

## 3. Information Displayed

### Company Information
- Name, Phone, Email, Website
- Industry, Company Size
- Tax ID, Address
- Credit Limit

### Business Metrics
- Total Opportunities
- Won Opportunities
- Total Revenue

### Related Data
- Contacts List
- Opportunities List
- Activity History
- Parent Customer (if any)

---

## 4. API Specification

### Endpoint
```
GET /crm/api/v1/customers/{customerId}
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 101,
    "name": "ABC Company Ltd",
    "phone": "+84901234567",
    "email": "contact@abc.com",
    "website": "https://abc.com",
    "industry": "Technology",
    "companySize": "50-200",
    "taxId": "0123456789",
    "creditLimit": 200000000,
    "totalOpportunities": 5,
    "wonOpportunities": 3,
    "totalRevenue": 150000000,
    "activeStatus": "ACTIVE",
    "address": {
      "street": "123 Nguyen Hue",
      "city": "Ho Chi Minh",
      "country": "Vietnam"
    },
    "contacts": [
      {
        "id": 202,
        "name": "Nguyễn Văn A",
        "email": "nguyenvana@abc.com",
        "isPrimary": true
      }
    ],
    "createdAt": "2025-12-01T10:00:00Z",
    "updatedAt": "2025-12-10T14:30:00Z"
  }
}
```

---

## 5. Exception Flows

### EF-01: Customer Not Found
```json
{
  "code": 404,
  "message": "Customer not found with id: 999"
}
```

---

## 6. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | View existing customer | Đầy đủ thông tin |
| TC-002 | View non-existing customer | 404 Error |
| TC-003 | View customer with contacts | Hiển thị contacts list |

---

## 7. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
