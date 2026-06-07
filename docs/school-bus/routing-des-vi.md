# Thiết kế tính toán ma trận định tuyến và timeline tuyến xe School Bus

Tài liệu này mô tả kiến trúc định tuyến theo cấu hình động, cơ chế fallback bằng khoảng cách đường chim bay, dịch vụ sinh ma trận khoảng cách/thời gian `N x N`, và logic tính timeline cho các điểm dừng trong module School Bus.

---

## 1. Cấu hình định tuyến động (`school_bus_app_config`)

Để có thể hiệu chỉnh tham số khi demo, mô phỏng hoặc vận hành mà không cần sửa source code, các tham số định tuyến được lấy động từ bảng cấu hình dùng chung của module School Bus. Bảng này là cấu hình cấp module, không phụ thuộc `tenant_id`.

### Các tham số cấu hình

| Mã cấu hình | Kiểu dữ liệu | Giá trị mặc định | Mô tả |
|---|---:|---:|---|
| `ROUTING_AVERAGE_SPEED_KMPH` | `DECIMAL` | `25.0` | Tốc độ trung bình của xe, dùng để ước lượng thời gian di chuyển khi dùng fallback. |
| `ROUTING_DWELL_TIME_MINUTES` | `INTEGER` | `2` | Thời gian dừng mặc định tại mỗi điểm đón/trả học sinh. |
| `ROUTING_ROAD_FACTOR` | `DECIMAL` | `1.3` | Hệ số nhân vào khoảng cách đường chim bay để xấp xỉ khoảng cách đường bộ thực tế. |
| `ROUTING_OSRM_ENABLED` | `BOOLEAN` | `true` | Bật/tắt việc gọi OSRM. Nếu OSRM tắt hoặc lỗi, hệ thống dùng fallback. |
| `ROUTING_WEIGHT_DISTANCE` | `DECIMAL` | `1.0` | Hệ số trọng số cho chi phí khoảng cách trong hàm mục tiêu. |
| `ROUTING_WEIGHT_DURATION` | `DECIMAL` | `1.0` | Hệ số trọng số cho chi phí thời gian trong hàm mục tiêu. |
| `ROUTING_WEIGHT_ROUTE_COUNT` | `DECIMAL` | `10.0` | Trọng số phạt cho mỗi tuyến xe hoạt động trong hàm mục tiêu giải pháp. |
| `ROUTING_WEIGHT_UNASSIGNED` | `DECIMAL` | `1000.0` | Trọng số phạt cho mỗi học sinh không được gán vào tuyến trong hàm mục tiêu giải pháp. |
| `ROUTING_WEIGHT_WAIT_TIME` | `DECIMAL` | `0.5` | Hệ số trọng số cho thời gian học sinh phải chờ trên xe. |
| `ROUTING_WEIGHT_BLOCKING_ISSUE` | `DECIMAL` | `10000.0` | Trọng số phạt cho mỗi vi phạm nghiêm trọng (blocking issue). |
| `ROUTING_WEIGHT_WARNING_ISSUE` | `DECIMAL` | `50.0` | Trọng số phạt cho mỗi cảnh báo (warning issue). |
| `ROUTING_WEIGHT_CAPACITY_EXCESS` | `DECIMAL` | `10000.0` | Trọng số phạt cho mỗi học sinh vượt quá sức chứa tối đa của xe buýt. |
| `ROUTING_WEIGHT_LOAD_BALANCE` | `DECIMAL` | `2.0` | Hệ số phạt cho sự lệch tải (không cân bằng số học sinh) giữa các tuyến. |

> [!NOTE]
> Tất cả các trọng số được lấy động từ bảng `school_bus_app_config` tại thời điểm chạy thuật toán/đánh giá, cho phép cấu hình linh hoạt mà không cần sửa mã nguồn.

---

## 2. Bộ đọc cấu hình (`RoutingConfigResolver`)

`RoutingConfigResolver` là lớp trung gian tập trung cho việc đọc các tham số routing. Các service định tuyến không đọc trực tiếp repository cấu hình, mà đi qua resolver để đảm bảo logic đọc cấu hình thống nhất.

Luồng đọc cấu hình:

