# Tài liệu Đặc tả Model: Toàn bộ Hệ thống

## Mục lục
- [Tổng quan Hệ thống](#tổng-quan-hệ-thống)
- [I. Equipment Models (Thiết bị)](#i-equipment-models-thiết-bị)
  - [1. Container](#1-container)
  - [2. Mooc (Rơ-moóc)](#2-mooc-rơ-moóc)
  - [3. MoocGroup](#3-moocgroup)
  - [4. MoocPacking](#4-moocpacking)
  - [5. Truck (Xe đầu kéo)](#5-truck-xe-đầu-kéo)
  - [Sơ đồ Quan hệ Equipment](#sơ-đồ-quan-hệ-equipment)
- [II. Place Models (Địa điểm)](#ii-place-models-địa-điểm)
  - [1. DepotContainer](#1-depotcontainer)
  - [2. DepotMooc](#2-depotmooc)
  - [3. DepotTruck](#3-depottruck)
  - [4. Port (Cảng)](#4-port-cảng)
  - [5. ShipCompany (Hãng tàu)](#5-shipcompany-hãng-tàu)
  - [6. Warehouse (Kho hàng)](#6-warehouse-kho-hàng)
  - [Bảng So sánh Place Models](#bảng-so-sánh-place-models)
- [III. Tích hợp và Routing](#iii-tích-hợp-và-routing)
- [IV. Ví dụ Thực tế](#iv-ví-dụ-thực-tế)
- [V. Bảng Thuật ngữ](#v-bảng-thuật-ngữ)
- [VI. Request Models (Yêu cầu vận chuyển)](#vi-request-models-yêu-cầu-vận-chuyển)
- [VII. Input Models (Dữ liệu đầu vào)](#vii-input-models-dữ-liệu-đầu-vào)
- [VIII. Routing Models (Mô hình tuyến)](#viii-routing-models-mô-hình-tuyến)
- [IX. Output Models (Đầu ra giải pháp)](#ix-output-models-đầu-ra-giải-pháp)
- [X. Bảng Bao phủ Model](#x-bảng-bao-phủ-model)

---

## Tổng quan Hệ thống

Hệ thống vận tải container-truck-mooc là một giải pháp logistics phức tạp để quản lý và tối ưu hóa việc vận chuyển container bằng xe đầu kéo (truck) kéo rơ-moóc (mooc). Hệ thống bao gồm:

- **Equipment (Thiết bị)**: Các phương tiện và thiết bị vận tải (Container, Mooc, Truck)
- **Places (Địa điểm)**: Các điểm trong mạng lưới logistics (Depot, Port, Warehouse, ShipCompany)
- **Requests (Yêu cầu)**: Các yêu cầu xuất/nhập container và vận chuyển giữa các warehouse
- **Routing (Định tuyến)**: Lập kế hoạch tuyến đường tối ưu cho từng truck

### Kiến trúc Tổng thể

```
┌─────────────┐
│    Truck    │ (Xe đầu kéo)
│  - Driver   │
│  - Working  │
│    Times    │
└──────┬──────┘
       │ kéo (tows)
       ▼
┌─────────────┐
│    Mooc     │ (Rơ-moóc/Trailer)
│  - Category │
│  - Weight   │
└──────┬──────┘
       │ chở (carries)
       ▼
┌─────────────┐
│  Container  │
│  - Weight   │
│  - Category │
└─────────────┘

Luồng vận chuyển:
DepotTruck → DepotMooc → DepotContainer → Warehouse → Port → Warehouse → DepotContainer → DepotMooc → DepotTruck
```

---

## I. Equipment Models (Thiết bị)

### 1. Container

**File**: `models/equipments/Container.java`

#### Mục đích và Vai trò
Container là đơn vị hàng hóa cơ bản trong hệ thống logistics. Mỗi container chứa hàng hóa cần được vận chuyển từ điểm này đến điểm khác, được chở bởi rơ-moóc và thuộc sở hữu của một hãng tàu.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `code` | String | Mã định danh duy nhất của container (VD: CONT-001) |
| `weight` | double | Trọng lượng/khối lượng của container (kg) |
| `categoryCode` | String | Loại container (VD: "20ft", "40ft", "45ft") |
| `depotContainerCode` | String | Mã depot nơi container xuất phát |
| `returnDepotCodes` | String[] | Mảng các mã depot có thể trả container về |
| `importedContainer` | boolean | Cờ đánh dấu container nhập khẩu (true) hay nội địa (false) |
| `shipCompanyCode` | String | Mã hãng tàu sở hữu/quản lý container |

#### Constructors

```java
// Constructor đầy đủ (bao gồm cờ importedContainer)
public Container(String code, double weight, String categoryCode, 
                 String depotContainerCode, String[] returnDepotCodes,
                 boolean importedContainer, String shipCompanyCode)

// Constructor rút gọn (importedContainer mặc định = false)
public Container(String code, double weight, String categoryCode,
                 String depotContainerCode, String[] returnDepotCodes,
                 String shipCompanyCode)

// Constructor mặc định
public Container()
```

#### Phương thức Chính

- `getCode()`, `setCode(String)` - Quản lý mã container
- `getWeight()`, `setWeight(double)` - Quản lý trọng lượng
- `getCategoryCode()`, `setCategoryCode(String)` - Quản lý loại container
- `getDepotContainerCode()`, `setDepotContainerCode(String)` - Quản lý depot xuất phát
- `getReturnDepotCodes()`, `setReturnDepotCodes(String[])` - Quản lý các depot trả về
- `isImportedContainer()`, `setImportedContainer(boolean)` - Kiểm tra/thiết lập trạng thái nhập khẩu
- `getShipCompanyCode()`, `setShipCompanyCode(String)` - Quản lý mã hãng tàu

#### Logic Nghiệp vụ và Ràng buộc

- **Phân loại**: Container được phân loại theo kích thước (20ft, 40ft, 45ft)
- **Trọng lượng**: Trọng lượng container phải được kiểm tra với sức chứa của mooc (xem `ContainerCapacityConstraint`)
- **Ràng buộc vận chuyển**: Container phải được chở bởi mooc phù hợp (xem `ContainerCarriedByTrailerConstraint`)
- **Điểm trả về linh hoạt**: Mảng `returnDepotCodes` cho phép container được trả về nhiều depot khác nhau, tăng tính linh hoạt cho routing
- **Thuộc tính nhập khẩu**: Container nhập khẩu có thể có quy trình xử lý và quy định riêng
- **Quyền sở hữu**: Mỗi container thuộc một hãng tàu cụ thể, ảnh hưởng đến depot được phép trả về

#### Quan hệ với các Class khác

- **Được chở bởi**: `Mooc` - một mooc có thể chở một hoặc nhiều container
- **Xuất phát từ**: `DepotContainer` - container phải được lấy từ depot
- **Thuộc về**: `ShipCompany` - container được sở hữu bởi một hãng tàu
- **Được tham chiếu trong**: 
  - `ExportContainerRequest`, `ImportContainerRequest` - các yêu cầu vận chuyển
  - `ContainerTruckMoocInput` - input tổng thể của hệ thống

---

### 2. Mooc (Rơ-moóc)

**File**: `models/equipments/Mooc.java`

#### Mục đích và Vai trò
Mooc (rơ-moóc/trailer) là thiết bị trung gian giữa xe đầu kéo và container. Mooc được xe truck kéo và chở container trên đó. Mỗi mooc có khả năng chở container theo kích thước và trọng lượng nhất định.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `id` | int | ID số của mooc |
| `code` | String | Mã định danh/biển số mooc (VD: MOOC-001) |
| `category` | String | Loại mooc theo kích thước ("20", "40", "45") |
| `categoryId` | int | ID số tương ứng với loại mooc |
| `weight` | double | Tải trọng tối đa của mooc (kg) |
| `status` | String | Trạng thái hoạt động (available, in-use, maintenance) |
| `statusId` | int | ID số của trạng thái |
| `depotMoocCode` | String | Mã depot mooc được gán |
| `depotMoocLocationCode` | String | Mã vị trí địa lý của depot |
| `returnDepotCodes` | String[] | Các depot có thể trả mooc rỗng về |
| `intervals` | Intervals[] | Các khung thời gian sẵn sàng hoạt động |

#### Constructors

```java
// Constructor đầy đủ (bao gồm intervals)
public Mooc(int id, String code, String category, int categoryId,
            double weight, String status, int statusId,
            String depotMoocCode, String depotMoocLocationCode,
            String[] returnDepotCodes, Intervals[] intervals)

// Constructor mặc định
public Mooc()
```

#### Phương thức Chính

- `getId()`, `setId(int)` - Quản lý ID mooc
- `getCode()`, `setCode(String)` - Quản lý mã mooc
- `getCategory()`, `setCategory(String)` - Quản lý loại mooc
- `getCategoryId()`, `setCategoryId(int)` - Quản lý ID loại
- `getWeight()`, `setWeight(double)` - Quản lý tải trọng
- `getStatus()`, `setStatus(String)` - Quản lý trạng thái
- `getDepotMoocCode()`, `setDepotMoocCode(String)` - Quản lý depot
- `getIntervals()`, `setIntervals(Intervals[])` - Quản lý khung thời gian

#### Logic Nghiệp vụ và Ràng buộc

- **Tương thích kích thước**: Mooc loại 40ft có thể chở container 40ft hoặc hai container 20ft
- **Ràng buộc trọng lượng**: Tổng trọng lượng các container trên mooc không được vượt quá `weight` (xem `MoocCapacityConstraint`)
- **Quản lý đội xe**: Trường `status` cho phép theo dõi tình trạng sử dụng mooc trong thời gian thực
- **Lập lịch**: Mảng `intervals` định nghĩa các khoảng thời gian mooc có thể được sử dụng
- **Linh hoạt trả về**: Nhiều `returnDepotCodes` cho phép tối ưu hóa điểm trả mooc rỗng

#### Quan hệ với các Class khác

- **Được kéo bởi**: `Truck` - xe đầu kéo kéo mooc
- **Chở**: `Container` - mooc chở một hoặc nhiều container
- **Thuộc về**: `DepotMooc` - mỗi mooc có depot gốc
- **Nhóm vào**: `MoocGroup` - các mooc được phân loại theo nhóm
- **Cấu hình chở**: `MoocPacking` - định nghĩa cấu hình container có thể chở

---

### 3. MoocGroup

**File**: `models/equipments/MoocGroup.java`

#### Mục đích và Vai trò
MoocGroup đại diện cho một nhóm logic các mooc có khả năng chở container tương tự nhau. Thay vì định nghĩa chi tiết cấu hình container cho từng mooc, hệ thống sử dụng MoocGroup để nhóm các mooc theo năng lực.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `id` | int | ID định danh duy nhất của nhóm |
| `code` | String | Mã/tên của nhóm mooc (VD: "GROUP-40FT") |
| `packing` | MoocPacking[] | Mảng các cấu hình đóng gói container |

#### Constructors

```java
// Constructor đầy đủ
public MoocGroup(int id, String code, MoocPacking[] packing)

// Constructor mặc định
public MoocGroup()
```

#### Phương thức Chính

- `getId()`, `setId(int)` - Quản lý ID nhóm
- `getCode()`, `setCode(String)` - Quản lý mã nhóm
- `getPacking()`, `setPacking(MoocPacking[])` - Quản lý cấu hình packing

#### Logic Nghiệp vụ

- **Phân loại năng lực**: Các mooc có cùng khả năng chở được gộp vào một nhóm
- **Đơn giản hóa gán container**: Thay vì kiểm tra từng mooc, hệ thống chỉ cần kiểm tra MoocGroup
- **Cấu hình linh hoạt**: Mảng `packing` cho phép định nghĩa nhiều cấu hình khác nhau

#### Ví dụ Cấu hình

```
MoocGroup: "40FT-STANDARD"
├─ MoocPacking: contTypeCode="20ft", contTypeQuantity=2
└─ MoocPacking: contTypeCode="40ft", contTypeQuantity=1

=> Nhóm này có thể chở: 2 container 20ft HOẶC 1 container 40ft
```

#### Quan hệ với các Class khác

- **Tổng hợp**: `MoocPacking` - một nhóm chứa nhiều cấu hình packing
- **Phân loại**: `Mooc` - các mooc thuộc về các nhóm khác nhau
- **Sử dụng trong**: Cấu hình đội xe và lập kế hoạch năng lực

---

### 4. MoocPacking

**File**: `models/equipments/MoocPacking.java`

#### Mục đích và Vai trò
MoocPacking định nghĩa chi tiết khả năng chở container của một MoocGroup. Mỗi MoocPacking chỉ định một loại container cụ thể và số lượng container loại đó có thể chở.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `contTypeCode` | String | Mã loại container (VD: "20ft", "40ft") |
| `contTypeQuantity` | int | Số lượng container loại này có thể chở |

#### Constructors

```java
// Constructor đầy đủ
public MoocPacking(String contTypeCode, int contTypeQuantity)

// Constructor mặc định
public MoocPacking()
```

#### Phương thức Chính

- `getContTypeCode()`, `setContTypeCode(String)` - Quản lý loại container
- `getContTypeQuantity()`, `setContTypeQuantity(int)` - Quản lý số lượng

#### Logic Nghiệp vụ

- **Cấu hình chính xác**: Định nghĩa chính xác loại và số lượng container có thể chở
- **Validation**: Hệ thống sử dụng MoocPacking để validate việc gán container cho mooc
- **Tối ưu hóa**: Giúp thuật toán routing chọn mooc phù hợp với yêu cầu

#### Ví dụ Sử dụng

```java
// Mooc 40ft có thể chở:
MoocPacking option1 = new MoocPacking("20ft", 2);  // 2 container 20ft
MoocPacking option2 = new MoocPacking("40ft", 1);  // HOẶC 1 container 40ft

// Mooc 20ft chỉ chở:
MoocPacking option = new MoocPacking("20ft", 1);   // 1 container 20ft
```

#### Quan hệ với các Class khác

- **Thuộc về**: `MoocGroup` - là phần tử trong mảng packing
- **Tham chiếu**: `Container.categoryCode` - liên kết với loại container

---

### 5. Truck (Xe đầu kéo)

**File**: `models/equipments/Truck.java`

#### Mục đích và Vai trò
Truck (xe đầu kéo) là phương tiện chính trong hệ thống, có nhiệm vụ kéo mooc và vận chuyển container đến các điểm đích. Mỗi truck có tài xế, giờ làm việc, và depot gốc.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `id` | int | ID số của truck |
| `code` | String | Biển số xe/mã truck (VD: TRUCK-001) |
| `weight` | double | Trọng tải xe/thông số khối lượng |
| `driverID` | int | ID của tài xế được gán |
| `driverCode` | String | Mã số/badge tài xế |
| `driverName` | String | Họ tên đầy đủ của tài xế |
| `depotTruckCode` | String | Mã depot truck được gán |
| `depotTruckLocationCode` | String | Mã vị trí địa lý của depot |
| `startWorkingTime` | String | Thời gian bắt đầu ca làm việc (HH:MM) |
| `endWorkingTime` | String | Thời gian kết thúc ca làm việc (HH:MM) |
| `status` | String | Trạng thái hoạt động (available, in-use, maintenance) |
| `returnDepotCodes` | String[] | Các depot có thể trả truck về cuối ca |
| `intervals` | Intervals[] | Các khung thời gian sẵn sàng |

#### Constructors

```java
// Constructor đầy đủ
public Truck(int id, String code, double weight, int driverID,
             String driverCode, String driverName, String depotTruckCode,
             String depotTruckLocationCode, String startWorkingTime,
             String endWorkingTime, String status,
             String[] returnDepotCodes, Intervals[] intervals)

// Constructor mặc định
public Truck()
```

#### Phương thức Chính

- `getId()`, `setId(int)` - Quản lý ID truck
- `getCode()`, `setCode(String)` - Quản lý biển số
- `getWeight()`, `setWeight(double)` - Quản lý trọng tải
- `getDriverID()`, `setDriverID(int)` - Quản lý ID tài xế
- `getDriverCode()`, `setDriverCode(String)` - Quản lý mã tài xế
- `getDriverName()`, `setDriverName(String)` - Quản lý tên tài xế
- `getDepotTruckCode()`, `setDepotTruckCode(String)` - Quản lý depot
- `getStartWorkingTime()`, `setStartWorkingTime(String)` - Quản lý giờ bắt đầu
- `getEndWorkingTime()`, `setEndWorkingTime(String)` - Quản lý giờ kết thúc
- `getStatus()`, `setStatus(String)` - Quản lý trạng thái
- `getIntervals()`, `setIntervals(Intervals[])` - Quản lý khung thời gian

#### Logic Nghiệp vụ và Ràng buộc

- **Gán tài xế**: Mỗi truck gắn liền với một tài xế cụ thể, quan trọng cho việc điều phối và tuân thủ quy định
- **Giới hạn thời gian làm việc**: `startWorkingTime` và `endWorkingTime` đảm bảo tuân thủ quy định lao động và ca làm việc
- **Khung thời gian**: Mảng `intervals` cho phép định nghĩa các khoảng thời gian cụ thể truck có thể hoạt động
- **Depot gốc**: Truck phải xuất phát và trả về depot của mình (có thể khác nhau nếu có nhiều `returnDepotCodes`)
- **Quản lý đội xe**: Trường `status` hỗ trợ theo dõi tình trạng truck

#### Quan hệ với các Class khác

- **Kéo**: `Mooc` - truck kéo một hoặc nhiều mooc trong tuyến đường
- **Xuất phát từ**: `DepotTruck` - truck có depot gốc
- **Điều khiển bởi**: Driver (thông tin lưu trong truck) - mối quan hệ tài xế-xe
- **Tạo ra**: `TruckRoute` - mỗi truck có một tuyến đường trong giải pháp routing

---

### Sơ đồ Quan hệ Equipment

```
┌──────────────────────────────────────┐
│          Truck (Xe đầu kéo)          │
│  ┌────────────────────────────────┐  │
│  │ • driverID, driverCode, name   │  │
│  │ • startWorkingTime/endTime     │  │
│  │ • depotTruckCode               │  │
│  │ • intervals (khung thời gian)  │  │
│  │ • status (trạng thái)          │  │
│  └────────────────────────────────┘  │
└────────────┬─────────────────────────┘
             │ 
             │ kéo (tows)
             │ 1:N
             ▼
┌──────────────────────────────────────┐
│         Mooc (Rơ-moóc)               │
│  ┌────────────────────────────────┐  │
│  │ • category (20, 40, 45)        │  │
│  │ • weight (tải trọng)           │  │
│  │ • depotMoocCode                │  │
│  │ • intervals                    │  │
│  │ • status                       │  │
│  └────────────────────────────────┘  │
└────────────┬─────────────────────────┘
             │
             │ chở (carries)
             │ 1:N
             ▼
┌──────────────────────────────────────┐
│           Container                  │
│  ┌────────────────────────────────┐  │
│  │ • categoryCode (20ft, 40ft)    │  │
│  │ • weight (khối lượng)          │  │
│  │ • shipCompanyCode              │  │
│  │ • depotContainerCode           │  │
│  │ • importedContainer (flag)     │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘

┌──────────────────────────────────────┐
│          MoocGroup                   │
│  ┌────────────────────────────────┐  │
│  │ • code (tên nhóm)              │  │
│  │ • packing[] (cấu hình)         │  │◄─── phân loại
│  └────────────────────────────────┘  │     (theo năng lực)
└────────────┬─────────────────────────┘            │
             │                                       │
             │ chứa (aggregates)                    │
             │ 1:N                                   │
             ▼                                       │
┌──────────────────────────────────────┐            │
│         MoocPacking                  │            │
│  ┌────────────────────────────────┐  │            │
│  │ • contTypeCode (loại)          │  ├────────────┘
│  │ • contTypeQuantity (số lượng)  │  │
│  └────────────────────────────────┘  │
│                                      │
│  Ví dụ: ("20ft", 2) hoặc ("40ft", 1)│
└──────────────────────────────────────┘
```

---

## II. Place Models (Địa điểm)

### 1. DepotContainer

**File**: `models/places/DepotContainer.java`

#### Mục đích và Vai trò
DepotContainer là kho chứa/depot chuyên dụng để lưu trữ và xử lý container. Đây là nơi container rỗng được lấy ra trước khi vận chuyển và nơi container được trả về sau khi hoàn thành công việc.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `code` | String | Mã định danh duy nhất của depot (VD: DC-001) |
| `locationCode` | String | Mã vị trí địa lý/tọa độ |
| `pickupContainerDuration` | int | Thời gian lấy container (phút) |
| `deliveryContainerDuration` | int | Thời gian giao/trả container (phút) |
| `returnedContainer` | boolean | Cờ đánh dấu depot xử lý container trả về |

#### Constructors

```java
// Constructor chính
public DepotContainer(String code, String locationCode,
                      int pickupContainerDuration,
                      int deliveryContainerDuration)

// Constructor mặc định
public DepotContainer()
```

#### Phương thức Chính

- `getCode()`, `setCode(String)` - Quản lý mã depot
- `getLocationCode()`, `setLocationCode(String)` - Quản lý vị trí
- `getPickupContainerDuration()`, `setPickupContainerDuration(int)` - Quản lý thời gian lấy
- `getDeliveryContainerDuration()`, `setDeliveryContainerDuration(int)` - Quản lý thời gian giao
- `getReturnedContainer()`, `setReturnedContainer(boolean)` - Quản lý cờ trả về

#### Logic Nghiệp vụ

- **Điểm xuất phát**: Container rỗng được lấy từ depot trước khi vận chuyển hàng
- **Điểm kết thúc**: Container rỗng được trả về depot sau khi giao hàng
- **Thời gian phục vụ**: Các thời gian `pickup/delivery` được cộng vào tổng thời gian tuyến đường
- **Luồng đa bước**: Hỗ trợ logistics nhiều chặng (lấy rỗng → chở hàng → trả rỗng)

#### Sử dụng trong Routing

**Các hành động routing**:
- `DEPOT_PICKUP_EMPTYCONT` - Lấy container rỗng từ depot
- `DEPOT_DELIVERY_EMPTYCONT` - Trả container rỗng về depot

**Ví dụ luồng**:
```
1. Truck đến DepotContainer
2. Lấy container rỗng (mất pickupContainerDuration phút)
3. Vận chuyển container đến Warehouse/Port
4. Sau khi dỡ hàng, trả container rỗng về DepotContainer (mất deliveryContainerDuration phút)
```

#### Quan hệ với các Class khác

- **Chứa**: `Container` - mỗi container có `depotContainerCode` tham chiếu đến depot
- **Được tham chiếu trong**: `ExportEmptyRequests`, `ImportEmptyRequests` - yêu cầu lấy/trả container
- **Map trong solver**: `TruckContainerSolver.mCode2DepotContainer` - HashMap để tra cứu nhanh

---

### 2. DepotMooc

**File**: `models/places/DepotMooc.java`

#### Mục đích và Vai trò
DepotMooc là depot chuyên dụng để lưu trữ và quản lý mooc (rơ-moóc). Truck phải lấy mooc từ depot này trước khi thực hiện vận chuyển container.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `code` | String | Mã định danh duy nhất của depot mooc |
| `locationCode` | String | Mã vị trí địa lý |
| `pickupMoocDuration` | int | Thời gian lấy mooc (phút) |
| `deliveryMoocDuration` | int | Thời gian trả mooc (phút) |

#### Constructors

```java
// Constructor chính
public DepotMooc(String code, String locationCode,
                 int pickupMoocDuration, int deliveryMoocDuration)

// Constructor mặc định
public DepotMooc()
```

#### Phương thức Chính

- `getCode()`, `setCode(String)` - Quản lý mã depot
- `getLocationCode()`, `setLocationCode(String)` - Quản lý vị trí  
- `getPickupMoocDuration()`, `setPickupMoocDuration(int)` - Quản lý thời gian lấy
- `getDeliveryMoocDuration()`, `setDeliveryMoocDuration(int)` - Quản lý thời gian trả

#### Logic Nghiệp vụ

- **Depot gốc của mooc**: Mỗi mooc có depot gốc nơi nó được lưu trữ
- **Bắt buộc trong tuyến đường**: Truck phải lấy mooc đầu tuyến và trả về cuối tuyến
- **Thời gian xử lý**: Thời gian lấy/trả mooc ảnh hưởng đến tổng thời gian tuyến đường
- **Chuỗi logistics**: Là một khâu trong chuỗi Truck → Mooc → Container

#### Sử dụng trong Routing

**Các hành động routing**:
- `PICKUP_MOOC` - Lấy mooc từ depot
- `DELIVERY_MOOC` - Trả mooc về depot

**Ví dụ tuyến đường hoàn chỉnh**:
```
START_TRUCK (DepotTruck)
    ↓
PICKUP_MOOC (DepotMooc) ← [pickupMoocDuration phút]
    ↓
[Các hoạt động vận chuyển container]
    ↓
DELIVERY_MOOC (DepotMooc) ← [deliveryMoocDuration phút]
    ↓
END_TRUCK (DepotTruck)
```

#### Quan hệ với các Class khác

- **Chứa**: `Mooc` - mỗi mooc có `depotMoocCode` tham chiếu
- **Ràng buộc**: `MoocCapacityConstraint` - kiểm tra năng lực mooc
- **Map trong solver**: `TruckContainerSolver.mCode2DepotMooc`

---

### 3. DepotTruck

**File**: `models/places/DepotTruck.java`

#### Mục đích và Vai trò
DepotTruck là trạm/depot gốc của xe đầu kéo (truck). Đây là điểm xuất phát và điểm kết thúc cho mỗi ca làm việc của truck.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `code` | String | Mã định danh duy nhất của depot truck |
| `locationCode` | String | Mã vị trí địa lý |

#### Constructors

```java
// Constructor chính
public DepotTruck(String code, String locationCode)

// Constructor mặc định
public DepotTruck()
```

#### Phương thức Chính

- `getCode()`, `setCode(String)` - Quản lý mã depot
- `getLocationCode()`, `setLocationCode(String)` - Quản lý vị trí

#### Logic Nghiệp vụ

- **Đơn giản nhất**: Không có thời gian phục vụ (service duration) như các depot khác
- **Điểm bắt đầu**: Truck bắt đầu ca làm việc từ depot gốc
- **Điểm kết thúc**: Truck phải quay về depot gốc (hoặc depot được phép) cuối ca
- **Ràng buộc địa lý**: Xác định vị trí xuất phát cho tính toán khoảng cách

#### Sử dụng trong Routing

**Các hành động routing**:
- `START_TRUCK` - Bắt đầu tuyến đường tại depot
- `END_TRUCK` - Kết thúc tuyến đường tại depot

**Ví dụ cấu trúc tuyến**:
```
9 (START_TRUCK) ← DepotTruck code = 9
    ↓
[Các hoạt động khác...]
    ↓
9 (END_TRUCK) ← Quay về cùng depot
```

#### Quan hệ với các Class khác

- **Depot của**: `Truck` - mỗi truck có `depotTruckCode`
- **Ràng buộc**: Truck phải bắt đầu và kết thúc tại depot được gán
- **Map trong solver**: `TruckContainerSolver.mCode2DepotTruck`
- **Ảnh hưởng**: Tính toán thời gian dựa trên giờ làm việc (`startWorkingTime`, `endWorkingTime`)

---

### 4. Port (Cảng)

**File**: `models/places/Port.java`

#### Mục đích và Vai trò
Port (cảng biển) là đầu mối quan trọng trong chuỗi cung ứng xuất nhập khẩu container. Container xuất khẩu được giao đến cảng để lên tàu, và container nhập khẩu được lấy từ cảng để phân phối.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `code` | String | Mã định danh duy nhất của cảng (VD: PORT-SGN) |
| `locationCode` | String | Mã vị trí địa lý của cảng |

#### Constructors

```java
// Constructor chính
public Port(String code, String locationCode)

// Constructor mặc định
public Port()
```

#### Phương thức Chính

- `getCode()`, `setCode(String)` - Quản lý mã cảng
- `getLocationCode()`, `setLocationCode(String)` - Quản lý vị trí

#### Logic Nghiệp vụ

- **Đầu mối xuất khẩu**: Container đầy được giao đến cảng để xuất khẩu
- **Đầu mối nhập khẩu**: Container đầy được lấy từ cảng sau khi nhập khẩu
- **Không có service duration**: Cảng không tính thời gian phục vụ cụ thể trong model (có thể được xử lý ở layer khác)
- **Node trong mạng**: Cảng là một node quan trọng trong graph routing

#### Sử dụng trong Routing

**Các hành động routing**:
- `PORT_PICKUP_FULLCONT` - Lấy container đầy từ cảng (nhập khẩu)
- `PORT_DELIVERY_FULLCONT` - Giao container đầy đến cảng (xuất khẩu)
- `PORT_DELIVERY_EMPTYCONT` - Giao container rỗng đến cảng
- `PORT_PICKUP_EMPTYCONT` - Lấy container rỗng từ cảng

**Ví dụ**:
```
Xuất khẩu:
Warehouse (PICKUP) → Port 3 (PORT_DELIVERY_FULLCONT)

Nhập khẩu:
Port 2 (PORT_PICKUP_FULLCONT) → Warehouse (DELIVERY)
```

#### Quan hệ với các Class khác

- **Điểm đến/xuất phát**: `ImportContainerRequest`, `ExportContainerRequest`
- **Trong requests**: `ExportLadenRequests`, `ImportLadenRequests`
- **Map trong solver**: `TruckContainerSolver.mCode2Port`
- **Network node**: Là một loại `Point` trong VRP graph

---

### 5. ShipCompany (Hãng tàu)

**File**: `models/places/ShipCompany.java`

#### Mục đích và Vai trò
ShipCompany đại diện cho công ty vận tải biển sở hữu/quản lý container. Mỗi hãng tàu có mối quan hệ với một số depot container cụ thể, quyết định nơi container của hãng có thể được lấy ra và trả về.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `code` | String | Mã định danh duy nhất của hãng tàu (VD: MAERSK, MSC) |
| `containerDepotCodes` | String[] | Mảng mã các depot container liên kết với hãng |

#### Constructors

```java
// Constructor chính
public ShipCompany(String code, String[] containerDepotCodes)

// Constructor mặc định
public ShipCompany()
```

#### Phương thức Chính

- `getCode()`, `setCode(String)` - Quản lý mã hãng tàu
- `getContainerDepotCodes()`, `setContainerDepotCodes(String[])` - Quản lý danh sách depot

#### Logic Nghiệp vụ

- **Quyền sở hữu container**: Mỗi container thuộc về một hãng tàu cụ thể
- **Ràng buộc depot**: Container chỉ có thể được trả về depot thuộc mạng lưới của hãng tàu sở hữu
- **Quản trị chuỗi cung ứng**: Hỗ trợ logistics đa công ty, mỗi hãng có hệ thống depot riêng
- **Validation**: Hệ thống kiểm tra container có được trả đúng depot của hãng không

#### Sử dụng trong Routing

- **Không phải node**: ShipCompany không xuất hiện trực tiếp trong tuyến đường
- **Vai trò validation**: Kiểm tra tính hợp lệ của các yêu cầu container
- **Xác định depot hợp lệ**: Giới hạn các depot có thể sử dụng cho `returnDepotCodes` của container

**Ví dụ**:
```java
ShipCompany maersk = new ShipCompany("MAERSK", 
    new String[]{"DC-001", "DC-003", "DC-007"});

Container cont = new Container("CONT-123", ...);
cont.setShipCompanyCode("MAERSK");
// Container này chỉ có thể trả về DC-001, DC-003, hoặc DC-007
```

#### Quan hệ với các Class khác

- **Sở hữu**: `Container` - container có field `shipCompanyCode`
- **Liên kết với**: `DepotContainer` - mảng `containerDepotCodes` tham chiếu đến depot
- **Trong requests**: `WarehouseTransportRequest` - chỉ định hãng tàu cho container
- **Validation**: Sử dụng trong preprocessing để kiểm tra tính hợp lệ

---

### 6. Warehouse (Kho hàng)

**File**: `models/places/Warehouse.java`

#### Mục đích và Vai trò
Warehouse (kho hàng/trung tâm phân phối) là hub logistics phức tạp nhất trong hệ thống. Warehouse là nơi bốc dỡ container, có các ràng buộc nghiêm ngặt về thời gian, phương tiện, và tài xế được phép hoạt động.

#### Các Thuộc tính

| Trường | Kiểu dữ liệu | Mô tả |
|--------|--------------|-------|
| `code` | String | Mã định danh duy nhất của warehouse |
| `locationCode` | String | Mã vị trí địa lý |
| `hardConstraintType` | int | Loại ràng buộc cứng (quy tắc dỡ hàng) |
| `vehicleConstraintType` | int | Loại ràng buộc phương tiện (xe nào được phép) |
| `drivers` | int[] | Mảng ID tài xế được phép vào warehouse |
| `vehicles` | int[] | Mảng ID xe được phép vào warehouse |
| `checkin` | Checkin[] | Các khung thời gian check-in (time windows) |
| `breaktimes` | Intervals[] | Các khoảng thời gian nghỉ của tài xế |

#### Constructors

```java
// Constructor đầy đủ
public Warehouse(String code, String locationCode,
                 int hardConstraintType, int vehicleConstraintType,
                 int[] drivers, int[] vehicles,
                 Checkin[] checkin, Intervals[] breaktimes)

// Constructor mặc định
public Warehouse()
```

#### Phương thức Chính

- `getCode()`, `setCode(String)` - Quản lý mã warehouse
- `getLocationCode()`, `setLocationCode(String)` - Quản lý vị trí
- `getHardConstraintType()`, `setHardConstraintType(int)` - Quản lý loại ràng buộc cứng
- `getVehicleConstraintType()`, `setVehicleConstraintType(int)` - Quản lý ràng buộc xe
- `getDrivers()`, `setDrivers(int[])` - Quản lý danh sách tài xế
- `getVehicles()`, `setVehicles(int[])` - Quản lý danh sách xe
- `getCheckin()`, `setCheckin(Checkin[])` - Quản lý khung thời gian
- `getBreaktimes()`, `setBreaktimes(Intervals[])` - Quản lý thời gian nghỉ

#### Logic Nghiệp vụ và Ràng buộc

**1. Ràng buộc cứng (Hard Constraints)**:
- `hardConstraintType` định nghĩa các quy tắc bắt buộc về bốc dỡ hàng
- Ví dụ: quy tắc FIFO, LIFO, hoặc quy tắc custom khác

**2. Ràng buộc phương tiện (Vehicle Constraints)**:
- `vehicleConstraintType` xác định loại kiểm soát phương tiện
- Mảng `vehicles` chứa whitelist các xe được phép
- Nếu mảng rỗng: tất cả xe đều được phép

**3. Ràng buộc tài xế (Driver Constraints)**:
- Mảng `drivers` chứa danh sách ID tài xế được cấp quyền
- Hỗ trợ quản lý an ninh và access control

**4. Ràng buộc thời gian (Time Windows)**:
- Mảng `checkin` định nghĩa earliest và latest arrival time
- Truck phải đến warehouse trong khung thời gian cho phép
- Vi phạm time window = giải pháp không hợp lệ

**5. Thời gian nghỉ (Break Times)**:
- Mảng `breaktimes` định nghĩa các khoảng thời gian tài xế phải nghỉ
- Tuân thủ quy định lao động và an toàn giao thông

#### Sử dụng trong Routing

**Các hành động routing**:
- `WH_PICKUP_EMPTYCONT` - Lấy container rỗng từ warehouse
- `WH_DELIVERY_EMPTYCONT` - Giao container rỗng đến warehouse
- `WH_PICKUP_FULLCONT` - Lấy container đầy từ warehouse
- `WH_DELIVERY_FULLCONT` - Giao container đầy đến warehouse

**Ví dụ trong tuyến đường**:
```
45 (WH_PICKUP_EMPTYCONT) - Lấy container rỗng tại warehouse 45
    ↓
3 (PORT_PICKUP_FULLCONT) - Lấy hàng tại cảng 3
    ↓
52 (WH_DELIVERY_FULLCONT) - Giao hàng tại warehouse 52
```

**Logistics ba chặng**:
```
Chặng 1: Warehouse A → Port (xuất khẩu container đầy)
Chặng 2: Port → Warehouse B (nhập khẩu container đầy)
Chặng 3: Warehouse B → Depot (trả container rỗng)
```

#### Ràng buộc được Áp dụng

1. **Earliest/Latest Arrival Time**: Từ mảng `checkin`
2. **Vehicle/Driver Whitelist**: Kiểm tra từ mảng `vehicles`/`drivers`
3. **Hard Constraint Type**: Quy tắc bốc dỡ đặc biệt
4. **Break Time Compliance**: Tài xế phải nghỉ theo quy định

#### Quan hệ với các Class khác

- **Điểm đến/xuất phát**: Tất cả loại request (export/import/warehouse-to-warehouse)
- **Ràng buộc**: `CEarliestArrivalTimeVR` - kiểm tra time window
- **VRP**: Là node phức tạp nhất trong Vehicle Routing Problem
- **Map trong solver**: `TruckContainerSolver.mCode2Warehouse`
- **Output**: Xuất hiện nhiều nhất trong các tuyến đường

---

### Bảng So sánh Place Models

| Tiêu chí | DepotContainer | DepotMooc | DepotTruck | Port | ShipCompany | Warehouse |
|----------|----------------|-----------|------------|------|-------------|-----------|
| **Loại** | Depot thiết bị | Depot thiết bị | Trạm gốc | Cảng biển | Đơn vị sở hữu | Hub phân phối |
| **Service Duration** | ✓ (pickup/delivery) | ✓ (pickup/delivery) | ✗ | ✗ | ✗ | ✗ |
| **Time Windows** | ✗ | ✗ | ✗ | ✗ | ✗ | ✓ (checkin) |
| **Driver/Vehicle Rules** | ✗ | ✗ | ✗ | ✗ | ✗ | ✓ |
| **Vai trò chính** | Lưu container | Lưu mooc | Trạm truck | Xuất/nhập khẩu | Quản lý tài sản | Xử lý hàng hóa |
| **Route Actions** | PICKUP/DELIVERY_EMPTYCONT | PICKUP/DELIVERY_MOOC | START/END_TRUCK | PORT_PICKUP/DELIVERY | N/A | WH_PICKUP/DELIVERY |
| **Mức độ ràng buộc** | Trung bình | Trung bình | Thấp | Thấp | N/A (validation) | **Cao** |
| **Tần suất trong route** | Phổ biến | Phổ biến | Thấp (đầu/cuối) | Trung bình | Không xuất hiện | **Rất cao** |
| **Ràng buộc phức tạp** | ✗ | ✗ | ✗ | ✗ | Depot liên kết | ✓✓✓ |

**Ghi chú**:
- ✓ = Có tính năng
- ✗ = Không có
- ✓✓✓ = Có nhiều tính năng phức tạp
- N/A = Không áp dụng

---

## III. Tích hợp và Routing

### Luồng Hoạt động Tích hợp

```
┌──────────────────────────────────────────────────────┐
│         CONTAINER LOGISTICS FLOW                     │
└──────────────────────────────────────────────────────┘

1. XE XUẤT PHÁT:
   DepotTruck (START_TRUCK)
        ↓
   Truck chuẩn bị hoạt động tại depot gốc
   (Thời gian: startWorkingTime)

2. LẤY RƠ-MOÓC:
   DepotMooc (PICKUP_MOOC)
        ↓
   Gắn mooc vào truck
   (Mất: pickupMoocDuration phút)

3. LẤY CONTAINER RỖNG:
   DepotContainer (DEPOT_PICKUP_EMPTYCONT)
        ↓
   Chất container rỗng lên mooc
   (Mất: pickupContainerDuration phút)

4. LẤY HÀNG:
   Warehouse (WH_PICKUP_FULLCONT)
   HOẶC
   Port (PORT_PICKUP_FULLCONT)
        ↓
   Chất hàng vào container
   (Phải đến trong time window của warehouse)

5. GIAO HÀNG:
   Warehouse (WH_DELIVERY_FULLCONT)
   HOẶC
   Port (PORT_DELIVERY_FULLCONT)
        ↓
   Dỡ hàng từ container
   (Kiểm tra: driver/vehicle whitelist tại warehouse)

6. TRẢ CONTAINER RỖNG:
   DepotContainer (DEPOT_DELIVERY_EMPTYCONT)
        ↓
   Trả container rỗng về depot
   (Mất: deliveryContainerDuration phút)
   (Phải trả đúng depot của ShipCompany)

7. TRẢ RƠ-MOÓC:
   DepotMooc (DELIVERY_MOOC)
        ↓
   Tháo mooc khỏi truck
   (Mất: deliveryMoocDuration phút)

8. KẾT THÚC CA:
   DepotTruck (END_TRUCK)
        ↓
   Truck về depot gốc
   (Phải trước: endWorkingTime)
```

### Ràng buộc Tích hợp

#### 1. Ràng buộc Thiết bị (Equipment Constraints)

**ContainerCapacityConstraint**:
```
∑(weight of containers on mooc) ≤ mooc.weight
```

**ContainerCarriedByTrailerConstraint**:
```
Container chỉ có thể được vận chuyển khi có mooc phù hợp
```

**MoocCapacityConstraint**:
```
Số lượng và loại container phải phù hợp với MoocPacking
Ví dụ: Mooc 40ft có thể chở:
  - 1 container 40ft, HOẶC
  - 2 container 20ft
```

#### 2. Ràng buộc Địa điểm (Place Constraints)

**Time Window Constraint** (Warehouse):
```
checkin[i].earliestTime ≤ arrivalTime ≤ checkin[i].latestTime
```

**Vehicle Access Constraint** (Warehouse):
```
IF warehouse.vehicles != null AND warehouse.vehicles.length > 0:
    truck.id MUST BE IN warehouse.vehicles[]
```

**Driver Access Constraint** (Warehouse):
```
IF warehouse.drivers != null AND warehouse.drivers.length > 0:
    truck.driverID MUST BE IN warehouse.drivers[]
```

**Working Time Constraint** (Truck):
```
truck.startWorkingTime ≤ routeTime ≤ truck.endWorkingTime
```

**Depot Return Constraint** (Container):
```
returnDepot MUST BE IN container.returnDepotCodes[]
AND returnDepot MUST BE IN shipCompany.containerDepotCodes[]
```

#### 3. Ràng buộc Định tuyến (Routing Constraints)

**Route Structure**:
```
START_TRUCK → PICKUP_MOOC → [Operations] → DELIVERY_MOOC → END_TRUCK
```

**Mooc Rule**:
```
Trước bất kỳ container operation nào, phải có PICKUP_MOOC
Sau tất cả container operations, phải có DELIVERY_MOOC
```

**Container Lifecycle**:
```
PICKUP_EMPTYCONT → PICKUP_FULLCONT → DELIVERY_FULLCONT → DELIVERY_EMPTYCONT
```

---

## IV. Ví dụ Thực tế

### Ví dụ 1: Tuyến Đường Hoàn Chỉnh

```
Truck ID: 1 (TRUCK-001)
Driver: Nguyễn Văn A (ID: 101)
Working Time: 06:00 - 18:00

Tuyến đường:
─────────────────────────────────────────────────────────
1. [06:00] 9 (START_TRUCK)
   Location: DepotTruck DT-9
   → Truck bắt đầu ca làm việc

2. [06:20] 15 (PICKUP_MOOC)
   Location: DepotMooc DM-15
   Mooc: MOOC-40FT-003 (category: 40ft, weight: 30000kg)
   Duration: 15 phút
   → Gắn mooc vào truck

3. [06:45] 22 (DEPOT_PICKUP_EMPTYCONT)
   Location: DepotContainer DC-22
   Container: CONT-20FT-105 (weight: 2400kg, category: 20ft)
   ShipCompany: MAERSK
   Duration: 10 phút
   → Lấy container rỗng

4. [07:30] 45 (WH_PICKUP_FULLCONT)
   Location: Warehouse WH-45
   Time Window: 07:00 - 09:00 ✓
   Driver Access: [101, 102, 105] ✓ (Driver 101 được phép)
   Vehicle Access: [1, 3, 5, 7] ✓ (Truck 1 được phép)
   → Chất hàng điện tử vào container (15 phút)
   Arrival: 07:30 (trong time window)

5. [09:15] 3 (PORT_DELIVERY_FULLCONT)
   Location: Port SGN-PORT-3
   → Giao container đầy đến cảng để xuất khẩu
   (Container sẽ được chuyển lên tàu)

6. [10:00] 3 (PORT_PICKUP_FULLCONT)
   Location: Port SGN-PORT-3
   Container: CONT-40FT-207 (weight: 18000kg, category: 40ft)
   ShipCompany: MSC
   → Lấy container nhập khẩu từ cảng

7. [11:45] 52 (WH_DELIVERY_FULLCONT)
   Location: Warehouse WH-52
   Time Window: 10:00 - 14:00 ✓
   → Giao container đầy vào warehouse
   (Dỡ hàng may mặc nhập khẩu)

8. [12:30] 28 (DEPOT_DELIVERY_EMPTYCONT)
   Location: DepotContainer DC-28
   → Trả container 40ft rỗng về depot của MSC
   Duration: 10 phút
   (DC-28 thuộc containerDepotCodes của MSC)

9. [13:15] 15 (DELIVERY_MOOC)
   Location: DepotMooc DM-15
   Duration: 15 phút
   → Tháo mooc, trả về depot

10. [13:45] 9 (END_TRUCK)
    Location: DepotTruck DT-9
    → Kết thúc ca, truck về depot gốc
    (Trước endWorkingTime 18:00 ✓)

─────────────────────────────────────────────────────────
Tổng thời gian: 7 giờ 45 phút
Tổng quãng đường: 285 km
Containers xử lý: 2 (1 xuất, 1 nhập)
Status: Feasible ✓
```

### Ví dụ 2: Vi phạm Ràng buộc

```
Tình huống: Truck cố gắng giao hàng tại Warehouse bị hạn chế

Warehouse WH-89:
  - Time Window: 08:00 - 12:00
  - Allowed Drivers: [102, 105, 108]
  - Allowed Vehicles: [2, 4, 6]

Truck ID: 1, Driver ID: 101
Attempted Delivery Time: 13:30

❌ Vi phạm:
  1. Driver 101 KHÔNG ĐƯỢC PHÉP tại WH-89
     (101 not in [102, 105, 108])
  2. Arrival time 13:30 NGOÀI time window
     (13:30 > 12:00)
  
=> Tuyến đường KHÔNG HỢP LỆ
=> Solver phải tìm tuyến đường khác
```

### Ví dụ 3: JSON Input/Output

**Input - Container**:
```json
{
  "code": "CONT-20-001",
  "weight": 2400.0,
  "categoryCode": "20ft",
  "depotContainerCode": "DC-001",
  "returnDepotCodes": ["DC-001", "DC-003", "DC-007"],
  "importedContainer": false,
  "shipCompanyCode": "MAERSK"
}
```

**Input - Warehouse**:
```json
{
  "code": "WH-045",
  "locationCode": "LOC-HCM-045",
  "hardConstraintType": 1,
  "vehicleConstraintType": 2,
  "drivers": [101, 102, 105],
  "vehicles": [1, 3, 5, 7],
  "checkin": [
    {
      "earliestTime": "07:00",
      "latestTime": "09:00"
    },
    {
      "earliestTime": "13:00",
      "latestTime": "16:00"
    }
  ],
  "breaktimes": [
    {
      "start": "12:00",
      "end": "13:00"
    }
  ]
}
```

**Output - Route**:
```json
{
  "truckCode": "TRUCK-001",
  "driverName": "Nguyễn Văn A",
  "route": [
    {"locationCode": "DT-9", "action": "START_TRUCK", "time": "06:00"},
    {"locationCode": "DM-15", "action": "PICKUP_MOOC", "time": "06:20"},
    {"locationCode": "DC-22", "action": "DEPOT_PICKUP_EMPTYCONT", "time": "06:45", "containerCode": "CONT-20-105"},
    {"locationCode": "WH-45", "action": "WH_PICKUP_FULLCONT", "time": "07:30", "containerCode": "CONT-20-105"},
    {"locationCode": "PORT-3", "action": "PORT_DELIVERY_FULLCONT", "time": "09:15", "containerCode": "CONT-20-105"},
    {"locationCode": "DM-15", "action": "DELIVERY_MOOC", "time": "13:15"},
    {"locationCode": "DT-9", "action": "END_TRUCK", "time": "13:45"}
  ],
  "totalDistance": 285.5,
  "totalTime": 465,
  "status": "FEASIBLE"
}
```

---

## V. Bảng Thuật ngữ

### Thuật ngữ Tiếng Việt - Tiếng Anh

| Tiếng Việt | Tiếng Anh | Mô tả |
|------------|-----------|-------|
| Xe đầu kéo | Truck / Tractor unit | Phương tiện kéo rơ-moóc |
| Rơ-moóc | Mooc / Trailer / Semi-trailer | Xe kéo chở container |
| Container | Container | Thùng chứa hàng hóa tiêu chuẩn |
| Cảng | Port | Cảng biển xuất nhập khẩu |
| Kho hàng | Warehouse | Trung tâm phân phối/kho bãi |
| Depot | Depot | Bãi đỗ/kho chứa thiết bị |
| Hãng tàu | Shipping Company / Ship Company | Công ty vận tải biển |
| Tài xế | Driver | Người lái xe |
| Định tuyến | Routing | Lập kế hoạch tuyến đường |
| Time window | Time window / Khung thời gian | Khoảng thời gian cho phép |
| Ràng buộc | Constraint | Điều kiện bắt buộc |
| Container đầy | Full container / Laden container | Container có hàng |
| Container rỗng | Empty container | Container không có hàng |
| Xuất khẩu | Export | Giao hàng ra nước ngoài |
| Nhập khẩu | Import | Nhận hàng từ nước ngoài |
| Chặng | Leg / Segment | Một phần của tuyến đường |
| Bốc hàng | Pickup | Lấy/chất hàng |
| Dỡ hàng | Delivery / Unload | Giao/dỡ hàng |

### Các Loại Action trong Route

| Action Code | Tên đầy đủ | Mô tả |
|-------------|------------|-------|
| START_TRUCK | Start Truck | Bắt đầu tuyến đường tại DepotTruck |
| END_TRUCK | End Truck | Kết thúc tuyến đường tại DepotTruck |
| PICKUP_MOOC | Pickup Mooc | Lấy mooc từ DepotMooc |
| DELIVERY_MOOC | Delivery Mooc | Trả mooc về DepotMooc |
| DEPOT_PICKUP_EMPTYCONT | Depot Pickup Empty Container | Lấy container rỗng từ DepotContainer |
| DEPOT_DELIVERY_EMPTYCONT | Depot Delivery Empty Container | Trả container rỗng về DepotContainer |
| WH_PICKUP_EMPTYCONT | Warehouse Pickup Empty Container | Lấy container rỗng từ Warehouse |
| WH_DELIVERY_EMPTYCONT | Warehouse Delivery Empty Container | Giao container rỗng đến Warehouse |
| WH_PICKUP_FULLCONT | Warehouse Pickup Full Container | Lấy container đầy từ Warehouse |
| WH_DELIVERY_FULLCONT | Warehouse Delivery Full Container | Giao container đầy đến Warehouse |
| PORT_PICKUP_FULLCONT | Port Pickup Full Container | Lấy container đầy từ Port |
| PORT_DELIVERY_FULLCONT | Port Delivery Full Container | Giao container đầy đến Port |
| PORT_PICKUP_EMPTYCONT | Port Pickup Empty Container | Lấy container rỗng từ Port |
| PORT_DELIVERY_EMPTYCONT | Port Delivery Empty Container | Giao container rỗng đến Port |

### Ký hiệu và Quy ước

- **20ft, 40ft, 45ft**: Kích thước container theo feet (1 foot ≈ 30.48 cm)
  - 20ft: Container 20 feet (~6m)
  - 40ft: Container 40 feet (~12m)
  - 45ft: Container 45 feet (~13.7m)

- **kg**: Kilogram - đơn vị trọng lượng

- **HH:MM**: Định dạng thời gian (Giờ:Phút)
  - Ví dụ: 06:00 = 6 giờ sáng, 18:00 = 6 giờ chiều

- **✓**: Đáp ứng điều kiện / Hợp lệ

- **✗**: Không đáp ứng / Không áp dụng

- **❌**: Vi phạm ràng buộc / Lỗi

---

## VI. Request Models (Yêu cầu vận chuyển)

Nhóm `models/requests` mô tả nghiệp vụ ở cấp đơn hàng và cấp dòng công việc (order item). Hệ thống có 3 lớp cấu trúc:

1. **Cấp gom đơn (Order-level wrappers)**: gom nhiều request con theo `orderID/orderCode`
2. **Cấp request chi tiết**: mô tả luồng vận chuyển container cụ thể
3. **Cấp thông tin chặng warehouse**: mô tả time window và service duration cho từng kho

### 1. ExportContainerRequest

**File**: `models/requests/ExportContainerRequest.java`

#### Mục đích
Mô hình một yêu cầu xuất khẩu chi tiết cho **một container** từ depot qua 1 hoặc nhiều warehouse, sau đó ra cảng.

#### Thuộc tính chính

| Nhóm | Trường | Mô tả |
|------|--------|-------|
| Định danh | `orderItemID`, `orderID`, `orderCode` | ID dòng đơn, ID đơn, mã đơn |
| Swap | `isSwap`, `orderItemSwapID` | Thông tin yêu cầu đổi/chuyển cặp job |
| Container | `shipCompanyCode`, `depotContainerCode`, `containerCategory`, `containerCode`, `containerNo`, `weight` | Thông tin container và nguồn lấy |
| Depot pickup TW | `earlyDateTimePickupAtDepot`, `lateDateTimePickupAtDepot` | Time window lấy container tại depot |
| Warehouse | `pickupWarehouses[]` (`PickupWarehouseInfo`) | Danh sách điểm load trung gian |
| Cảng | `portCode`, `earlyDateTimeUnloadAtPort`, `lateDateTimeUnloadAtPort`, `unloadDuration` | Time window + thời gian hạ tại cảng |
| Khách hàng | `customerCode`, `customerName` | Metadata khách hàng |
| Trạng thái | `rejectCode` | Mã reject sau khi solver xử lý |

#### Logic quan trọng
- `getEarlyDateTimeLoadAtWarehouse()` trả về **mốc sớm nhất** trong mảng `pickupWarehouses`.
- `getLateDateTimeLoadAtWarehouse()` trả về **mốc muộn nhất** trong mảng `pickupWarehouses`.
- Hai hàm trên chuẩn hóa nhiều warehouse thành một khung tổng để heuristic kiểm tra khả thi nhanh.

### 2. ImportContainerRequest

**File**: `models/requests/ImportContainerRequest.java`

#### Mục đích
Mô hình một yêu cầu nhập khẩu chi tiết cho **một container** theo luồng cảng -> warehouse -> depot.

#### Thuộc tính chính

| Nhóm | Trường | Mô tả |
|------|--------|-------|
| Định danh | `orderItemID`, `orderID`, `orderCode` | ID dòng đơn và đơn tổng |
| Swap | `isSwap`, `orderItemSwapID` | Cờ ghép/đổi công việc |
| Container | `shipCompanyCode`, `depotContainerCode[]`, `containerCategory`, `containerCode`, `containerNo`, `weight` | Cho phép nhiều depot trả về hợp lệ |
| Cảng pickup TW | `portCode`, `earlyDateTimePickupAtPort`, `lateDateTimePickupAtPort`, `loadDuration` | Lấy container đầy tại cảng |
| Warehouse | `deliveryWarehouses[]` (`DeliveryWarehouseInfo`) | Danh sách điểm dỡ hàng |
| Depot return TW | `earlyDateTimeDeliveryAtDepot`, `lateDateTimeDeliveryAtDepot` | Khung thời gian trả container về depot |
| Khách hàng | `customerCode`, `customerName` | Metadata đơn |
| Trạng thái | `rejectCode` | Mã reject |

#### Logic quan trọng
- `getEarlyDateTimeUnloadAtWarehouse()` lấy mốc sớm nhất trong `deliveryWarehouses`.
- `getLateDateTimeUnloadAtWarehouse()` lấy mốc muộn nhất trong `deliveryWarehouses`.

### 3. WarehouseContainerTransportRequest

**File**: `models/requests/WarehouseContainerTransportRequest.java`

#### Mục đích
Mô hình vận chuyển container giữa các warehouse theo tối đa 3 chặng:

1. Depot -> warehouse (empty)
2. Warehouse A -> warehouse B (laden)
3. Warehouse -> depot (empty)

#### Thuộc tính chính

| Nhóm | Trường | Mô tả |
|------|--------|-------|
| Định danh | `orderItemID`, `orderID`, `orderCode` | Mức dòng/chứng từ |
| Container | `containerCategory`, `containerCode`, `containerNo`, `weight`, `shipCompanyCode` | Metadata container |
| Chặng 1 | `fromWarehouseCode`, `earlyDateTimeLoad`, `lateDateTimeLoad`, `loadDuration`, `detachEmptyMoocContainerDurationFromWarehouse` | Lấy rỗng và đóng hàng |
| Chặng 2 | `earlyDateTimePickupLoadedContainerFromWarehouse`, `lateDateTimePickupLoadedContainerFromWarehouse`, `attachLoadedMoocContainerDurationFromWarehouse`, `toWarehouseCode`, `earlyDateTimeUnload`, `lateDateTimeUnload`, `unloadDuration`, `detachLoadedMoocContainerDurationToWarehouse` | Chuyển laden giữa kho |
| Chặng 3 | `earlyDateTimePickupEmptyContainerToWarehouse`, `lateDateTimePickupEmptyContainerToWarehouse`, `attachEmptyMoocContainerDurationToWarehouse`, `returnDepotContainerCodes[]` | Thu hồi rỗng về depot |
| Điều khiển | `levelRequest` | Mã segment: `1`, `2`, `3`, `12`, `23`, `123` |
| Depot đặc thù | `getDepotContainerCode`, `returnDepotContainerCode` | Ưu tiên lấy/trả depot cụ thể |
| Khách hàng | `customerCode`, `customerName` | Metadata |
| Trạng thái | `rejectCode` | Mã reject |

### 4. PickupWarehouseInfo

**File**: `models/requests/PickupWarehouseInfo.java`

Mô hình phụ cho một điểm warehouse trong luồng export:

- `wareHouseCode`
- `earlyDateTimeLoadAtWarehouse`, `lateDateTimeLoadAtWarehouse`
- `loadDuration`, `detachEmptyMoocContainerDuration`
- `earlyDateTimePickupLoadedContainerAtWarehouse`, `lateDateTimePickupLoadedContainerAtWarehouse`
- `attachLoadedMoocContainerDuration`

### 5. DeliveryWarehouseInfo

**File**: `models/requests/DeliveryWarehouseInfo.java`

Mô hình phụ cho một điểm warehouse trong luồng import:

- `wareHouseCode`
- `earlyDateTimeUnloadAtWarehouse`, `lateDateTimeUnloadAtWarehouse`
- `unloadDuration`, `detachLoadedMoocContainerDuration`
- `earlyPickupEmptyContainerAtWarehouse`, `latePickupEmptyContainerAtWarehouse`
- `attachEmptyMoocContainerDuration`

### 6. Các request gom theo order

#### ExportContainerTruckMoocRequest
**File**: `models/requests/ExportContainerTruckMoocRequest.java`

- `orderID`, `orderCode`
- `containerRequest[]` kiểu `ExportContainerRequest`

=> Một order export có thể chứa nhiều request container con.

#### ImportContainerTruckMoocRequest
**File**: `models/requests/ImportContainerTruckMoocRequest.java`

- `orderID`, `orderCode`
- `containerRequest[]` kiểu `ImportContainerRequest`

=> Một order import có thể chứa nhiều request container con.

#### WarehouseTransportRequest
**File**: `models/requests/WarehouseTransportRequest.java`

- `orderID`, `orderCode`
- `warehouseContainerTransportRequests[]` kiểu `WarehouseContainerTransportRequest`

### 7. Các request dạng chuẩn hóa input JSON

Các lớp này bám sát schema input ở file dữ liệu:

- `ExportEmptyRequests` (`models/requests/ExportEmptyRequests.java`)
- `ExportLadenRequests` (`models/requests/ExportLadenRequests.java`)
- `ImportEmptyRequests` (`models/requests/ImportEmptyRequests.java`)
- `ImportLadenRequests` (`models/requests/ImportLadenRequests.java`)

#### Điểm chung
- Đều có các trường: `id`, `isBreakRomooc`, `containerCategory/containerType/containerCode`, `orderCode`, `customerCode`, `requestDate`, `weight`, `rejectCode`, `prevStatusID`.
- Đều mô tả time window tác nghiệp và service duration cho từng chặng.
- Đều được dùng trực tiếp trong `ContainerTruckMoocInput` và `TruckMoocContainerOutputJson` (danh sách unscheduled).

---

## VII. Input Models (Dữ liệu đầu vào)

### 1. ContainerTruckMoocInput

**File**: `models/input/ContainerTruckMoocInput.java`

#### Mục đích
Root model gom toàn bộ dữ liệu đầu vào cho solver.

#### Thành phần chính

| Nhóm | Trường |
|------|--------|
| Request tổng hợp | `exRequests[]`, `imRequests[]`, `warehouseRequests[]` |
| Request chuẩn hóa | `exEmptyRequests[]`, `exLadenRequests[]`, `imEmptyRequests[]`, `imLadenRequests[]` |
| Places | `companies[]`, `depotContainers[]`, `depotMoocs[]`, `depotTrucks[]`, `warehouses[]`, `ports[]` |
| Equipment | `trucks[]`, `moocs[]`, `moocGroup[]`, `containers[]` |
| Matrix | `distance[]`, `travelTime[]` (đều kiểu `DistanceElement`) |
| Cấu hình | `params` (`ConfigParam`) |

#### Ghi chú
- Class cung cấp 2 constructor: một constructor đầy đủ (bao gồm cả nhóm request chuẩn hóa), và một constructor rút gọn.

### 2. ConfigParam

**File**: `models/input/ConfigParam.java`

#### Mục đích
Cấu hình runtime cho solver và bật/tắt constraint nghiệp vụ.

#### Nhóm tham số

| Nhóm | Trường | Ý nghĩa |
|------|--------|---------|
| Thời gian thao tác mooc | `cutMoocDuration`, `linkMoocDuration` | Cắt/nối mooc |
| Biên thời gian | `hourPrev`, `hourPost`, `currentTime` | Mở rộng phạm vi thời gian xét |
| Chiến lược | `strategy` | Tên chiến lược tối ưu |
| Cờ constraint kho | `constraintWarehouseTractor`, `constraintWarehouseDriver`, `constraintWarehouseVendor`, `constraintWarehouseBreaktimes`, `constraintWarehouseHard` | Bật/tắt từng ràng buộc |
| Cân bằng | `constraintDriverBalance` | Bật heuristic cân bằng tài xế |
| Thời gian thao tác container | `unlinkEmptyContainerDuration`, `unlinkLoadedContainerDuration`, `linkEmptyContainerDuration`, `linkLoadedContainerDuration` | Các duration phục vụ |

### 3. DistanceElement

**File**: `models/input/DistanceElement.java`

#### Mục đích
Đại diện một cung trong ma trận khoảng cách/thời gian giữa 2 location code.

#### Thuộc tính

- `srcCode`, `destCode`
- `distance`, `travelTime`
- `d`, `t` (biến dự phòng/biến thể dữ liệu)
- `isDriverBalance`, `drivers[]` (metadata phục vụ logic cân bằng tài xế)

---

## VIII. Routing Models (Mô hình tuyến)

### 1. RouteElement

**File**: `models/routing/RouteElement.java`

Đơn vị nhỏ nhất của một tuyến:

- `locationCode`
- `action` (ví dụ `PICKUP_MOOC`, `WH_DELIVERY_FULLCONT`)
- `arrivalTime`, `departureTime`
- `travelTime`

### 2. TruckRoute

**File**: `models/routing/TruckRoute.java`

Mô tả một tuyến thực thi cho một truck:

- `truck` (tham chiếu `models.equipments.Truck`)
- `nbStops`
- `travelTime`
- `nodes[]` kiểu `RouteElement`

---

## IX. Output Models (Đầu ra giải pháp)

### 1. TruckMoocContainerOutputJson

**File**: `models/output/TruckMoocContainerOutputJson.java`

DTO đầu ra chính trả về sau khi solver chạy:

- `truckRoutes[]` kiểu `TruckRoute`
- `unscheduledExEmptyRequests[]`
- `unscheduledExLadenRequests[]`
- `unscheduledImEmptyRequests[]`
- `unscheduledImLadenRequests[]`
- `statisticInformation` kiểu `StatisticInformation`

### 2. StatisticInformation

**File**: `models/output/StatisticInformation.java`

Thống kê tổng hợp:

- `totalRequests`
- `totalRejectedRequests`
- `totalDistance`
- `numberTrucks`

### 3. TruckContainerSolution

**File**: `models/output/TruckContainerSolution.java`

Mô hình snapshot nghiệm dùng bên trong quá trình tối ưu (ALNS/local search), không chỉ là DTO cuối.

#### Thuộc tính chính

- `_route`: danh sách route theo `Point` nội bộ của VRP
- `_rejectPickupPoints`, `_rejectDeliveryPoints`: tập điểm bị reject
- `_point2Group`, `_group2marked`: ánh xạ nhóm request và trạng thái đánh dấu
- `_cost`, `_nbTrucks`, `_nbReject`: KPI nghiệm

#### Hàm nghiệp vụ quan trọng

- `copy2XR(VarRoutesVR XR)`: ghi lại snapshot nghiệm vào cấu trúc route hiện hành của VRP engine
- `getNbRejectedRequests()`: đếm số request bị reject theo **group**, tránh đếm trùng point cùng nhóm

---

## X. Bảng Bao phủ Model

### Danh sách model đã đặc tả trong tài liệu

| Package | Số lớp | Trạng thái |
|---------|--------|------------|
| `models/equipments` | 5 | Đã đặc tả |
| `models/places` | 6 | Đã đặc tả |
| `models/requests` | 12 | Đã đặc tả |
| `models/input` | 3 | Đã đặc tả |
| `models/routing` | 2 | Đã đặc tả |
| `models/output` | 3 | Đã đặc tả |

**Tổng cộng**: 31 lớp model nghiệp vụ trong thư mục `models/` đã được mô tả.

---

## Kết luận

Tài liệu này cung cấp đặc tả chi tiết cho **toàn bộ model trong thư mục `models/`** của hệ thống vận tải container-truck-mooc, bao gồm Equipment, Places, Requests, Input, Routing và Output. Các model này tạo thành nền tảng cho:

1. **Vehicle Routing Problem (VRP)** - Bài toán định tuyến xe
2. **Constraint Satisfaction** - Thỏa mãn các ràng buộc phức tạp
3. **Logistics Optimization** - Tối ưu hóa chuỗi cung ứng
4. **Multi-modal Transportation** - Vận tải đa phương thức

### Các File Liên quan

- **Equipment Models**: `models/equipments/*.java`
- **Place Models**: `models/places/*.java`
- **Request Models**: `models/requests/*.java`
- **Input Models**: `models/input/*.java`
- **Routing Models**: `models/routing/*.java`
- **Output Models**: `models/output/*.java`
- **Constraint Classes**: `constraints/*.java`
- **Solver**: `solver/TruckContainerSolver.java`
- **VRP Engine**: `vrp/*.java`

### Tham khảo Thêm

- Để hiểu về các Request models (ImportRequest, ExportRequest, etc.), xem `models/requests/`
- Để hiểu về routing algorithm, xem `solver/opt/ALNS.java` và `solver/opt/SearchOptimumSolution.java`
- Để hiểu về constraint validation, xem `constraints/*.java`

---

**Phiên bản**: 1.1  
**Ngày cập nhật**: 2026-03-28  
**Người biên soạn**: Generated by GitHub Copilot
