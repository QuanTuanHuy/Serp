# Thiết kế Lập kế hoạch Tuyến xe buýt trường học (Sau V31)

> Cập nhật sau Phase 2 backend cleanup. Toàn bộ logic greedy/OSRM/timeline/window/trace/issue/score đã được gỡ bỏ.

---

## 1. Tổng quan

Module lập kế hoạch tuyến theo hướng **student-centric, manual-first**:

1. Admin tạo phiên lập kế hoạch (trường + lịch học + chiều đi/về + ngày phục vụ)
2. Hệ thống hiển thị danh sách học sinh đủ điều kiện
3. Admin gán thủ công học sinh vào tuyến và điểm dừng
4. Admin publish phiên → tuyến sẵn sàng để dispatch
5. Dispatch gán xe + tài xế + phụ xe cho mỗi tuyến

**Đã loại bỏ trong V31:**
- Thuật toán tham lam tự động tạo tuyến
- Tích hợp OSRM routing engine
- Tính ma trận khoảng cách N×N
- Validate time window (cửa sổ đón/trả)
- Lưu trace tính toán (route_calculation_trace)
- Planning issue / blocking issue / quality score
- Tính điểm mục tiêu (objective scoring) và trọng số tối ưu
- Timeline calculator (planned arrival/departure mỗi điểm dừng)

---

## 2. Logic xác định học sinh đủ điều kiện (Eligibility)

Một subscription đủ điều kiện khi:

| Tiêu chí | Quy tắc |
|-----------|---------|
| Tenant | đúng tenant của phiên |
| Trường | đúng trường của phiên |
| Trạng thái | subscription ACTIVE |
| Khoảng ngày | `effective_from <= ngày_phục_vụ` VÀ (`effective_to IS NULL` HOẶC `effective_to >= ngày_phục_vụ`) |
| Ngày trong tuần | cờ ngày tương ứng = true |
| Chiều đi | OUTBOUND → trip_option IN (MORNING, ROUND_TRIP); RETURN → trip_option IN (AFTERNOON, ROUND_TRIP) |
| Điểm đón/trả | OUTBOUND cần có pickup_point; RETURN cần có dropoff_point |
| Học sinh | active và chưa xóa |

Không validate time window.

---

## 3. Cấu trúc tuyến

Mỗi tuyến (`school_bus_route_plan`) gồm:
- Danh sách điểm dừng có thứ tự (`school_bus_route_stop`) – mỗi điểm liên kết với `pickup_point`
- Phân công học sinh (`school_bus_route_plan_student`) – **1 row duy nhất mỗi học sinh mỗi tuyến**:
  - `pickup_stop_id` → điểm dừng học sinh lên xe
  - `dropoff_stop_id` → điểm dừng học sinh xuống xe

Loại điểm dừng:
- `START_TERMINAL` – điểm xuất phát (depot hoặc trường tùy chiều)
- `END_TERMINAL` – điểm kết thúc
- Điểm dừng giữa – các điểm đón/trả

---

## 4. Khoảng cách & Geometry (Haversine đơn giản)

Khi thêm, xóa hoặc sắp xếp lại điểm dừng:
1. Tính khoảng cách Haversine giữa các điểm dừng liên tiếp (theo thứ tự)
2. Tổng cộng → `planned_distance_km` trên tuyến
3. Tạo mảng tọa độ → `geometry_path` (dạng `[[lng,lat],...]`)

**Không tính:**
- Khoảng cách theo mạng lưới đường (không OSRM)
- Thời gian đến/đi dự kiến mỗi điểm
- Ước tính thời gian di chuyển

Frontend dùng `geometry_path` để vẽ polyline trên bản đồ. Khoảng cách là xấp xỉ đường thẳng.

---

## 5. Validate khi Publish

Trước khi publish một phiên:

| # | Quy tắc |
|---|---------|
| 1 | Phiên phải ở trạng thái DRAFT |
| 2 | Phiên phải có ít nhất 1 tuyến |
| 3 | Mỗi tuyến phải có ít nhất 1 học sinh được gán |

Không validate blocking issue. Không check capacity lúc publish.

---

## 6. Sức chứa (Capacity)

- Mỗi tuyến có field `capacity` (set lúc tạo, thường từ sức chứa xe)
- `studentCount` được cập nhật khi gán/gỡ học sinh
- Frontend nên hiển thị cảnh báo khi `studentCount > capacity`
- Backend **không chặn** gán khi vượt capacity (chỉ advisory trong phase này)

---

## 7. Tương lai (Phase 3+)

Các tính năng sẽ xây lại sau:
- Thuật toán tham lam tự động (thiết kế mới)
- Tích hợp routing engine thật (OSRM hoặc Google Maps)
- Ước tính timeline (ETA mỗi điểm dừng)
- Audit logging (thiết kế mới)
- Quản lý tạm dừng dịch vụ (thiết kế mới)
