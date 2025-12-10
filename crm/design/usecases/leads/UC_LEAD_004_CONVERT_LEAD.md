# UC-LEAD-004: Chuyển đổi Lead (Convert Lead)

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-LEAD-004 |
| **Use Case Name** | Chuyển đổi Lead (Convert Lead to Opportunity/Customer) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | Critical |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép chuyển đổi một Lead đã qualified thành Opportunity và/hoặc Customer. Đây là bước quan trọng trong sales pipeline, đánh dấu sự chuyển tiếp từ giai đoạn tiếp cận sang giai đoạn bán hàng thực sự.

---

## 3. User Stories

### US-LEAD-004-01
**As a** Sales Representative  
**I want to** convert a qualified lead to an opportunity  
**So that** I can track the sales process in the pipeline

### US-LEAD-004-02
**As a** Sales Representative  
**I want to** create a customer record during conversion  
**So that** I have a complete customer profile

### US-LEAD-004-03
**As a** Sales Manager  
**I want to** see converted leads linked to opportunities  
**So that** I can track lead-to-opportunity conversion rate

---

## 4. Preconditions

| ID | Điều kiện |
|----|-----------|
| PRE-01 | Lead tồn tại và ở status QUALIFIED |
| PRE-02 | Lead có estimatedValue > 0 |
| PRE-03 | Người dùng có quyền CONVERT_LEAD |
| PRE-04 | Người dùng có quyền CREATE_OPPORTUNITY và/hoặc CREATE_CUSTOMER |

---

## 5. Postconditions

| ID | Điều kiện |
|----|-----------|
| POST-01 | Lead status được chuyển thành CONVERTED |
| POST-02 | Opportunity mới được tạo (nếu chọn) |
| POST-03 | Customer mới được tạo hoặc link với existing (nếu chọn) |
| POST-04 | Lead.convertedOpportunityId được set |
| POST-05 | Lead.convertedCustomerId được set |
| POST-06 | Contact được tạo cho Customer |

---

## 6. Business Rules

| Rule ID | Mô tả | Loại |
|---------|-------|------|
| BR-001 | Chỉ Lead QUALIFIED mới có thể convert | Constraint |
| BR-002 | Lead phải có estimatedValue > 0 để convert | Validation |
| BR-003 | Một Lead chỉ có thể convert một lần | Constraint |
| BR-004 | Opportunity được tạo với stage = PROSPECTING | Default |
| BR-005 | Customer được tạo với status = ACTIVE | Default |
| BR-006 | Thông tin Lead được copy sang Opportunity và Customer | Behavior |
| BR-007 | Contact chính được tạo từ thông tin Lead | Behavior |

---

## 7. Data Mapping

### Lead → Opportunity Mapping

| Lead Field | Opportunity Field |
|------------|-------------------|
| id | leadId |
| name | name |
| estimatedValue | estimatedValue |
| probability | probability |
| expectedCloseDate | expectedCloseDate |
| assignedTo | assignedTo |
| notes | notes |

### Lead → Customer Mapping

| Lead Field | Customer Field |
|------------|----------------|
| company | name |
| industry | industry |
| companySize | companySize |
| website | website |
| address | address |

### Lead → Contact Mapping

| Lead Field | Contact Field |
|------------|---------------|
| name | name |
| email | email |
| phone | phone |
| jobTitle | jobPosition |

---

## 8. Main Flow

| Step | Actor | System | Description |
|------|-------|--------|-------------|
| 1 | User | - | Truy cập Lead đã QUALIFIED |
| 2 | User | - | Click nút "Convert Lead" |
| 3 | - | System | Kiểm tra Lead có thể convert (QUALIFIED, estimatedValue > 0) |
| 4 | - | System | Hiển thị Convert Dialog với các options |
| 5 | User | - | Chọn: Create Opportunity (checkbox) |
| 6 | User | - | Chọn: Create New Customer / Link to Existing Customer |
| 7 | User | - | (Optional) Chỉnh sửa thông tin Opportunity |
| 8 | User | - | (Optional) Chỉnh sửa thông tin Customer |
| 9 | User | - | Click "Convert" |
| 10 | - | System | Begin Transaction |
| 11 | - | System | Tạo Customer mới (hoặc link existing) |
| 12 | - | System | Tạo Contact từ thông tin Lead |
| 13 | - | System | Tạo Opportunity với customerId |
| 14 | - | System | Cập nhật Lead: status = CONVERTED |
| 15 | - | System | Set Lead.convertedOpportunityId, convertedCustomerId |
| 16 | - | System | Commit Transaction |
| 17 | - | System | Publish events: LEAD_CONVERTED, OPPORTUNITY_CREATED, CUSTOMER_CREATED |
| 18 | - | System | Trả về kết quả conversion |
| 19 | User | - | Redirect đến Opportunity detail page |

---

## 9. Alternative Flows

### AF-01: Link to Existing Customer

| Step | Description |
|------|-------------|
| 6a | User chọn "Link to Existing Customer" |
| 6b | System hiển thị customer search dialog |
| 6c | User tìm và chọn existing customer |
| 6d | System link Lead với existing customer |
| 6e | System tạo Contact mới cho existing customer |

### AF-02: Create Opportunity Only