```txt
Routing service
→ RoutingConfigResolver
→ AppConfigService
→ SchoolBusAppConfigRepository.findFirstByConfigCodeAndIsActiveTrueAndIsDeletedFalse(...)
```

Các mã cấu hình được định nghĩa tập trung trong class `AppConfigCode` để tránh hard-code string rải rác trong code.

Resolver có trách nhiệm:

- Đọc cấu hình theo `config_code`.
- Chuyển đổi `config_value` sang kiểu dữ liệu phù hợp.
- Ghi log cảnh báo nếu cấu hình bị thiếu hoặc parse lỗi.
- Dùng giá trị mặc định an toàn để không làm vỡ pipeline planning/routing.

Giá trị fallback mặc định:

```txt
averageSpeedKmph = 25.0
routingDwellTimeMinutes = 2
roadFactor = 1.3
osrmEnabled = true
```

---

## 3. Fallback định tuyến bằng khoảng cách đường chim bay

Khi OSRM bị tắt qua cấu hình hoặc không thể gọi thành công, hệ thống dùng `StraightLineFallbackRoutingEngineServiceImpl` để ước lượng khoảng cách và thời gian.

### 3.1. Ước lượng khoảng cách

Hệ thống tính khoảng cách Haversine giữa 2 tọa độ, sau đó nhân với `ROUTING_ROAD_FACTOR` để xấp xỉ khoảng cách đường bộ:

```txt
estimatedRoadDistanceKm = haversineDistanceKm × roadFactor
```

### 3.2. Ước lượng thời gian

Sau khi có khoảng cách xấp xỉ đường bộ, hệ thống tính thời gian di chuyển dựa trên `ROUTING_AVERAGE_SPEED_KMPH`:

```txt
durationMinutes = estimatedRoadDistanceKm / averageSpeedKmph × 60
```

Fallback này không chính xác bằng OSRM, nhưng giúp hệ thống vẫn có thể demo, tính timeline và kiểm tra feasibility trong trường hợp OSRM lỗi hoặc không có mạng.

---

## 4. Dịch vụ sinh ma trận định tuyến `N x N`

Để phục vụ tính toán nhiều điểm dừng, frontend, manual route validation và các thuật toán route planning ở phase sau, backend cung cấp dịch vụ sinh ma trận khoảng cách/thời gian giữa mọi cặp điểm.

### API Endpoint

```txt
POST /routes/matrix
```

### Request Body

```txt
List<RoutingPointRequest>
```

Mỗi điểm gồm các thông tin chính:

```txt
id
latitude
longitude
```

### Response Body

```txt
RoutingMatrixResponse
```

Response gồm:

```txt
durations: mảng 2 chiều, đơn vị phút
distances: mảng 2 chiều, đơn vị km
```

Trong đó:

```txt
durations[i][j] = thời gian di chuyển từ điểm i đến điểm j
distances[i][j] = khoảng cách từ điểm i đến điểm j
```

### Chiến lược tính toán

1. Nếu `ROUTING_OSRM_ENABLED = true`, hệ thống ưu tiên gọi OSRM Table API.
2. Nếu OSRM bị tắt hoặc gọi lỗi, hệ thống fallback sang tính từng cặp điểm bằng Haversine + `roadFactor`.

> TODO Phase 4/5: Ma trận sẽ được build trực tiếp từ planning session/context để manual validation và greedy generation không cần frontend tự truyền danh sách điểm thủ công.

---

## 5. Tính timeline và kiểm tra issue (`TimelineCalculatorService`)

`TimelineCalculatorService` chịu trách nhiệm tính thời gian đến/đi dự kiến tại từng stop, cập nhật mốc bắt đầu/kết thúc route, và sinh các issue feasibility nếu route vi phạm ràng buộc vận hành.

### 5.1. Quy tắc dwell time

| Loại điểm dừng | Dwell time |
|---|---:|
| `DEPOT` | `0` phút |
| `SCHOOL` | `0` phút |
| `PICKUP` | Theo `ROUTING_DWELL_TIME_MINUTES` |
| `DROPOFF` | Theo `ROUTING_DWELL_TIME_MINUTES` |

Ở Phase 3, dwell time tại mỗi điểm đón/trả là một giá trị mặc định cấu hình chung. Các phase sau có thể mở rộng dwell time theo số lượng học sinh tại stop.

---

