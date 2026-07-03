# Design Spec: Optimization Risks Tab Enhancement

## Goal

Improve the clarity, understandability, and actionability of the **Risks** tab in the PM Optimization Run Review page. 
Currently, the Risks tab shows a raw list of backend warnings with unformatted `detailsJson` blocks and no association to work items. This spec introduces:
1. Data aggregation (combining run-level `warnings` and item-level `violations`).
2. A unified, polished dashboard layout categorized by severity (Errors, Warnings, Info) and category.
3. Friendly, translated warning messages and parsed `detailsJson` data.
4. An interactive **Locate (Định vị)** mechanism that switches to the Review tab, scrolls to the item, and temporarily highlights it.

---

## Architectural Flow

```mermaid
graph TD
    A[OptimizationRunApi] --> B[Aggregate Risks]
    B -->|run.warnings| C[UnifiedRisk]
    B -->|run.items[].violations| C
    C --> D[PMOptimizationRunRisks Component]
    D --> E[Filter by Severity & Category]
    D --> F[Locate Button Click]
    F -->|Callback handleLocateItem| G[PMProjectOptimizationRunPage]
    G -->|State activeTab = 'review'| H[PMOptimizationRunReviewTable]
    G -->|State highlightedWorkItemId = ID| H
    H -->|Auto scroll & Pulsing style| I[Render Highlighted Row]
```

---

## Proposed Changes

### Component 1: `PMOptimizationRunRisks` [NEW]
A new component located at [PMOptimizationRunRisks.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/optimization/PMOptimizationRunRisks.tsx).

- **Data Parsing & Translation**:
  - Map `warning.code` to friendly Vietnamese labels.
  - Parse `detailsJson` to extract contextual parameters (e.g., predecessor work item, capacity values, missing skills) and render them cleanly instead of raw JSON.
- **Summary Cards**:
  - Show cards for Total, Errors, Warnings, and Info. Clicking a card sets the active severity filter.
- **Filtering Options**:
  - Search bar (filters by work item key/summary/text).
  - Category buttons: Dependencies, Capacity, Skills, System.
- **Grouped Risks List**:
  - Group risks by `workItemId`. Unassociated warnings are grouped under "Global / Project Risks".
  - Render each work item group with its Key, Summary, and a **Locate (Định vị)** button.

### Page 2: `PMProjectOptimizationRunPage` [MODIFY]
File: [PMProjectOptimizationRunPage.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/pages/PMProjectOptimizationRunPage.tsx)

- Add state `highlightedWorkItemId: number | null` (defaults to `null`).
- Implement `handleLocateItem(workItemId: number)`:
  1. Switch `activeTab` to `'review'`.
  2. Set `highlightedWorkItemId` to `workItemId`.
  3. Set a short `setTimeout` (100ms) to wait for rendering, find the element `review-item-${workItemId}`, and call `scrollIntoView({ behavior: 'smooth', block: 'center' })`.
  4. Set another `setTimeout` (2000ms) to reset `highlightedWorkItemId` to `null` to clear the highlight effect.
- Render the new `PMOptimizationRunRisks` component in the `<TabsContent value='risks'>` tab, passing the `run` data and `onLocateItem` callback.
- Pass `highlightedWorkItemId` down to `<PMOptimizationRunReviewTable>`.

### Component 3: `PMOptimizationRunReviewTable` [MODIFY]
File: [PMOptimizationRunReviewTable.tsx](file:///d:/User2/open_source/serp/serp_web/src/modules/pm/components/optimization/PMOptimizationRunReviewTable.tsx)

- Accept `highlightedWorkItemId?: number | null` in props.
- Add `id={`review-item-${item.workItemId}`}` to the row container wrapper.
- Apply a pulsing highlight style to the row if `highlightedWorkItemId === item.workItemId` (e.g., a soft yellow/amber background pulsing effect with border highlights).

---

## Detailed Formatting & Parsing Rules

### 1. Translation Maps
```typescript
const CODE_LABELS: Record<string, string> = {
  DEPENDENCY_CYCLE: 'Vòng lặp phụ thuộc',
  DEPENDENCY_VIOLATION: 'Vi phạm thứ tự phụ thuộc',
  EXTERNAL_DEPENDENCY: 'Phụ thuộc ngoài phạm vi',
  MISSING_ESTIMATE: 'Thiếu thời gian ước lượng',
  DEFAULT_DURATION_USED: 'Sử dụng thời lượng mặc định',
  LOW_CONFIDENCE_DURATION: 'Ước lượng thời gian độ tin cậy thấp',
  NO_ELIGIBLE_ASSIGNEE: 'Không tìm thấy người thực hiện phù hợp',
  OVER_CAPACITY: 'Quá tải tài nguyên',
  LATE_RISK: 'Rủi ro trễ hạn',
  STALE_ITEM: 'Dữ liệu gốc đã bị thay đổi',
  PERMISSION_DENIED: 'Không có quyền cập nhật công việc',
  INVALID_OVERRIDE: 'Ghi đè không hợp lệ',
  REQUIRED_SKILL_MISSING: 'Thiếu kỹ năng bắt buộc',
  PARTIAL_SKILL_MATCH: 'Chỉ đáp ứng một phần kỹ năng',
};
```

### 2. Smart detailsJson Parsing
- **`DEPENDENCY_VIOLATION` & `DEPENDENCY_CYCLE`**:
  If `detailsJson` contains a predecessor ID (e.g., `{"predecessorId": 123}` or a list), resolve it from other run items to display the friendly key (e.g., `PROJ-12` instead of ID `123`).
- **`OVER_CAPACITY`**:
  Format capacity details (e.g., `{"requiredCapacity": 40, "availableCapacity": 30}`) into *"Yêu cầu: 40h | Khả dụng: 30h (Quá tải 10h)"*.
- **Fallback**:
  Convert simple key-value pairs from parsed JSON into clean text labels instead of showing code block formatting.

---

## Verification Plan

### Manual Verification
1. Open the optimization run page.
2. Select the **Risks** tab and verify the counts in the Summary cards match the warnings/violations.
3. Apply filters (Severity & Category) and ensure the list updates correctly.
4. Click the **Locate (Định vị)** button on a risk linked to a specific work item.
5. Verify that the tab automatically switches to **Review**, scrolls directly to that item, and highlights it with a temporary yellow pulsing effect for 2 seconds.
