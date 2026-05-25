# Tài liệu Format Input JSON

## Mục lục
- [Tổng quan](#tổng-quan)
- [Cấu trúc JSON Tổng thể](#cấu-trúc-json-tổng-thể)
- [1. Request Models](#1-request-models)
  - [1.1 exEmptyRequests - Yêu cầu Xuất Container Rỗng](#11-exemptyrequests---yêu-cầu-xuất-container-rỗng)
  - [1.2 exLadenRequests - Yêu cầu Xuất Container Đầy](#12-exladenrequests---yêu-cầu-xuất-container-đầy)
  - [1.3 imEmptyRequests - Yêu cầu Nhập Container Rỗng](#13-imemptyrequests---yêu-cầu-nhập-container-rỗng)
  - [1.4 imLadenRequests - Yêu cầu Nhập Container Đầy](#14-imladenrequests---yêu-cầu-nhập-container-đầy)
- [2. Place Models](#2-place-models)
  - [2.1 depotContainers - Depot Container](#21-depotcontainers---depot-container)
  - [2.2 depotMoocs - Depot Mooc](#22-depotmoocs---depot-mooc)
  - [2.3 depotTrucks - Depot Truck](#23-depottru cks---depot-truck)
  - [2.4 warehouses - Warehouse](#24-warehouses---warehouse)
  - [2.5 ports - Port](#25-ports---port)
- [3. Equipment Models](#3-equipment-models)
  - [3.1 trucks - Truck](#31-trucks---truck)
  - [3.2 moocs - Mooc](#32-moocs---mooc)
  - [3.3 containers - Container](#33-containers---container)
- [4. Distance/Time Matrix](#4-distancetime-matrix)
- [5. Ví dụ Input Hoàn chỉnh](#5-ví-dụ-input-hoàn-chỉnh)
- [6. Quy tắc Validation](#6-quy-tắc-validation)

---

## Tổng quan

File input của hệ thống Truck-Container-Mooc Routing là một file JSON chứa toàn bộ dữ liệu cần thiết để giải bài toán định tuyến. Input bao gồm:

- **Requests (Yêu cầu)**: Các yêu cầu vận chuyển container xuất/nhập khẩu
- **Places (Địa điểm)**: Depot, warehouse, port trong mạng lưới logistics
- **Equipment (Thiết bị)**: Truck, mooc, container có sẵn
- **Distance Matrix**: Ma trận khoảng cách và thời gian di chuyển giữa các địa điểm

**Định dạng**: JSON (có thể trên một dòng hoặc formatted)  
**Encoding**: UTF-8  
**Vị trí**: `data/truck-container/input/*.txt` hoặc `*.json`

---

## Cấu trúc JSON Tổng thể

```json
{
    "exEmptyRequests": [...],      // Yêu cầu xuất container rỗng
    "exLadenRequests": [...],      // Yêu cầu xuất container đầy
    "imEmptyRequests": [...],      // Yêu cầu nhập/trả container rỗng
    "imLadenRequests": [...],      // Yêu cầu nhập container đầy
    
    "depotContainers": [...],      // Danh sách depot container
    "depotMoocs": [...],           // Danh sách depot mooc
    "depotTrucks": [...],          // Danh sách depot truck
    "warehouses": [...],           // Danh sách warehouse
    "ports": [...],                // Danh sách port
    
    "trucks": [...],               // Danh sách truck có sẵn
    "moocs": [...],                // Danh sách mooc có sẵn
    "containers": [...],           // Danh sách container
    
    "distance": [...]              // Ma trận khoảng cách
}
```

---

## 1. Request Models

### 1.1 exEmptyRequests - Yêu cầu Xuất Container Rỗng

**Mô tả**: Yêu cầu lấy container rỗng từ depot, chở đến warehouse để chất hàng xuất khẩu.

**Luồng nghiệp vụ**:
```
DepotContainer (lấy container rỗng) → Warehouse (chất hàng) → [chuyển sang exLadenRequests]
```

#### Cấu trúc JSON

```json
{
    "id": 0,
    "isBreakRomooc": false,
    "containerCode": "C-0",
    "earlyDateTimePickupAtDepot": "2019-06-14 17:15:57",
    "lateDateTimePickupAtDepot": "2019-06-15 00:15:57",
    "earlyDateTimeLoadAtWarehouse": "2019-06-14 21:37:36",
    "lateDateTimeLoadAtWarehouse": "2019-06-15 04:52:36",
    "weight": 0.0,
    "depotContainerCode": "23",
    "wareHouseCode": "50",
    "linkContainerDuration": 0,
    "rejectCode": 0,
    "prevStatusID": 0
}
```

#### Các Trường Dữ liệu

| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `id` | int | ✓ | ID của request (thường là 0 trong input) |
| `isBreakRomooc` | boolean | ✓ | Cờ đánh dấu có cần tháo mooc không (thường false) |
| `containerCode` | string | ✓ | Mã container cần vận chuyển (VD: "C-0", "C-1") |
| `earlyDateTimePickupAtDepot` | string | ✓ | Thời gian sớm nhất có thể lấy container tại depot (format: "YYYY-MM-DD HH:MM:SS") |
| `lateDateTimePickupAtDepot` | string | ✓ | Thời gian muộn nhất có thể lấy container tại depot |
| `earlyDateTimeLoadAtWarehouse` | string | ✓ | Thời gian sớm nhất có thể chất hàng tại warehouse |
| `lateDateTimeLoadAtWarehouse` | string | ✓ | Thời gian muộn nhất có thể chất hàng tại warehouse |
| `weight` | double | ✓ | Trọng lượng hàng hóa (kg) - có thể là 0 nếu chưa xác định |
| `depotContainerCode` | string | ✓ | Mã depot nơi lấy container rỗng |
| `wareHouseCode` | string | ✓ | Mã warehouse nơi chất hàng |
| `linkContainerDuration` | int | ✗ | Thời gian liên kết container (phút) - mặc định 0 |
| `rejectCode` | int | ✗ | Mã lỗi nếu request bị từ chối - mặc định 0 |
| `prevStatusID` | int | ✗ | ID trạng thái trước đó - mặc định 0 |

#### Ràng buộc Time Window

```
earlyDateTimePickupAtDepot ≤ actualPickupTime ≤ lateDateTimePickupAtDepot
earlyDateTimeLoadAtWarehouse ≤ actualLoadTime ≤ lateDateTimeLoadAtWarehouse
actualPickupTime < actualLoadTime  (phải lấy trước khi chất)
```

#### Ví dụ

```json
{
    "id": 0,
    "isBreakRomooc": false,
    "containerCode": "C-0",
    "earlyDateTimePickupAtDepot": "2019-06-14 17:15:57",
    "lateDateTimePickupAtDepot": "2019-06-15 00:15:57",
    "earlyDateTimeLoadAtWarehouse": "2019-06-14 21:37:36",
    "lateDateTimeLoadAtWarehouse": "2019-06-15 04:52:36",
    "weight": 0.0,
    "depotContainerCode": "23",
    "wareHouseCode": "50",
    "linkContainerDuration": 0,
    "rejectCode": 0,
    "prevStatusID": 0
}
```

**Giải thích**:
- Container C-0 phải được lấy từ depot 23 trong khoảng 17:15:57 đến 00:15:57 (ngày hôm sau)
- Sau đó chở đến warehouse 50 để chất hàng trong khoảng 21:37:36 đến 04:52:36 (ngày hôm sau)

---

### 1.2 exLadenRequests - Yêu cầu Xuất Container Đầy

**Mô tả**: Yêu cầu chở container đầy hàng từ warehouse đến cảng để xuất khẩu.

**Luồng nghiệp vụ**:
```
Warehouse (đã có container đầy) → Port (giao container để lên tàu)
```

#### Cấu trúc JSON

```json
{
    "id": 0,
    "isBreakRomooc": false,
    "earlyDateTimeAttachAtWarehouse": "2019-06-16 01:12:51",
    "lateDateTimeUnloadAtPort": "2019-06-16 11:29:53",
    "weight": 0.0,
    "wareHouseCode": "54",
    "portCode": "2",
    "linkContainerAtWarehouseDuration": 0,
    "releaseLoadedContainerAtPortDuration": 0,
    "rejectCode": 0,
    "prevStatusID": 0
}
```

#### Các Trường Dữ liệu

| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `id` | int | ✓ | ID của request |
| `isBreakRomooc` | boolean | ✓ | Cờ đánh dấu có cần tháo mooc không |
| `earlyDateTimeAttachAtWarehouse` | string | ✓ | Thời gian sớm nhất có thể gắn container tại warehouse |
| `lateDateTimeUnloadAtPort` | string | ✓ | Thời gian muộn nhất phải dỡ container tại cảng |
| `weight` | double | ✓ | Trọng lượng hàng hóa (kg) |
| `wareHouseCode` | string | ✓ | Mã warehouse nơi lấy container đầy |
| `portCode` | string | ✓ | Mã cảng nơi giao container |
| `linkContainerAtWarehouseDuration` | int | ✗ | Thời gian gắn container tại warehouse (phút) |
| `releaseLoadedContainerAtPortDuration` | int | ✗ | Thời gian dỡ container tại cảng (phút) |
| `rejectCode` | int | ✗ | Mã lỗi nếu bị từ chối |
| `prevStatusID` | int | ✗ | ID trạng thái trước |

#### Ví dụ

```json
{
    "id": 0,
    "isBreakRomooc": false,
    "earlyDateTimeAttachAtWarehouse": "2019-06-16 01:12:51",
    "lateDateTimeUnloadAtPort": "2019-06-16 11:29:53",
    "weight": 0.0,
    "wareHouseCode": "54",
    "portCode": "2",
    "linkContainerAtWarehouseDuration": 0,
    "releaseLoadedContainerAtPortDuration": 0,
    "rejectCode": 0,
    "prevStatusID": 0
}
```

**Giải thích**:
- Container đầy tại warehouse 54 cần được lấy sớm nhất lúc 01:12:51
- Phải được giao đến cảng 2 trước 11:29:53

---

### 1.3 imEmptyRequests - Yêu cầu Nhập Container Rỗng

**Mô tả**: Yêu cầu trả container rỗng từ warehouse về depot sau khi đã dỡ hàng.

**Luồng nghiệp vụ**:
```
Warehouse (đã dỡ hàng, container rỗng) → DepotContainer (trả container)
```

#### Cấu trúc JSON

```json
{
    "id": 0,
    "isBreakRomooc": false,
    "lateDateTimeReturnEmptyAtDepot": "2019-06-15 17:02:13",
    "weight": 0.0,
    "wareHouseCode": "54",
    "depotContainerCode": "27",
    "linkContainerDuration": 0,
    "rejectCode": 0,
    "prevStatusID": 0
}
```

#### Các Trường Dữ liệu

| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `id` | int | ✓ | ID của request |
| `isBreakRomooc` | boolean | ✓ | Cờ đánh dấu có cần tháo mooc không |
| `lateDateTimeReturnEmptyAtDepot` | string | ✓ | Thời gian muộn nhất phải trả container rỗng về depot |
| `weight` | double | ✓ | Trọng lượng (thường là 0 cho container rỗng) |
| `wareHouseCode` | string | ✓ | Mã warehouse nơi lấy container rỗng |
| `depotContainerCode` | string | ✓ | Mã depot nơi trả container |
| `linkContainerDuration` | int | ✗ | Thời gian liên kết container (phút) |
| `rejectCode` | int | ✗ | Mã lỗi nếu bị từ chối |
| `prevStatusID` | int | ✗ | ID trạng thái trước |

#### Ví dụ

```json
{
    "id": 0,
    "isBreakRomooc": false,
    "lateDateTimeReturnEmptyAtDepot": "2019-06-15 17:02:13",
    "weight": 0.0,
    "wareHouseCode": "54",
    "depotContainerCode": "27",
    "linkContainerDuration": 0,
    "rejectCode": 0,
    "prevStatusID": 0
}
```

**Giải thích**:
- Lấy container rỗng từ warehouse 54
- Phải trả về depot 27 trước 17:02:13

---

### 1.4 imLadenRequests - Yêu cầu Nhập Container Đầy

**Mô tả**: Yêu cầu lấy container đầy (hàng nhập khẩu) từ cảng và chở đến warehouse để dỡ hàng.

**Luồng nghiệp vụ**:
```
Port (lấy container từ tàu) → Warehouse (dỡ hàng)
```

#### Cấu trúc JSON

```json
{
    "id": 0,
    "isBreakRomooc": false,
    "earlyDateTimePickupAtPort": "2019-06-16 04:44:37",
    "lateDateTimePickupAtPort": "2019-06-16 11:44:37",
    "earlyDateTimeUnloadAtWarehouse": "2019-06-16 08:36:01",
    "lateDateTimeUnloadAtWarehouse": "2019-06-16 15:51:01",
    "weight": 0.0,
    "portCode": "2",
    "wareHouseCode": "54",
    "linkLoadedContainerAtPortDuration": 0,
    "rejectCode": 0,
    "prevStatusID": 0
}
```

#### Các Trường Dữ liệu

| Trường | Kiểu | Bắt buộc | Mô tả |
|--------|------|----------|-------|
| `id` | int | ✓ | ID của request |
| `isBreakRomooc` | boolean | ✓ | Cờ đánh dấu có cần tháo mooc không |
| `earlyDateTimePickupAtPort` | string | ✓ | Thời gian sớm nhất có thể lấy container tại cảng |
| `lateDateTimePickupAtPort` | string | ✓ | Thời gian muộn nhất có thể lấy container tại cảng |
| `earlyDateTimeUnloadAtWarehouse` | string | ✓ | Thời gian sớm nhất có thể dỡ hàng tại warehouse |
| `lateDateTimeUnloadAtWarehouse` | string | ✓ | Thời gian muộn nhất có thể dỡ hàng tại warehouse |
| `weight` | double | ✓ | Trọng lượng hàng hóa (kg) |
| `portCode` | string | ✓ | Mã cảng nơi lấy container |
| `wareHouseCode` | string | ✓ | Mã warehouse nơi dỡ hàng |
| `linkLoadedContainerAtPortDuration` | int | ✗ | Thời gian gắn container tại cảng (phút) |
| `rejectCode` | int | ✗ | Mã lỗi nếu bị từ chối |
| `prevStatusID` | int | ✗ | ID trạng thái trước |

#### Ví dụ

```json
{
    "id": 0,
    "isBreakRomooc": false,
    "earlyDateTimePickupAtPort": "2019-06-16 04:44:37",
    "lateDateTimePickupAtPort": "2019-06-16 11:44:37",
    "earlyDateTimeUnloadAtWarehouse": "2019-06-16 08:36:01",
    "lateDateTimeUnloadAtWarehouse": "2019-06-16 15:51:01",
    "weight": 0.0,
    "portCode": "2",
    "wareHouseCode": "54",
    "linkLoadedContainerAtPortDuration": 0,
    "rejectCode": 0,
    "prevStatusID": 0
}
```

**Giải thích**:
- Lấy container nhập khẩu từ cảng 2 trong khoảng 04:44:37 - 11:44:37
- Dỡ hàng tại warehouse 54 trong khoảng 08:36:01 - 15:51:01

---

## 2. Place Models

### 2.1 depotContainers - Depot Container

**Mô tả**: Depot lưu trữ container, nơi lấy container rỗng và trả container về.

#### Cấu trúc JSON

```json
{
    "code": "23",
    "locationCode": "23",
    "pickupContainerDuration": 0,
    "deliveryContainerDuration": 0,
    "returnedContainer": false
}
```

#### Các Trường

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `code` | string | Mã định danh depot |
| `locationCode` | string | Mã vị trí (thường giống code) |
| `pickupContainerDuration` | int | Thời gian lấy container (phút) |
| `deliveryContainerDuration` | int | Thời gian giao/trả container (phút) |
| `returnedContainer` | boolean | Cờ đánh dấu depot có nhận container trả về |

---

### 2.2 depotMoocs - Depot Mooc

**Mô tả**: Depot lưu trữ mooc, nơi truck lấy và trả mooc.

#### Cấu trúc JSON

```json
{
    "code": "19",
    "locationCode": "19",
    "pickupMoocDuration": 0,
    "deliveryMoocDuration": 0
}
```

#### Các Trường

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `code` | string | Mã định danh depot mooc |
| `locationCode` | string | Mã vị trí |
| `pickupMoocDuration` | int | Thời gian lấy mooc (phút) |
| `deliveryMoocDuration` | int | Thời gian trả mooc (phút) |

---

### 2.3 depotTrucks - Depot Truck

**Mô tả**: Depot gốc của truck, nơi truck xuất phát và kết thúc ca làm việc.

#### Cấu trúc JSON

```json
{
    "code": "8",
    "locationCode": "8"
}
```

#### Các Trường

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `code` | string | Mã định danh depot truck |
| `locationCode` | string | Mã vị trí |

---

### 2.4 warehouses - Warehouse

**Mô tả**: Kho hàng/trung tâm phân phối, nơi chất và dỡ hàng.

#### Cấu trúc JSON

```json
{
    "code": "38",
    "locationCode": "38",
    "hardConstraintType": 0,
    "vehicleConstraintType": 0
}
```

#### Các Trường

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `code` | string | Mã định danh warehouse |
| `locationCode` | string | Mã vị trí |
| `hardConstraintType` | int | Loại ràng buộc cứng (0 = không có) |
| `vehicleConstraintType` | int | Loại ràng buộc phương tiện (0 = không hạn chế) |

**Lưu ý**: Trong file input mẫu, các warehouse không có thông tin về `drivers`, `vehicles`, `checkin`, `breaktimes`. Các trường này có thể được thêm vào nếu cần thiết.

---

### 2.5 ports - Port

**Mô tả**: Cảng biển, nơi xuất/nhập container.

#### Cấu trúc JSON

```json
{
    "code": "2",
    "locationCode": "2"
}
```

#### Các Trường

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `code` | string | Mã định danh cảng |
| `locationCode` | string | Mã vị trí |

---

## 3. Equipment Models

### 3.1 trucks - Truck

**Mô tả**: Xe đầu kéo, phương tiện chính thực hiện vận chuyển.

#### Cấu trúc JSON

```json
{
    "id": 0,
    "code": "T-0",
    "weight": 0.0,
    "driverID": 0,
    "depotTruckCode": "8",
    "depotTruckLocationCode": "8",
    "startWorkingTime": "2019-06-12 00:00:00",
    "returnDepotCodes": ["8"]
}
```

#### Các Trường

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `id` | int | ID số của truck |
| `code` | string | Mã/biển số truck |
| `weight` | double | Trọng tải xe |
| `driverID` | int | ID tài xế |
| `depotTruckCode` | string | Mã depot gốc |
| `depotTruckLocationCode` | string | Mã vị trí depot |
| `startWorkingTime` | string | Thời gian bắt đầu làm việc |
| `returnDepotCodes` | string[] | Mảng các depot có thể trả xe về |

**Lưu ý**: Trong mẫu không có `endWorkingTime`, `status`, `intervals` - có thể là optional.

---

### 3.2 moocs - Mooc

**Mô tả**: Rơ-moóc, chở container trên đó.

#### Cấu trúc JSON

```json
{
    "id": 0,
    "code": "M-0",
    "categoryId": 0,
    "weight": 0.0,
    "statusId": 0,
    "depotMoocCode": "19",
    "depotMoocLocationCode": "19",
    "returnDepotCodes": ["19"]
}
```

#### Các Trường

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `id` | int | ID số của mooc |
| `code` | string | Mã mooc |
| `categoryId` | int | ID loại mooc (0, 1, 2... tương ứng 20ft, 40ft...) |
| `weight` | double | Tải trọng tối đa |
| `statusId` | int | ID trạng thái |
| `depotMoocCode` | string | Mã depot gốc |
| `depotMoocLocationCode` | string | Mã vị trí depot |
| `returnDepotCodes` | string[] | Các depot có thể trả mooc về |

**Lưu ý**: Không có trường `category` (string), `status` (string), `intervals` trong mẫu.

---

### 3.3 containers - Container

**Mô tả**: Container chứa hàng hóa cần vận chuyển.

#### Cấu trúc JSON

```json
{
    "code": "C-0",
    "weight": 0.0,
    "depotContainerCode": "23",
    "returnDepotCodes": ["23"],
    "importedContainer": false
}
```

#### Các Trường

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `code` | string | Mã container |
| `weight` | double | Trọng lượng/khối lượng |
| `depotContainerCode` | string | Mã depot xuất phát |
| `returnDepotCodes` | string[] | Các depot có thể trả container về |
| `importedContainer` | boolean | Cờ đánh dấu container nhập khẩu |

**Lưu ý**: Không có trường `categoryCode`, `shipCompanyCode` trong mẫu - có thể là optional.

---

## 4. Distance/Time Matrix

**Mô tả**: Ma trận khoảng cách và thời gian di chuyển giữa tất cả các cặp địa điểm trong hệ thống.

#### Cấu trúc JSON

```json
{
    "srcCode": "0",
    "destCode": "2",
    "isDriverBalance": false,
    "distance": 5800.0,
    "travelTime": 522.0,
    "d": 0.0,
    "t": 0.0
}
```

#### Các Trường

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `srcCode` | string | Mã địa điểm xuất phát |
| `destCode` | string | Mã địa điểm đích |
| `isDriverBalance` | boolean | Cờ đánh dấu liên quan đến cân bằng tài xế |
| `distance` | double | Khoảng cách (mét) |
| `travelTime` | double | Thời gian di chuyển (giây) |
| `d` | double | Khoảng cách bổ sung (thường là 0) |
| `t` | double | Thời gian bổ sung (thường là 0) |

#### Ma trận Đầy đủ

Ma trận distance phải chứa **tất cả các cặp** (srcCode, destCode), bao gồm:
- Tất cả depot containers
- Tất cả depot moocs
- Tất cả depot trucks
- Tất cả warehouses
- Tất cả ports

**Công thức**: Nếu có `n` địa điểm, cần `n × n` phần tử trong mảng distance.

#### Ví dụ

```json
{
    "srcCode": "0",
    "destCode": "2",
    "isDriverBalance": false,
    "distance": 5800.0,
    "travelTime": 522.0,
    "d": 0.0,
    "t": 0.0
}
```

**Giải thích**:
- Từ địa điểm 0 đến địa điểm 2
- Khoảng cách: 5800m = 5.8km
- Thời gian: 522 giây ≈ 8.7 phút

---

## 5. Ví dụ Input Hoàn chỉnh

Dưới đây là một ví dụ input JSON đơn giản với 2 requests:

```json
{
    "exEmptyRequests": [
        {
            "id": 0,
            "isBreakRomooc": false,
            "containerCode": "C-0",
            "earlyDateTimePickupAtDepot": "2019-06-14 17:15:57",
            "lateDateTimePickupAtDepot": "2019-06-15 00:15:57",
            "earlyDateTimeLoadAtWarehouse": "2019-06-14 21:37:36",
            "lateDateTimeLoadAtWarehouse": "2019-06-15 04:52:36",
            "weight": 0.0,
            "depotContainerCode": "23",
            "wareHouseCode": "50",
            "linkContainerDuration": 0,
            "rejectCode": 0,
            "prevStatusID": 0
        }
    ],
    "exLadenRequests": [
        {
            "id": 0,
            "isBreakRomooc": false,
            "earlyDateTimeAttachAtWarehouse": "2019-06-16 01:12:51",
            "lateDateTimeUnloadAtPort": "2019-06-16 11:29:53",
            "weight": 0.0,
            "wareHouseCode": "54",
            "portCode": "2",
            "linkContainerAtWarehouseDuration": 0,
            "releaseLoadedContainerAtPortDuration": 0,
            "rejectCode": 0,
            "prevStatusID": 0
        }
    ],
    "imEmptyRequests": [],
    "imLadenRequests": [],
    "depotContainers": [
        {
            "code": "23",
            "locationCode": "23",
            "pickupContainerDuration": 0,
            "deliveryContainerDuration": 0,
            "returnedContainer": false
        }
    ],
    "depotMoocs": [
        {
            "code": "19",
            "locationCode": "19",
            "pickupMoocDuration": 0,
            "deliveryMoocDuration": 0
        }
    ],
    "depotTrucks": [
        {
            "code": "8",
            "locationCode": "8"
        }
    ],
    "warehouses": [
        {
            "code": "50",
            "locationCode": "50",
            "hardConstraintType": 0,
            "vehicleConstraintType": 0
        },
        {
            "code": "54",
            "locationCode": "54",
            "hardConstraintType": 0,
            "vehicleConstraintType": 0
        }
    ],
    "ports": [
        {
            "code": "2",
            "locationCode": "2"
        }
    ],
    "trucks": [
        {
            "id": 0,
            "code": "T-0",
            "weight": 0.0,
            "driverID": 0,
            "depotTruckCode": "8",
            "depotTruckLocationCode": "8",
            "startWorkingTime": "2019-06-12 00:00:00",
            "returnDepotCodes": ["8"]
        }
    ],
    "moocs": [
        {
            "id": 0,
            "code": "M-0",
            "categoryId": 0,
            "weight": 0.0,
            "statusId": 0,
            "depotMoocCode": "19",
            "depotMoocLocationCode": "19",
            "returnDepotCodes": ["19"]
        }
    ],
    "containers": [
        {
            "code": "C-0",
            "weight": 0.0,
            "depotContainerCode": "23",
            "returnDepotCodes": ["23"],
            "importedContainer": false
        }
    ],
    "distance": [
        {
            "srcCode": "8",
            "destCode": "8",
            "isDriverBalance": false,
            "distance": 0.0,
            "travelTime": 0.0,
            "d": 0.0,
            "t": 0.0
        },
        {
            "srcCode": "8",
            "destCode": "19",
            "isDriverBalance": false,
            "distance": 5000.0,
            "travelTime": 450.0,
            "d": 0.0,
            "t": 0.0
        },
        {
            "srcCode": "19",
            "destCode": "23",
            "isDriverBalance": false,
            "distance": 3000.0,
            "travelTime": 270.0,
            "d": 0.0,
            "t": 0.0
        }
        // ... more distance entries
    ]
}
```

---

## 6. Quy tắc Validation

### 6.1 Validation Requests

#### exEmptyRequests
```
✓ containerCode phải tồn tại trong mảng containers
✓ depotContainerCode phải tồn tại trong mảng depotContainers
✓ wareHouseCode phải tồn tại trong mảng warehouses
✓ earlyDateTimePickupAtDepot ≤ lateDateTimePickupAtDepot
✓ earlyDateTimeLoadAtWarehouse ≤ lateDateTimeLoadAtWarehouse
✓ Time window phải hợp lý (có thể thực hiện được)
```

#### exLadenRequests
```
✓ wareHouseCode phải tồn tại trong warehouses
✓ portCode phải tồn tại trong ports
✓ earlyDateTimeAttachAtWarehouse < lateDateTimeUnloadAtPort
```

#### imEmptyRequests
```
✓ wareHouseCode phải tồn tại trong warehouses
✓ depotContainerCode phải tồn tại trong depotContainers
✓ lateDateTimeReturnEmptyAtDepot phải hợp lý
```

#### imLadenRequests
```
✓ portCode phải tồn tại trong ports
✓ wareHouseCode phải tồn tại trong warehouses
✓ earlyDateTimePickupAtPort ≤ lateDateTimePickupAtPort
✓ earlyDateTimeUnloadAtWarehouse ≤ lateDateTimeUnloadAtWarehouse
✓ Thời gian pickup < thời gian unload
```

### 6.2 Validation Equipment

#### Trucks
```
✓ depotTruckCode phải tồn tại trong depotTrucks
✓ returnDepotCodes phải chứa các depot hợp lệ
✓ startWorkingTime phải có định dạng đúng "YYYY-MM-DD HH:MM:SS"
```

#### Moocs
```
✓ depotMoocCode phải tồn tại trong depotMoocs
✓ returnDepotCodes phải chứa các depot mooc hợp lệ
✓ categoryId phải hợp lệ (0, 1, 2...)
```

#### Containers
```
✓ depotContainerCode phải tồn tại trong depotContainers
✓ returnDepotCodes phải chứa các depot container hợp lệ
✓ code phải unique
```

### 6.3 Validation Distance Matrix

```
✓ Phải chứa tất cả các cặp (src, dest)
✓ srcCode và destCode phải là các location code hợp lệ
✓ distance ≥ 0
✓ travelTime ≥ 0
✓ Khi srcCode == destCode: distance = 0, travelTime = 0
```

### 6.4 Validation Tổng thể

```
✓ Tất cả các mã tham chiếu phải tồn tại
✓ Không có mã trùng lặp trong cùng một loại
✓ Số lượng containers ≥ số lượng requests cần containers
✓ Số lượng trucks ≥ 1
✓ Số lượng moocs ≥ số lượng trucks
✓ Ma trận distance phải đầy đủ
```

---

## 7. Quy ước và Best Practices

### 7.1 Quy ước Đặt tên

- **Truck codes**: `T-0`, `T-1`, `T-2`, ...
- **Mooc codes**: `M-0`, `M-1`, `M-2`, ...
- **Container codes**: `C-0`, `C-1`, `C-2`, ...
- **Location codes**: Số nguyên hoặc chuỗi ngắn: `"0"`, `"1"`, `"23"`, ...

### 7.2 Format Thời gian

- **Format chuẩn**: `"YYYY-MM-DD HH:MM:SS"`
- **Ví dụ**: `"2019-06-14 17:15:57"`
- **Lưu ý**: Sử dụng 24-hour format

### 7.3 Đơn vị Đo lường

- **Khoảng cách**: mét (m)
- **Thời gian**: giây (s)
- **Trọng lượng**: kilogram (kg)
- **Duration**: phút (minutes)

### 7.4 Giá trị Mặc định

- `weight`: `0.0` nếu chưa xác định
- `duration`: `0` nếu không có service time
- `isBreakRomooc`: `false` trong hầu hết trường hợp
- `rejectCode`: `0` (không bị từ chối)
- `prevStatusID`: `0` (trạng thái ban đầu)

---

## 8. Lỗi Thường gặp

### 8.1 Lỗi Format

❌ **Thời gian sai format**
```json
"earlyDateTimePickupAtDepot": "14/06/2019 17:15:57"  // SAI
```
✅ **Đúng**
```json
"earlyDateTimePickupAtDepot": "2019-06-14 17:15:57"  // ĐÚNG
```

### 8.2 Lỗi Tham chiếu

❌ **Container code không tồn tại**
```json
"exEmptyRequests": [
    {
        "containerCode": "C-99",  // Container C-99 không có trong mảng containers
        ...
    }
]
```

### 8.3 Lỗi Time Window

❌ **Time window không hợp lý**
```json
{
    "earlyDateTimePickupAtDepot": "2019-06-15 10:00:00",
    "lateDateTimePickupAtDepot": "2019-06-14 10:00:00"  // SAI: muộn nhất < sớm nhất
}
```

### 8.4 Lỗi Ma trận Khoảng cách

❌ **Thiếu cặp khoảng cách**
```json
// Có location "50" và "51" nhưng không có distance từ "50" đến "51"
```

---

## 9. Tools và Utilities

### 9.1 Format JSON

Để format file JSON từ một dòng sang dạng đẹp:

**PowerShell**:
```powershell
Get-Content input.txt | ConvertFrom-Json | ConvertTo-Json -Depth 10 | Set-Content formatted.json
```

**Python**:
```python
import json

with open('input.txt', 'r') as f:
    data = json.load(f)

with open('formatted.json', 'w') as f:
    json.dump(data, f, indent=4)
```

### 9.2 Validate JSON

**Python script**:
```python
import json

def validate_input(file_path):
    with open(file_path, 'r') as f:
        data = json.load(f)
    
    # Validate structure
    required_keys = [
        'exEmptyRequests', 'exLadenRequests',
        'imEmptyRequests', 'imLadenRequests',
        'depotContainers', 'depotMoocs', 'depotTrucks',
        'warehouses', 'ports',
        'trucks', 'moocs', 'containers',
        'distance'
    ]
    
    for key in required_keys:
        if key not in data:
            print(f"❌ Missing key: {key}")
            return False
    
    print("✅ All required keys present")
    return True

validate_input('input.txt')
```

---

## 10. Tham khảo Thêm

- **Model documentation**: Xem [README-Models.md](README-Models.md) để hiểu chi tiết về các model
- **Output format**: Xem thư mục `data/truck-container/output/` để xem format output
- **Solver**: File [TruckContainerSolver.java](solver/TruckContainerSolver.java) xử lý input

---

**Phiên bản**: 1.0  
**Ngày cập nhật**: 2026-03-08  
**File mẫu**: `data/truck-container/input/random-8reqs-RealLoc-0.txt`