## 5.2. Timeline cho tuyến OUTBOUND, chiều đi học

OUTBOUND là chiều từ nhà đến trường:

```txt
Depot → Pickup points → School
```

Mục tiêu quan trọng nhất là xe phải đến trường không muộn hơn `arrivalDeadline` của schedule.

Vì vậy timeline OUTBOUND được tính ngược từ trường về depot.

### Quy tắc tính

1. Điểm cuối `SCHOOL`:

```txt
plannedArrivalTime = arrivalDeadline
plannedDepartureTime = arrivalDeadline
```

2. Duyệt ngược từ điểm trước trường về điểm đầu:

```txt
plannedDepartureTime[i] = plannedArrivalTime[i + 1] - travelTime(i → i + 1)
plannedArrivalTime[i] = plannedDepartureTime[i] - dwellTime[i]
```

3. Kết quả route:

```txt
plannedStartTime = plannedDepartureTime của DEPOT
plannedEndTime = arrivalDeadline
```

Cách tính ngược này giúp đảm bảo stop cuối tại trường luôn được căn theo deadline của lịch học.

---

## 5.3. Timeline cho tuyến RETURN, chiều tan học

RETURN là chiều từ trường về nhà:

```txt
School → Drop-off points → Depot
```

Xe bắt đầu từ trường tại `departureTime` của schedule.

### Quy tắc tính

1. Điểm đầu `SCHOOL`:

```txt
plannedArrivalTime = departureTime
plannedDepartureTime = departureTime
```

2. Duyệt xuôi từ trường đến depot:

```txt
plannedArrivalTime[i] = plannedDepartureTime[i - 1] + travelTime(i - 1 → i)
plannedDepartureTime[i] = plannedArrivalTime[i] + dwellTime[i]
```

3. Kết quả route:

```txt
plannedStartTime = departureTime
plannedEndTime = plannedArrivalTime của DEPOT
```

---

## 5.4. Kiểm tra feasibility và sinh issue

Trong quá trình tính timeline, route được kiểm tra theo các ràng buộc vận hành. Nếu phát hiện vi phạm, hệ thống sinh `RoutePlanningIssueEntity` và lưu xuống cơ sở dữ liệu.

| Issue code | Mức độ | Mô tả | Điều kiện phát sinh |
|---|---|---|---|
| `MISSING_COORDINATES` | `BLOCKING` | Điểm dừng thiếu tọa độ | Stop không có latitude hoặc longitude. |
| `MATRIX_CELL_MISSING` | `BLOCKING` | Thiếu dữ liệu ma trận di chuyển | Không có distance/travel time từ stop trước đến stop hiện tại. |
| `MISSING_TIME_WINDOW` | `BLOCKING` | Thiếu time window đón/trả | Không có time window phù hợp với chiều route của stop. |
| `TIME_WINDOW_LATE` | `BLOCKING` | Đến muộn hơn time window | Thời gian xe đến stop vượt quá `windowEnd`. |
| `SCHOOL_ARRIVAL_DEADLINE_MISSED` | `BLOCKING` | Đến trường muộn | OUTBOUND đến trường sau `arrivalDeadline`. |
| `OSRM_FALLBACK_USED` | `INFO` | Đã dùng fallback | OSRM bị tắt hoặc lỗi nên hệ thống dùng Haversine fallback. |

### Quy tắc xử lý time window

- Nếu xe đến sớm hơn `windowStart`, xe có thể chờ, không tính là vi phạm.
- Nếu xe đến sau `windowEnd`, route bị issue `TIME_WINDOW_LATE`.
- OUTBOUND kiểm tra time window theo hướng `PICKUP_TO_SCHOOL`.
- RETURN kiểm tra time window theo hướng `DROPOFF_FROM_SCHOOL`.

### Dọn issue trùng lặp khi tính lại

Khi route được tính lại do thay đổi stop, reorder stop hoặc người dùng bấm compute nhiều lần, hệ thống soft-delete các issue cũ của route trước khi sinh issue mới:

```txt
is_deleted = true
is_active = false
```

Điều này giúp tránh tình trạng một lỗi bị ghi lặp nhiều lần sau mỗi lần tính lại.

---

## 5.5. Output của Phase 3

Phase 3 không tính objective score hoặc quality score.

