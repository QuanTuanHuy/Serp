# Báo cáo Phase 3 – Dọn dẹp Frontend School Bus

## Tổng quan

Phase 3 thực hiện dọn dẹp toàn bộ frontend `serp_web/src/modules/school-bus` để đồng bộ với backend Phase 2 đã hoàn tất (OSRM geometry, student-centric model). Kết quả: **17 file thay đổi, ~2.895 dòng xóa, ~117 dòng thêm**, type-check 0 lỗi, build thành công.

---

## 1. API Cleanup (`schoolBusApi.ts`)

**Đã xóa các endpoint deprecated:**
- `getSubscriptionPausePeriods` – không còn dùng
- `computeRoutePath` – thay bằng `getRoutePath` (backend tự tính OSRM)
- `validateRoute` – đã bỏ validate riêng
- Tất cả endpoint **Calculation Trace** (`getLatestRouteCalculationTrace`, `getRouteCalculationTraceHistory`)
- Tất cả endpoint **Window CRUD** (`createPickupPointWindow`, `updatePickupPointWindow`, `deletePickupPointWindow`)
- Tất cả endpoint **Objective Score** (`getRouteObjectiveScore`, `recalculateRouteObjectiveScore`, `getSessionObjectiveScores`)
- `generateGreedyForSession` – greedy algorithm deprecated

**Đã xóa import & re-export tương ứng** cho các hook RTK Query.

**Đã xóa invalidatesTags**: `SESSION-OBJECTIVE`, `OBJECTIVE-*`, `TRACE-LATEST`, `TRACE-HISTORY`.

---

## 2. Type Updates (`types/index.ts`)

**Đã xóa các field từ interface:**
- `Student`: bỏ `emergencyContactName`, `emergencyContactPhone`
- `PickupPoint` & `UpsertRequest`: bỏ `zoneCode`
- `SchoolBusRoute`: bỏ `estimatedCost`, `issueCount`, `blockingIssueCount`
- `SchoolBusRouteStop`: bỏ `plannedArrivalTime`, `plannedDepartureTime`
- `SchoolBusRouteDetail`: bỏ `issues`, `blockingIssues`, `warningIssues`
- `SchoolPickupPoint`: bỏ `windowStart`, `windowEnd`, `hasWindow`, `windows`, `pickupWindowCount`, `missingWindowCount`

**Đã xóa toàn bộ interface:**
- `SchoolBusRouteIssueDetail`
- `SchoolBusSchoolPickupPointWindow`, `SchoolPickupPointWindowUpsertRequest`, `PickupPointWindowDirection`
- `SchoolBusPlanningIssue`, `PlanningIssueSeverity`
- `SchoolBusRouteQuality`
- `SchoolBusGreedyGenerateResult`, `GreedyGenerateRequest`
- `SchoolBusRouteCalculationTrace`, `SchoolBusObjectiveScore`

**Đã cập nhật `SchoolBusRoutePlanStudent`:**
```typescript
// Mô hình mới: 1 student = 1 row với pickupStopId + dropoffStopId
{
  id, routeId, studentId, studentName,
  subscriptionId?, pickupStopId?, pickupPointName?,
  dropoffStopId?, dropoffPointName?
}
```

---

## 3. Planning Workspace UI (`PlanningResultsPanel.tsx`)

- Xóa toàn bộ **Objective Score UI** (score cards, radar chart)
- Xóa toàn bộ **Trace Panel** (calculation trace drawer)
- Xóa toàn bộ **Issue Badges** (blocking/warning issue indicators)
- Xóa toàn bộ **Greedy Result section** (~80 dòng) – generated routes + unassigned students
- Cập nhật student grouping từ `serviceAction: BOARD/DROPOFF` sang mô hình `pickupStopId/dropoffStopId`
- Xóa prop `greedyResult` khỏi interface `PlanningResultsPanelProps`
- Thêm thông báo placeholder cho chế độ tự động

---

## 4. Route Student Display (`PlanningResultsPanel.tsx`)

- Thay thế nhóm theo `serviceAction` bằng hiển thị trực tiếp `pickupPointName` / `dropoffPointName`
- Mỗi student hiện 1 dòng duy nhất với cả pickup và dropoff

---

## 5. Map Geometry (`RouteMapClient.tsx`, `PlanningMapClient.tsx`)

- **Đã xác nhận đúng format**: Backend trả `{latitude, longitude}` objects → Frontend map sang `[lat, lng]` tuples cho Leaflet
- Xóa reference `plannedArrivalTime` / `plannedDepartureTime` khỏi map tooltips
- `SchoolBusRoutePath.coordinates` sử dụng `SchoolBusRoutePathCoordinate` với `geometrySource` hỗ trợ `ROAD_NETWORK | STRAIGHT_LINE_ESTIMATE | NONE`

---

## 6. Window / Pickup Point Removal

### `WindowFormDialog.tsx`
- Thay toàn bộ component (~370 dòng) bằng stub trống (no-op export)

### `SchoolBusSchoolsPage.tsx`
- Xóa import & sử dụng `useCreatePickupPointWindowMutation`, `useUpdatePickupPointWindowMutation`, `useDeletePickupPointWindowMutation`
- Xóa import `SchoolBusSchoolPickupPointWindow`
- Xóa `zoneCode` khỏi column definition, filter, và form
- Xóa bảng window con trong pickup point detail

### `SchoolBusSchoolDetailPage.tsx`
- Xóa `expandedSppId` state, window action buttons, window table
- Xóa import & render `WindowFormDialog`
- Xóa `deletingWindow` state và handlers

---

## 7. Student Form (`SchoolBusMasterDataForms.tsx`)