| Step | Description |
|------|-------------|
| 5a | User chỉ chọn "Create Opportunity" |
| 5b | System tạo Opportunity mà không tạo Customer |
| 5c | Opportunity.customerId = null |

### AF-03: Convert Without Opportunity

| Step | Description |
|------|-------------|
| 5a | User bỏ chọn "Create Opportunity" |
| 5b | System chỉ tạo Customer và Contact |
| 5c | Lead.convertedOpportunityId = null |

---

## 10. Exception Flows

### EF-01: Lead Not Qualified

| Condition | Response |
|-----------|----------|
| Lead status != QUALIFIED | HTTP 400 - "Lead cannot be converted. Ensure it is qualified and has a valid estimated value" |

### EF-02: Missing Estimated Value

| Condition | Response |
|-----------|----------|
| estimatedValue <= 0 hoặc null | HTTP 400 - "Lead must have estimated value > 0 to convert" |

### EF-03: Transaction Failed

| Condition | Response |
|-----------|----------|
| Lỗi trong quá trình transaction | Rollback all changes, HTTP 500 |

---

## 11. API Specification

### Endpoint

```
POST /crm/api/v1/leads/{leadId}/convert
```

### Request Body

```json
{
  "createOpportunity": true,
  "createCustomer": true,
  "existingCustomerId": null,
  "opportunityData": {
    "name": "ABC Company - Enterprise License",
    "expectedCloseDate": "2026-03-15",
    "notes": "High priority deal"
  },
  "customerData": {
    "name": "ABC Company Ltd",
    "creditLimit": 100000000
  }
}
```

### Response (Success - 200 OK)

```json
{
  "code": 200,
  "message": "Lead converted successfully",
  "data": {
    "leadId": 456,
    "leadStatus": "CONVERTED",
    "opportunity": {
      "id": 789,
      "name": "ABC Company - Enterprise License",
      "stage": "PROSPECTING",
      "estimatedValue": 75000000,
      "customerId": 101
    },
    "customer": {
      "id": 101,
      "name": "ABC Company Ltd",
      "activeStatus": "ACTIVE"
    },
    "contact": {
      "id": 202,
      "name": "Nguyễn Văn A",
      "email": "nguyenvana@abc.com",
      "isPrimary": true,
      "customerId": 101
    }
  }
}
```

---

## 12. Sequence Diagram

```
┌─────┐          ┌──────────┐          ┌─────────┐          ┌──────────┐          ┌──────────┐
│User │          │Controller│          │ UseCase │          │LeadSvc   │          │Database  │
└──┬──┘          └────┬─────┘          └────┬────┘          └────┬─────┘          └────┬─────┘
   │                  │                     │                    │                     │
   │ POST /convert    │                     │                    │                     │
   │─────────────────▶│                     │                    │                     │
   │                  │ convertLead()       │                    │                     │
   │                  │────────────────────▶│                    │                     │
   │                  │                     │ validateLead()     │                     │
   │                  │                     │───────────────────▶│                     │
   │                  │                     │◀───────────────────│                     │
   │                  │                     │                    │                     │
   │                  │                     │ BEGIN TRANSACTION  │                     │
   │                  │                     │────────────────────────────────────────▶│
   │                  │                     │                    │                     │
   │                  │                     │ createCustomer()   │                     │
   │                  │                     │────────────────────────────────────────▶│
   │                  │                     │◀────────────────────────────────────────│
   │                  │                     │                    │                     │
   │                  │                     │ createContact()    │                     │
   │                  │                     │────────────────────────────────────────▶│
   │                  │                     │◀────────────────────────────────────────│
   │                  │                     │                    │                     │
   │                  │                     │ createOpportunity()│                     │
   │                  │                     │────────────────────────────────────────▶│
   │                  │                     │◀────────────────────────────────────────│
   │                  │                     │                    │                     │
   │                  │                     │ updateLeadStatus() │                     │
   │                  │                     │────────────────────────────────────────▶│
   │                  │                     │◀────────────────────────────────────────│
   │                  │                     │                    │                     │
   │                  │                     │ COMMIT TRANSACTION │                     │
   │                  │                     │────────────────────────────────────────▶│
   │                  │◀────────────────────│                    │                     │
   │◀─────────────────│                     │                    │                     │
   │  Response        │                     │                    │                     │
```

---

## 13. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | Convert Lead với đầy đủ options | Lead CONVERTED, Opp + Customer + Contact created |
| TC-002 | Convert Lead không tạo Customer | Lead CONVERTED, Opp created, customerId = null |
| TC-003 | Convert Lead link existing Customer | Lead CONVERTED, Contact added to existing Customer |
| TC-004 | Convert Lead chưa QUALIFIED | Error - cannot convert |
| TC-005 | Convert Lead không có estimatedValue | Error - need estimatedValue |
| TC-006 | Convert Lead đã CONVERTED | Error - already converted |

---

## 14. Related Use Cases

| Use Case ID | Name | Relationship |
|-------------|------|--------------|
| UC-LEAD-003 | Qualify Lead | Precedes |
| UC-OPP-001 | Create Opportunity | Included |
| UC-CUST-001 | Create Customer | Included |
| UC-CONT-001 | Create Contact | Included |

---

## 15. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