Phase 3 chỉ tạo ra:

```txt
planned stop timeline
routing issues
issueCount
blockingIssueCount
route feasibility status nếu entity/DTO hiện có hỗ trợ
```

Hàm mục tiêu chính thức được hoãn sang Phase 6. Khi đó hệ thống sẽ dùng các trọng số cấu hình trong `school_bus_app_config`, ví dụ:

```txt
distance weight
duration weight
route count weight
unassigned student weight
time-window violation weight
```

---

## 6. Ví dụ tính timeline

### 6.1. Ví dụ OUTBOUND

Input:

```txt
Depot → Pickup A → Pickup B → School
Arrival deadline: 07:00
Dwell time: 2 phút
Travel times:
  Depot → A = 8 phút
  A → B = 6 phút
  B → School = 12 phút
```

Tính ngược:

```txt
1. School:
   Planned Arrival = 07:00
   Planned Departure = 07:00

2. Pickup B:
   Planned Departure = 07:00 - 12 phút = 06:48
   Planned Arrival = 06:48 - 2 phút = 06:46

3. Pickup A:
   Planned Departure = 06:46 - 6 phút = 06:40
   Planned Arrival = 06:40 - 2 phút = 06:38

4. Depot:
   Planned Departure = 06:38 - 8 phút = 06:30
   Planned Arrival = 06:30
```

Output:

```txt
Depot departure = 06:30
Pickup A arrival = 06:38, departure = 06:40
Pickup B arrival = 06:46, departure = 06:48
School arrival = 07:00
```

### 6.2. Ví dụ RETURN

Input:

```txt
School → Drop-off A → Depot
Departure time: 11:00
Dwell time: 2 phút
Travel times:
  School → A = 10 phút
  A → Depot = 15 phút
```

Tính xuôi:

```txt
1. School:
   Planned Arrival = 11:00
   Planned Departure = 11:00

2. Drop-off A:
   Planned Arrival = 11:00 + 10 phút = 11:10
   Planned Departure = 11:10 + 2 phút = 11:12

3. Depot:
   Planned Arrival = 11:12 + 15 phút = 11:27
   Planned Departure = 11:27
```

Output:

```txt
School departure = 11:00
Drop-off A arrival = 11:10, departure = 11:12
Depot arrival = 11:27
```

---

## 7. Lưu trữ dấu vết tính toán định tuyến (Route Calculation Trace Persistence)

Để phục vụ kỹ thuật audit, debug thuật toán, minh họa tính toán ma trận và làm nguồn dữ liệu cho việc xuất báo cáo Excel (Phase 3.2)/chạy benchmark (Phase 7), hệ thống sẽ tự động lưu lại snapshot dấu vết của mỗi lượt tính toán định tuyến.

### Bảng cơ sở dữ liệu: `school_bus_route_calculation_trace`

Khác với các đối tượng vận hành thông thường (chẳng hạn như `RoutePlanningIssue` - sẽ bị xóa mềm và tạo lại mỗi lần tính để phản ánh trạng thái hiện tại của route), các trace tính toán định tuyến được lưu giữ lịch sử để phục vụ audit.

Bản ghi trace bao gồm:
- **Operational IDs**: Liên kết đến `route_plan_id`, `planning_session_id`, và `tenant_id`.
- **Calculation Type & Status**: Kiểu tính toán `MATRIX_AND_TIMELINE` và trạng thái `SUCCESS`, `PARTIAL`, hoặc `FAILED`.
- **input_json**: Snapshot thông số đầu vào (mã route, hướng, danh sách stop và thứ tự, depot, ngày phục vụ).
- **matrix_json**: Ma trận khoảng cách và thời gian di chuyển, bao gồm cả nguồn công cụ định tuyến (`OSRM` hoặc `FALLBACK`).
- **timeline_json**: Thời gian đến/đi thực tế được tính toán cho từng stop, khoảng cách/thời gian di chuyển từ điểm dừng trước đó và cấu hình dwell time.
- **issues_json**: Danh sách serialized các vấn đề khả thi được sinh ra trong lượt tính toán này.
- **config_snapshot_json**: Snapshot các tham số cấu hình định tuyến hoạt động tại thời điểm tính toán từ `school_bus_app_config`.
- **source_summary**: Chuỗi mô tả công cụ định tuyến (ví dụ: `OSRM` hoặc `STRAIGHT_LINE_FALLBACK`).

