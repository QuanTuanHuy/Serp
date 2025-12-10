# UC-CUST-006: Tìm kiếm Customer

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-CUST-006 |
| **Use Case Name** | Tìm kiếm Customer (Search Customer) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép tìm kiếm và lọc danh sách Customers theo nhiều tiêu chí.

---

## 3. Search Criteria

| Field | Operator | Description |
|-------|----------|-------------|
| name | LIKE | Tìm theo tên |
| email | LIKE | Tìm theo email |
| phone | LIKE | Tìm theo SĐT |
| industry | IN | Lọc theo ngành |
| activeStatus | EQUALS | Lọc ACTIVE/INACTIVE |
| minRevenue | >= | Revenue tối thiểu |
| maxRevenue | <= | Revenue tối đa |

---

## 4. API Specification

### Endpoint
```
GET /crm/api/v1/customers
```

### Query Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| keyword | String | Tìm kiếm theo name, email, phone |
| industry | String | Filter by industry |
| activeStatus | String | ACTIVE or INACTIVE |
| minRevenue | Decimal | Minimum total revenue |
| maxRevenue | Decimal | Maximum total revenue |
| page | Integer | Page number |
| size | Integer | Page size |
| sortBy | String | Sort field |
| sortDir | String | ASC or DESC |

### Example
```
GET /crm/api/v1/customers?keyword=ABC&activeStatus=ACTIVE&page=0&size=20
```

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 101,
        "name": "ABC Company Ltd",
        "email": "contact@abc.com",
        "industry": "Technology",
        "activeStatus": "ACTIVE",
        "totalOpportunities": 5,
        "wonOpportunities": 3,
        "totalRevenue": 150000000
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 50,
    "totalPages": 3
  }
}
```

---

## 5. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Search với keyword | Trả về matching customers |
| TC-002 | Filter theo activeStatus | Trả về customers với status |
| TC-003 | Combine filters | Trả về customers thỏa mãn tất cả |
| TC-004 | Empty result | Trả về empty list |

---

## 6. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
