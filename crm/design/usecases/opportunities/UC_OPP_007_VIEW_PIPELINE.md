# UC-OPP-007: Xem Pipeline

## 1. Thông tin chung

| Thuộc tính | Giá trị |
|------------|---------|
| **Use Case ID** | UC-OPP-007 |
| **Use Case Name** | Xem Pipeline (View Pipeline) |
| **Actor** | Sales Representative, Sales Manager |
| **Priority** | High |
| **Status** | Implemented |
| **Version** | 1.0 |
| **Last Updated** | 2025-12-10 |

---

## 2. Mô tả

Use case này cho phép xem toàn bộ sales pipeline với các Opportunities được phân theo stage, cho phép drag-drop để di chuyển qua các stage.

---

## 3. User Stories

### US-OPP-007-01
**As a** Sales Manager  
**I want to** see all opportunities in a Kanban board view  
**So that** I can visualize the sales pipeline

### US-OPP-007-02
**As a** Sales Manager  
**I want to** see pipeline value by stage  
**So that** I can forecast revenue accurately

---

## 4. Pipeline View Components

### Kanban Board
```
┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐
│PROSPECTING │ │QUALIFICATION│ │  PROPOSAL  │ │NEGOTIATION │ │   CLOSED   │
│            │ │            │ │            │ │            │ │            │
│ ┌────────┐ │ │ ┌────────┐ │ │ ┌────────┐ │ │ ┌────────┐ │ │ ┌────────┐ │
│ │ Deal 1 │ │ │ │ Deal 3 │ │ │ │ Deal 5 │ │ │ │ Deal 7 │ │ │ │ Deal 9 │ │
│ │ $50K   │ │ │ │ $80K   │ │ │ │ $120K  │ │ │ │ $200K  │ │ │ │ $150K  │ │
│ └────────┘ │ │ └────────┘ │ │ └────────┘ │ │ └────────┘ │ │ │ WON ✓  │ │
│ ┌────────┐ │ │ ┌────────┐ │ │            │ │            │ │ └────────┘ │
│ │ Deal 2 │ │ │ │ Deal 4 │ │ │            │ │            │ │ ┌────────┐ │
│ │ $30K   │ │ │ │ $60K   │ │ │            │ │            │ │ │ Deal 10│ │
│ └────────┘ │ │ └────────┘ │ │            │ │            │ │ │ $100K  │ │
│            │ │            │ │            │ │            │ │ │ LOST ✗ │ │
│────────────│ │────────────│ │────────────│ │────────────│ │ └────────┘ │
│ Total:$80K │ │Total:$140K │ │Total:$120K │ │Total:$200K │ │Total:$250K │
└────────────┘ └────────────┘ └────────────┘ └────────────┘ └────────────┘
```

### Summary Metrics
- Total Pipeline Value
- Weighted Pipeline Value (based on probability)
- Number of deals per stage
- Average deal size
- Win/Loss ratio

---

## 5. API Specification

### Get Pipeline Summary
```
GET /crm/api/v1/opportunities/pipeline
```

### Query Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| assignedTo | Long | Filter by owner |
| customerId | Long | Filter by customer |
| fromDate | Date | Expected close from |
| toDate | Date | Expected close to |

### Response (Success - 200 OK)
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stages": [
      {
        "stage": "PROSPECTING",
        "count": 10,
        "totalValue": 500000000,
        "weightedValue": 50000000,
        "opportunities": [...]
      },
      {
        "stage": "QUALIFICATION",
        "count": 8,
        "totalValue": 400000000,
        "weightedValue": 100000000,
        "opportunities": [...]
      },
      {
        "stage": "PROPOSAL",
        "count": 5,
        "totalValue": 300000000,
        "weightedValue": 150000000,
        "opportunities": [...]
      },
      {
        "stage": "NEGOTIATION",
        "count": 3,
        "totalValue": 200000000,
        "weightedValue": 150000000,
        "opportunities": [...]
      }
    ],
    "summary": {
      "totalOpportunities": 26,
      "totalPipelineValue": 1400000000,
      "weightedPipelineValue": 450000000,
      "averageDealSize": 53846154
    }
  }
}
```

---

## 6. Test Cases

| TC ID | Scenario | Expected Result |
|-------|----------|-----------------|
| TC-001 | View full pipeline | Hiển thị tất cả stages |
| TC-002 | Filter by assignedTo | Chỉ hiển thị opportunities của user |
| TC-003 | Drag-drop giữa stages | Opportunity được move |
| TC-004 | Verify weighted values | Calculated correctly |

---

## 7. Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-10 | QuanTuanHuy | Initial version |