> [!NOTE]
> **Chính sách dọn dẹp Trace**: Hiện tại mỗi lần recalculate sẽ append thêm một bản ghi trace mới. Trong tương lai, chính sách lưu giữ và dọn dẹp (retention policy) có thể được áp dụng để prune các log trace cũ (TODO).

### 7.1. Xuất Excel dấu vết tính toán (Phase 3.2)

Để hỗ trợ việc debug và báo cáo/thanh tra ngoại tuyến, người dùng có thể tải file Excel kết xuất chi tiết trace.

> [!IMPORTANT]
> **Phạm vi của Phase 3.2**:
> Phase 3.2 chỉ export dấu vết tính toán của tuyến đã tồn tại. Tức là đã có `routePlanId` và route stop order, sau đó hệ thống tính timeline/issues rồi lưu trace. Export này không phải là export ma trận đầu vào trước khi tạo route (Planning Matrix Export).
>
> **TODO Phase 5/7**:
> Lưu và export ma trận N x N theo planning context trước khi tạo route. Ma trận này phục vụ thuật toán greedy và benchmark thực nghiệm.

- **Luồng xử lý**:
  - `ExportController` / `RouteController` nhận request export.
  - Phân giải `ExportHandler` tương ứng qua `ExportHandlerResolver` dựa trên mã `ExportCode.ROUTING_TRACE`.
  - Đọc trực tiếp dữ liệu snapshot lịch sử từ `RouteCalculationTraceEntity` mà không cần tính toán lại ma trận hay timeline.
  - Gọi `ExcelTemplateEngine` nạp template tại `export-templates/routing-trace-export-template.xlsx`.
  - Thay thế các placeholder đơn (như `${trace.id}`, `${routePlan.code}`) và lặp các dòng bảng động (như `${timeline.stops[].plannedArrivalTime}`).
  - Tạo file download với tên dạng `routing-trace-route-{routePlanId}-{traceId}.xlsx`.
- **Định dạng dữ liệu ma trận**:
  - If trace lưu ma trận đầy đủ $N \times N$, file Excel sẽ hiển thị dưới dạng lưới hai chiều.
  - Nếu trace chỉ lưu danh sách các chặng leg segment (như luồng định tuyến hiện tại), engine sẽ tự động chuyển đổi cấu trúc lưới ma trận thành bảng chặng tuyến nối tiếp (`Từ` $\rightarrow$ `Đến` $\rightarrow$ `Giá trị`) để bảo đảm hiển thị chính xác và rõ ràng.

---

## 8. Các file và class liên quan

