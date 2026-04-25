import requests
import json

def preprocess_message(message: dict) -> dict:
    """
    Hàm nhận message, gọi OSRM và chuẩn bị Data Model cho OR-Tools.
    """
    # ---------------------------------------------------------
    # BƯỚC 1: TRÍCH XUẤT TỌA ĐỘ & LƯU LẠI MAPPING NODE ID
    # ---------------------------------------------------------
    depot = message['depot']
    slips = message['slips']
    vehicles = message['vehicles']
    
    # Danh sách tọa độ (Chú ý: OSRM yêu cầu định dạng là Longitude,Latitude)
    coordinates = []
    
    # Node 0 luôn là Depot
    coordinates.append(f"{depot['lng']},{depot['lat']}")
    
    # Danh sách map từ Index (số nguyên) sang Node ID (String) để lát nữa parse kết quả
    index_to_node_id = {0: depot['depot_id']}
    
    # Node 1 -> N là các điểm giao hàng
    for i, slip in enumerate(slips):
        index = i + 1
        coordinates.append(f"{slip['lng']},{slip['lat']}")
        index_to_node_id[index] = slip['slip_id']

    # ---------------------------------------------------------
    # BƯỚC 2: GỌI API OSRM LẤY MA TRẬN KHOẢNG CÁCH
    # ---------------------------------------------------------
    # Nối các tọa độ bằng dấu chấm phẩy ";"
    coord_string = ";".join(coordinates)
    
    # Sử dụng OSRM Demo Server (Lưu ý: Trên Production bạn nên dựng server OSRM riêng)
    # Tham số ?annotations=distance yêu cầu trả về Khoảng Cách (mét), mặc định là Thời Gian (giây)
    osrm_url = f"http://localhost:5000/table/v1/driving/{coord_string}?annotations=distance"
    
    try:
        response = requests.get(osrm_url, timeout=10)
        response.raise_for_status() # Bắn lỗi nếu HTTP Status không phải 200 OK
        osrm_data = response.json()
        
        if osrm_data['code'] != 'Ok':
            raise Exception(f"Lỗi từ OSRM: {osrm_data.get('message', 'Unknown Error')}")
            
        distance_matrix = osrm_data['distances']
        
    except requests.exceptions.RequestException as e:
        print(f"Lỗi khi kết nối đến OSRM: {e}")
        return None

    # Chuyển đổi float thành int (OR-Tools chạy tốt nhất với số nguyên)
    # OSRM trả về mét dạng float (ví dụ 1250.5m), ta làm tròn thành integer
    for i in range(len(distance_matrix)):
        for j in range(len(distance_matrix[i])):
            distance_matrix[i][j] = int(distance_matrix[i][j])

    # ---------------------------------------------------------
    # BƯỚC 3: THÊM DUMMY END NODE (ĐIỂM KẾT THÚC ẢO)
    # ---------------------------------------------------------
    num_nodes = len(distance_matrix)
    dummy_node_index = num_nodes # Vị trí index cuối cùng
    
    # Thêm 1 cột số 0 vào tất cả các hàng hiện tại
    for row in distance_matrix:
        row.append(0)
        
    # Thêm 1 hàng số 0 cho chính Dummy Node
    dummy_row = [0] * (num_nodes + 1)
    distance_matrix.append(dummy_row)

    # ---------------------------------------------------------
    # BƯỚC 4: TRẢI PHẲNG (FLATTEN) CÁC RÀNG BUỘC (WEIGHT, VOLUME)
    # ---------------------------------------------------------
    weight_demands = [0] # Depot không có nhu cầu khối lượng
    volume_demands = [0] # Depot không có nhu cầu thể tích
    
    for slip in slips:
        # Ép kiểu int nếu cần thiết
        weight_demands.append(int(slip['weight']))
        volume_demands.append(int(slip['volume']))
        
    # Thêm nhu cầu 0 cho Dummy End Node
    weight_demands.append(0)
    volume_demands.append(0)

    # ---------------------------------------------------------
    # BƯỚC 5: XỬ LÝ DỮ LIỆU XE (VEHICLES)
    # ---------------------------------------------------------
    vehicle_max_weights = []
    vehicle_max_volumes = []
    vehicle_ids = []
    
    for vehicle in vehicles:
        vehicle_max_weights.append(int(vehicle['max_weight']))
        vehicle_max_volumes.append(int(vehicle['max_volume']))
        vehicle_ids.append(vehicle['vehicle_id'])
        
    num_vehicles = len(vehicles)

    # ---------------------------------------------------------
    # BƯỚC 6: ĐÓNG GÓI MODEL ĐỂ TRẢ VỀ CHO OR-TOOLS
    # ---------------------------------------------------------
    # Tất cả các xe xuất phát từ Depot (Index 0)
    starts = [0] * num_vehicles
    
    # Tất cả các xe kết thúc ở Dummy Node (Index cuối)
    ends = [dummy_node_index] * num_vehicles

    model_data = {
        'plan_id': message['plan_id'],
        'distance_matrix': distance_matrix,
        'weight_demands': weight_demands,
        'volume_demands': volume_demands,
        'vehicle_max_weights': vehicle_max_weights,
        'vehicle_max_volumes': vehicle_max_volumes,
        'num_vehicles': num_vehicles,
        'starts': starts,
        'ends': ends,
        'vehicle_ids': vehicle_ids,
        'index_to_node_id': index_to_node_id # Cực kỳ quan trọng để lát map kết quả
    }

    return model_data

