# Báo cáo: Đơn giản hóa UI Planning Workspace

**Ngày:** $(date)  
**Module:** School Bus → Route Planning (`/school-bus/dispatch/planning`)

---

## 1. KPI Cards — Xóa "Blocked"

- Xóa KPI card "Blocked" (màu đỏ) khỏi panel Demand Preview.
- Grid chuyển từ `grid-cols-4` → `grid-cols-3` (Total, Eligible, Points).
- Summary text trên header tab cũng sửa: `Total · Eligible · Points` thay vì `Total · Eligible · Blocked`.

## 2. Xóa tab "Overview" (Readiness Diagnostics)

- Xóa toàn bộ tab "Overview" gồm: block Readiness Diagnostics (paused/inactive/day mismatch/expired/missing coords) và block "All Clear".
- Tab mặc định bây giờ là "Students" thay vì "Overview".
- Chỉ còn 3 tab: **Students**, **Points**, **Context**.

## 3. Tab Students — Đơn giản hóa

- Xóa filter bar "all / eligible / blocked" (3 nút lọc).
- Xóa badge readinessStatus ("Eligible" / "Blocked") trên mỗi card.
- Xóa issue labels (dòng lỗi đỏ dưới student card khi blocked).
- Xóa viền đỏ trái cho student bị blocked.
- Mỗi student card bây giờ chỉ hiển thị: Tên, mã HS, trip option (tiếng Việt: "Một chiều" / "Hai chiều"), pickup point.

## 4. Tab Points — Đơn giản hóa

- Xóa badge readinessStatus ("✅ Ready" / "⚠️ Blocked").
- Xóa issueLabels hiển thị bên dưới point card.
- Thay bằng dòng nhỏ "Chưa có tọa độ" (amber) khi point thiếu lat/lng.
- Point card: tên, mã, số học sinh, cảnh báo tọa độ nếu có.

## 5. Tab Context — Sửa label Planning Method

- Dòng "Greedy auto-generate" → **"Lập tuyến thủ công"** (hardcode MANUAL).
- Biến `methodVal` không còn tham chiếu `form.planningMethod` hay `preview.planningMethod`.

## 6. PlanningContextPanel — Xóa dropdown Planning Method

- Xóa dropdown "Planning Method" (Greedy/Manual).
- Depot selector và Bus Capacity luôn hiển thị (không còn conditionally cho GREEDY).
- Xóa label "Depot is required for greedy generation." (validation message).
- Xóa prop `hasBlockingIssues` từ interface và nơi truyền.
- Nút "Create" không còn bị disable bởi `hasBlockingIssues`.

## 7. SchoolBusRoutePlanningPage — Subtitle & Default

- Default `planningMethod` chuyển từ `'GREEDY'` → `'MANUAL'`.
- Subtitle page: `'Build student routes by pickup points, vehicle capacity, and service date.'`
- Xóa biến `hasBlockingIssues` và tất cả nơi truyền prop.
- Xóa `readinessStatus` khỏi `mapPickupPoints` mapping.

## 8. PlanningMapClient — Xóa Straight-line Fallback

- Xóa biến `isFallback` (fallbackUsed / STRAIGHT_LINE_ESTIMATE / no coords check).
- Polyline chỉ được vẽ khi có **real road geometry** (≥2 actual path coordinates).
- Khi không có geometry: chỉ hiển thị markers, **không vẽ đường thẳng**.
- Xóa warning banner "⚠ Straight-line estimate — road geometry unavailable" (amber).
- Thay bằng dòng nhỏ "Chưa tính được đường đi thực tế." (neutral slate) khi có stops nhưng chưa có geometry.

## 9. Frontend Types — Xóa fields không dùng

Từ `types/index.ts`:
- `PlanningReadinessSummary`: xóa `blockedStudents`, `warningStudents`, `missingCoordinateCount`, `pausedCount`, `inactiveCount`, `outOfEffectiveRangeCount`, `dayMismatchCount`, `missingWindowCount`.
- `PlanningDemandResponse`: xóa `readinessStatus`, `reasonCode`, `reasonLabel`, `issueCodes`, `issueLabels`.
- `PlanningPointResponse`: xóa `readinessStatus`, `issueLabels`.
- Xóa interface `PlanningReadinessIssueResponse` (không còn trả ra).
- `SchoolBusPlanningPreview`: xóa `blockedDemands`.

## 10. Backend DTOs — Xóa fields

- `PlanningPreviewResponse.java`: xóa `blockedDemands`, `issues`.
- `PlanningDemandResponse.java`: xóa `readinessStatus`, `reasonCode`, `reasonLabel`, `issueCodes`, `issueLabels`. Xóa import `List`.
- `PlanningPointResponse.java`: xóa `readinessStatus`, `issueLabels`. Xóa import `List`.
- `PlanningReadinessSummary.java`: xóa `blockedStudents`, `warningStudents`, `missingCoordinateCount`, `missingWindowCount`, `pausedCount`, `inactiveCount`, `outOfEffectiveRangeCount`, `dayMismatchCount`.
- `RoutePlanningSessionServiceImpl.java`: xóa tất cả setter tương ứng (`setBlockedStudents`, `setWarningStudents`, `setMissingCoordinateCount`, `setReadinessStatus`, `setBlockedDemands`). Xóa biến `missingCoordCount`.

## 11. Build Verification

- `npx tsc --noEmit` → **EXIT:0** (0 errors)
- `./mvnw compile -q` → **EXIT:0** (0 errors)

---

## Tổng kết

| Hạng mục | Trước | Sau |
|----------|-------|-----|
| KPI cards | 4 (Total, Eligible, **Blocked**, Points) | 3 (Total, Eligible, Points) |
| Tabs | 4 (Overview, Students, Points, Context) | 3 (Students, Points, Context) |
| Student card | Badge + issue labels + red border | Tên, mã, trip option, point |
| Point card | Badge + issue labels | Tên, mã, student count, cảnh báo tọa độ |
| Map fallback | Dashed amber line + warning | Chỉ markers + message trung tính |
| Planning method | Dropdown (Greedy/Manual) | Hardcode MANUAL, luôn hiện depot/capacity |
| Backend response | `blockedDemands`, `issues`, readiness fields | Chỉ `eligibleDemands`, `points`, summary đơn giản |