| File/Class | Loại | Trách nhiệm | Method quan trọng |
|---|---|---|---|
| `ExportCode` | Constants Class | Định nghĩa mã hằng số của các định dạng export. | N/A |
| `ExportRequest` | DTO Class | Lưu thông số request export (code, routePlanId, traceId). | N/A |
| `ExportResult` | DTO Class | Lưu tên file, content type và nội dung nhị phân byte array. | N/A |
| `ExportHandler` | Interface | Interface xử lý nghiệp vụ export cụ thể. | `export(...)` |
| `ExportHandlerResolver` | Component | Quản lý và phân giải các handler theo mã exportCode. | `resolve(...)` |
| `IExportService` / `ExportServiceImpl` | Application Service | Dịch vụ điều phối luồng export dùng chung. | `export(...)` |
| `ExcelTemplateEngine` | Domain Component | Nạp template Excel, thay thế placeholder và render matrix. | `render(...)` |
| `RoutingTraceExportHandler` | Domain Exporter | Nạp trace từ DB và chuẩn bị data map điền vào template Excel. | `export(...)` |
|---|---|---|---|
| `SchoolBusAppConfigEntity` | JPA Entity | Lưu cấu hình global của module School Bus. | N/A |
| `AppConfigCode` | Constants class | Định nghĩa các key cấu hình dùng chung. | N/A |
| `RoutingConfigResolverImpl` | Domain service | Gom và resolve các tham số routing với default an toàn. | `resolve()` |
| `SchoolPickupPointWindowRepository` | Repository | Truy vấn time window theo schedule và direction. | `findWindow(...)` |
| `SchoolPickupPointWindowServiceImpl` | Application service | Cung cấp logic lookup time window. | `findWindow(...)` |
| `IRoutingMatrixService` | Interface | Định nghĩa contract sinh ma trận `N x N`. | `generateMatrix(...)` |
| `RoutingMatrixServiceImpl` | Domain service | Tính ma trận distance/duration bằng OSRM hoặc fallback. | `generateMatrix(...)` |
| `TimelineCalculatorServiceImpl` | Domain service | Tính timeline và sinh feasibility issues. | `calculateTimeline(...)`, `validateAndGenerateIssues(...)` |
| `RouteController` | REST Controller | Cung cấp các REST API thao tác định tuyến và trace. | `getRoutingMatrix(...)`, `getLatestCalculationTrace(...)`, `getCalculationTraceHistory(...)` |
| `RouteCalculationTraceEntity` | JPA Entity | Thực thể lưu vết tính toán định tuyến. | N/A |
| `RouteCalculationTraceRepository` | Repository | Cung cấp truy vấn lấy vết tính toán định tuyến. | `findFirstByRoutePlanIdAndCalculationType...` |
| `RouteCalculationTraceServiceImpl` | Application service | Quản lý lưu vết mới và lấy vết tính toán cũ. | `saveTrace(...)`, `findLatestByRoutePlanId(...)` |
| `V23__refactor_school_bus_app_config.sql` | Flyway migration | Bỏ `tenant_id`, tạo unique index theo `config_code`. | N/A |
| `V24__create_route_calculation_trace.sql` | Flyway migration | Tạo bảng trace, các khóa ngoại và chỉ mục bán phần. | N/A |

---

## 9. TODO cho các phase tiếp theo

- **Phase 7**: Simulation Benchmark Runner. Chạy thử nghiệm và so sánh song song nhiều giải pháp điều phối để đánh giá thời gian tính toán, trạng thái khả thi và điểm số hàm mục tiêu.

---

## 10. Phase 5 — Thuật toán sinh tuyến tham lam (Greedy Route Generation)

Bộ máy tự động sinh tuyến xe (`GreedyRouteGenerationServiceImpl.java`) xây dựng tập hợp các tuyến khả thi ban đầu cho phiên lập lịch.

### Quy trình tìm kiếm heuristics

1. **Thông số đầu vào**:
   - `planningSessionId` (xác định trường học, lịch trình học, ngày thực hiện, chiều đi/về).
   - `defaultBusCapacity` (sức chứa mặc định của xe, mặc định là 30).
   - `depotId` (ID của Depot xuất phát/kết thúc).
2. **Gom cụm học sinh (Student Aggregation)**:
   - Truy vấn danh sách học sinh đủ điều kiện dựa trên lịch học, ngày và chiều định tuyến.
   - Nhóm học sinh theo cùng một điểm đón/trả (`PointAggregate`).
   - Sắp xếp các cụm điểm này theo thứ tự giảm dần về số lượng học sinh.
3. **Chèn khả thi gần nhất (Nearest Feasible Insertion)**:
   - Với mỗi cụm điểm, thuật toán thử chèn vào tất cả các vị trí stop trống của các tuyến đang hoạt động.
   - Mỗi lần thử nghiệm, hệ thống gọi **Phase 3/4 (TimelineCalculatorService)** để tạm thời tạo stop, gán học sinh, tính lại thời gian di chuyển, khoảng cách, timeline và phát hiện lỗi an toàn.
   - **Ràng buộc loại bỏ (Rejection)**: Một vị trí chèn bị từ chối nếu nó vi phạm các lỗi nghiêm trọng `BLOCKING` (ví dụ: muộn time window đón trả, muộn giờ học) hoặc tổng số học sinh trên xe vượt quá sức chứa (`capacity`).
   - **Lựa chọn chi phí**: Hệ thống chọn vị trí chèn tối ưu hóa hàm tăng chi phí cục bộ:
     $$\Delta \text{Cost} = w_{\text{distance}} \times \Delta \text{Distance} + w_{\text{duration}} \times \Delta \text{Duration} + w_{\text{waitTime}} \times \Delta \text{WaitTime}$$
   - Nếu không có tuyến hiện tại nào chèn khả thi, hệ thống sẽ mở một tuyến mới. Nếu mở tuyến mới vẫn không thể chèn được (do vi phạm các ràng buộc cứng), cụm học sinh sẽ bị đẩy vào danh sách **Chưa được gán (Unassigned)**.