- Xóa `emergencyContactName` và `emergencyContactPhone` khỏi Zod schema, default values, submit handler, và form render
- Xóa `zoneCode` khỏi pickup point form (schema, defaults, submit, render)

---

## 8. Request / Subscription UI

### `SubscriptionHistoryDialog.tsx`
- Xóa `useGetSubscriptionPausePeriodsQuery` import
- Xóa hiển thị pause period data
- Đơn giản hóa timeline

### `SchoolBusSubscriptionDetailPage.tsx`
- Xóa pause periods hook & UI section
- Đơn giản hóa timeline hiển thị

---

## 9. Label Helper (`schoolBusLabels.ts`) — **FILE MỚI**

Tạo file `serp_web/src/modules/school-bus/schoolBusLabels.ts` chứa mapping Vietnamese cho:
- `routeStatusLabel`: PLANNED → Đã lên kế hoạch, ASSIGNED → Đã phân công, ...
- `requestStatusLabel`: DRAFT → Nháp, SUBMITTED → Chờ duyệt, APPROVED → Đã duyệt, ...
- `subscriptionStatusLabel`: ACTIVE → Đang hoạt động, INACTIVE → Ngưng hoạt động, ...
- `sessionStatusLabel`: DRAFT → Nháp, IN_PROGRESS → Đang thực hiện, ...
- `directionLabel`: TO_SCHOOL → Đến trường, FROM_SCHOOL → Về nhà
- `usageTypeLabel`: ONE_WAY_TO → Một chiều – Đến, ROUND_TRIP → Hai chiều, ...
- Helper `getLabel(map, key)` cho tra cứu an toàn

---

## 10. Dispatch Page (`SchoolBusDispatchPage.tsx`)

- Xóa `useComputeRoutePathMutation` import & usage (nút "Compute Path")
- Xóa `useGetRouteObjectiveScoreQuery` & `useRecalculateRouteObjectiveScoreMutation`
- Xóa objective score widget trong route detail sidebar
- Giữ nguyên route status badge & route list

---

## 11. Route Detail Page (`SchoolBusRouteDetailPage.tsx`)

**Xóa ~1.100 dòng:**
- 6 deprecated import hooks (`useComputeRoutePathMutation`, `useValidateRouteMutation`, `useGetLatestRouteCalculationTraceQuery`, v.v.)
- Toàn bộ trace handlers & state
- Issue panel (blocking issues, warning issues badges & lists)
- Score section (objective score cards)
- Trace drawer (~470 dòng UI)
- `blockingIssueCount`, `issueCount` references
- `plannedArrivalTime`, `plannedDepartureTime` trong stop timeline

---

## 12. Planning Page (`SchoolBusRoutePlanningPage.tsx`)

- Xóa `useGenerateGreedyForSessionMutation` & `SchoolBusGreedyGenerateResult` import
- Xóa `greedyResult` state & `handleGenerate` callback
- Xóa `greedyResult={null}` prop truyền cho `PlanningResultsPanel`
- Sửa `hasBlockingIssues` logic (không còn field `blockingIssueCount`)

---

## 13. Dashboard / Menu

### `SchoolBusDashboardPage.tsx`
- Đã review, giữ nguyên cấu trúc chính
- Chart data từ backend (`requestStatusChart`) vẫn hoạt động

---

## 14. Manual Demand Assign (`ManualDemandAssignPanel.tsx`)

- Xóa `windowStart` / `windowEnd` khỏi eligible student display cards

---

## 15. Kết quả kiểm tra

| Kiểm tra | Kết quả |
|----------|---------|
| `npx tsc --noEmit` | ✅ 0 lỗi, exit code 0 |
| `npm run build` | ✅ Thành công, exit code 0 |
| Tổng file thay đổi | 17 (16 sửa + 1 mới) |
| Dòng xóa | ~2.895 |
| Dòng thêm | ~117 |

---

## 16. Danh sách file đã thay đổi

| File | Thay đổi |
|------|----------|
| `api/schoolBusApi.ts` | -257 dòng (xóa endpoint deprecated) |
| `types/index.ts` | -153 dòng (xóa type/interface deprecated) |
| `components/forms/WindowFormDialog.tsx` | -373 dòng (thay bằng stub) |
| `components/SchoolBusMasterDataForms.tsx` | -17 dòng (xóa zoneCode, emergencyContact) |
| `components/history/SubscriptionHistoryDialog.tsx` | ±12 dòng (xóa pause periods) |
| `components/map/PlanningMapClient.tsx` | -3 dòng (xóa plannedArrivalTime) |
| `components/map/RouteMapClient.tsx` | -5 dòng (xóa plannedArrivalTime) |
| `components/planning/ManualDemandAssignPanel.tsx` | ±9 dòng (xóa window fields) |
| `components/planning/PlanningResultsPanel.tsx` | -514 dòng (xóa greedy/score/trace/issues) |
| `pages/SchoolBusDashboardPage.tsx` | ±26 dòng |
| `pages/SchoolBusDispatchPage.tsx` | -149 dòng (xóa compute/score) |
| `pages/SchoolBusRouteDetailPage.tsx` | -1.103 dòng (xóa trace/issues/score) |
| `pages/SchoolBusRoutePlanningPage.tsx` | -63 dòng (xóa greedy) |
| `pages/SchoolBusSchoolDetailPage.tsx` | -158 dòng (xóa window UI) |
| `pages/SchoolBusSchoolsPage.tsx` | -136 dòng (xóa zoneCode/window) |
| `pages/SchoolBusSubscriptionDetailPage.tsx` | -34 dòng (xóa pause periods) |
| `schoolBusLabels.ts` | **+65 dòng (file mới – label helper)** |