# ==========================================
# MÔ PHỎNG CHẠY THỬ (TEST)
# ==========================================
if __name__ == '__main__':
    # 1. Giả lập chuỗi JSON nhận được từ Kafka (giống hệt cấu trúc bạn mong muốn)
    kafka_json_string = """
    {
        "plan_id": "PLD-20231025001",
        "depot": {
            "depot_id": "FAC_000001",
            "lat": 21.028511,
            "lng": 105.804817
        },
        "vehicles": [
            {
            "vehicle_id": "VSP_000001",
            "max_weight": 500,
            "max_volume": 4500
            },
            {
            "vehicle_id": "VSP_000002",
            "max_weight": 1000,
            "max_volume": 8500
            }
        ],
        "slips": [
            {
            "slip_id": "DSL_000001",
            "lat": 21.031111,
            "lng": 105.811111,
            "weight": 50,
            "volume": 200
            },
            {
            "slip_id": "DSL_000002",
            "lat": 21.042222,
            "lng": 105.822222,
            "weight": 120,
            "volume": 500
            }
        ]
    }
    """
    
    # 2. Parse JSON thành Python Dictionary
    kafka_message = json.loads(kafka_json_string)
    
    # 3. Chạy khối Tiền xử lý
    print("Đang gọi OSRM và chuẩn bị dữ liệu...")
    or_tools_data = preprocess_message(kafka_message)
    
    # 4. In kết quả để kiểm tra
    if or_tools_data:
        print("\n=== DỮ LIỆU ĐẦU VÀO CHO OR-TOOLS ===")
        print(f"Số lượng Node thực tế: {len(or_tools_data['index_to_node_id'])}")
        print(f"Kích thước Ma trận (Đã thêm Dummy Node): {len(or_tools_data['distance_matrix'])}x{len(or_tools_data['distance_matrix'][0])}")
        print("\nMa trận khoảng cách (Mét):")
        for row in or_tools_data['distance_matrix']:
            print(row)
        print(f"\nNhu cầu Khối lượng: {or_tools_data['weight_demands']}")
        print(f"Nhu cầu Thể tích: {or_tools_data['volume_demands']}")
        print(f"Điểm xuất phát của xe: {or_tools_data['starts']}")
        print(f"Điểm kết thúc của xe: {or_tools_data['ends']}")
        print(f"\nMapping Index -> Node ID: {or_tools_data['index_to_node_id']}")