4. **Ghi nhận kết quả**:
   - Lưu trữ các tuyến xe, điểm dừng và học sinh chính thức vào Database.
   - Ghi lại vết chạy thuật toán vào `RouteCalculationTraceEntity` dưới dạng `GREEDY_GENERATION`.
   - *Thuật toán Greedy được thiết kế để chạy nhanh và dễ giải thích, nhưng không đảm bảo tìm được tối ưu toàn cục.*

---

## 11. Phase 6 — Đánh giá chất lượng tuyến bằng Hàm mục tiêu (Objective Function)

Bộ máy chấm điểm hàm mục tiêu (`RouteObjectiveScoringServiceImpl.java`) đánh giá chất lượng của một tuyến xe riêng lẻ hoặc của toàn bộ giải pháp điều phối.

### Công thức tính toán

#### A. Giá trị hàm mục tiêu giải pháp ($Z_{\text{session}}$)
Tổng giá trị hàm mục tiêu là tổng của các chi phí vận hành thực tế và hình phạt vi phạm ràng buộc. **Giá trị hàm mục tiêu càng nhỏ thì chất lượng giải pháp càng tốt.**

$$Z_{\text{session}} = \sum_{r \in R} \left( C_{\text{distance}}(r) + C_{\text{duration}}(r) + C_{\text{waitTime}}(r) + C_{\text{blocking}}(r) + C_{\text{warning}}(r) + C_{\text{excess}}(r) \right) + C_{\text{route\_count}} + C_{\text{unassigned}} + C_{\text{imbalance}}$$

Trong đó:
- $C_{\text{distance}}(r) = w_{\text{distance}} \times \text{distanceKm}(r)$ (Chi phí khoảng cách)
- $C_{\text{duration}}(r) = w_{\text{duration}} \times \text{durationMin}(r)$ (Chi phí thời gian)
- $C_{\text{waitTime}}(r) = w_{\text{waitTime}} \times \text{totalStudentWaitTimeMin}(r)$ (Thời gian học sinh phải chờ trên xe)
- $C_{\text{blocking}}(r) = w_{\text{blocking}} \times \text{blockingIssueCount}(r)$ (Hình phạt lỗi vi phạm nghiêm trọng)
- $C_{\text{warning}}(r) = w_{\text{warning}} \times \text{warningIssueCount}(r)$ (Hình phạt cảnh báo)
- $C_{\text{excess}}(r) = w_{\text{capacity\_excess}} \times \max(0, \text{studentCount}(r) - \text{busCapacity}(r))$ (Hình phạt chở quá tải)
- $C_{\text{route\_count}} = w_{\text{route\_count}} \times |R|$ (Chi phí sử dụng số lượng xe)
- $C_{\text{unassigned}} = w_{\text{unassigned}} \times \text{totalUnassignedStudents}$ (Hình phạt bỏ sót học sinh)
- $C_{\text{imbalance}} = w_{\text{load\_balance}} \times \left( \max_{r \in R} \text{studentCount}(r) - \min_{r \in R} \text{studentCount}(r) \right)$ (Hình phạt mất cân bằng tải giữa các xe)

#### B. Điểm số Chuẩn hóa hiển thị (Normalized Display Score)
Để người dùng dễ đánh giá, giá trị hàm mục tiêu $Z$ được quy đổi về thang điểm $[0, 100]$ hiển thị trên UI:

$$\text{Display Score} = \frac{100}{1 + \frac{Z}{500}}$$

- Khi giá trị hàm mục tiêu $Z = 0$, điểm số hiển thị đạt tối đa là $100.00$.
- Khi hàm mục tiêu tăng (do tăng chi phí vận hành hoặc có vi phạm lỗi), điểm số sẽ tiệm cận về $0$.
- **Ý nghĩa**: Điểm số này mang tính chất so sánh tương đối giữa các phương án điều phối dưới các hệ số trọng số hiện tại, không phải là sự tối ưu tuyệt đối về mặt toán học.

