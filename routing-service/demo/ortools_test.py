from ortools.constraint_solver import routing_enums_pb2
from ortools.constraint_solver import pywrapcp

def solve_delivery_plan(data):
    """
    Hàm giải bài toán VRP.
    Nhận vào dictionary `data` và trả về dictionary kết quả.
    """
    # 1. Khởi tạo Manager và Model
    manager = pywrapcp.RoutingIndexManager(
        len(data['distance_matrix']),
        data['num_vehicles'],
        data['starts'],
        data['ends']
    )
    routing = pywrapcp.RoutingModel(manager)

    # 2. Callback Khoảng cách (Distance)
    def distance_callback(from_index, to_index):
        from_node = manager.IndexToNode(from_index)
        to_node = manager.IndexToNode(to_index)
        return data['distance_matrix'][from_node][to_node]

    transit_callback_index = routing.RegisterTransitCallback(distance_callback)
    routing.SetArcCostEvaluatorOfAllVehicles(transit_callback_index)

    # 3. Callback và Dimension cho Khối lượng (Weight)
    def weight_callback(from_index):
        from_node = manager.IndexToNode(from_index)
        return data['weight_demands'][from_node]

    weight_callback_index = routing.RegisterUnaryTransitCallback(weight_callback)
    routing.AddDimensionWithVehicleCapacity(
        weight_callback_index,
        0,  # null capacity slack (Không cho phép vượt quá)
        data['vehicle_max_weights'],  # Mảng tải trọng của từng xe
        True,  # start cumul to zero
        'Weight'
    )

    # 4. Callback và Dimension cho Thể tích (Volume)
    def volume_callback(from_index):
        from_node = manager.IndexToNode(from_index)
        # Chuyển đổi sang số nguyên nếu cần (OR-Tools làm việc tốt nhất với integer)
        # Ở đây giả sử bạn đã nhân thể tích thực với 1000 để thành số nguyên ở bước parse data
        return data['volume_demands'][from_node]

    volume_callback_index = routing.RegisterUnaryTransitCallback(volume_callback)
    routing.AddDimensionWithVehicleCapacity(
        volume_callback_index,
        0,  
        data['vehicle_max_volumes'], 
        True,  
        'Volume'
    )

    # 5. Cho phép rớt đơn (Tránh báo lỗi Infeasible khi không đủ xe)
    # Gắn một mức phạt (penalty) rất lớn cho mỗi đơn hàng nếu bị bỏ qua
    penalty = 1000000 
    for node in range(1, len(data['distance_matrix']) - 1): # Bỏ qua Depot và Dummy End
        routing.AddDisjunction([manager.NodeToIndex(node)], penalty)

    # 6. Cấu hình Search Parameters (Thời gian chạy)
    search_parameters = pywrapcp.DefaultRoutingSearchParameters()
    search_parameters.first_solution_strategy = (
        routing_enums_pb2.FirstSolutionStrategy.PATH_CHEAPEST_ARC)
    search_parameters.local_search_metaheuristic = (
        routing_enums_pb2.LocalSearchMetaheuristic.GUIDED_LOCAL_SEARCH)
    search_parameters.time_limit.seconds = 60  # Giới hạn chạy trong 60 giây

    # 7. CHẠY THUẬT TOÁN
    solution = routing.SolveWithParameters(search_parameters)

    # 8. PARSE KẾT QUẢ TRẢ VỀ
    if not solution:
        return {"status": "FAILED", "message": "Không tìm được giải pháp (Vô nghiệm)"}

    result = {
        "status": "COMPLETED",
        "total_distance": 0,
        "routes": [],
        "unused_vehicle_ids": [],
        "dropped_nodes": []
    }

    # Tìm các đơn hàng bị rớt
    for node in range(1, len(data['distance_matrix']) - 1):
        if solution.Value(routing.NextVar(manager.NodeToIndex(node))) == manager.NodeToIndex(node):
            result["dropped_nodes"].append(node)

    # Đọc lộ trình từng xe
    for vehicle_id in range(data['num_vehicles']):
        index = routing.Start(vehicle_id)
        next_index = solution.Value(routing.NextVar(index))
        
        # Nếu ngay sau Start là End -> Xe rỗng
        if routing.IsEnd(next_index):
            result["unused_vehicle_ids"].append(data['vehicle_ids'][vehicle_id])
            continue

        route_nodes = []
        route_distance = 0
        route_load_weight = 0
        route_load_volume = 0

        while not routing.IsEnd(index):
            node_index = manager.IndexToNode(index)
            # Bỏ qua Node 0 (Depot) nếu bạn không muốn lưu vào DB, hoặc giữ lại tùy bạn
            if node_index != 0: 
                route_nodes.append(node_index)
            
            route_load_weight += data['weight_demands'][node_index]
            route_load_volume += data['volume_demands'][node_index]
            
            previous_index = index
            index = solution.Value(routing.NextVar(index))
            route_distance += routing.GetArcCostForVehicle(previous_index, index, vehicle_id)

        # Lưu thông tin chuyến đi của xe này
        result["routes"].append({
            "vehicle_id": data['vehicle_ids'][vehicle_id],
            "distance": route_distance,
            "total_weight": route_load_weight,
            "total_volume": route_load_volume,
            "stops": route_nodes  # Mảng thứ tự các điểm giao
        })
        result["total_distance"] += route_distance

    return result

# =====================================================================
# BỘ DỮ LIỆU MẪU ĐỂ TEST (Mock Data)
# Đây là cấu trúc dữ liệu mà hàm Parse Kafka của bạn cần tạo ra
# =====================================================================
if __name__ == '__main__':
    # Node 0: Depot, Node 1-3: Đơn hàng, Node 4: Dummy End
    mock_data = {
        'distance_matrix': [
            [0, 10, 15, 20, 0],  # 0: Depot
            [10, 0, 8, 12, 0],   # 1: Slip A
            [15, 8, 0, 5, 0],    # 2: Slip B
            [20, 12, 5, 0, 0],   # 3: Slip C
            [0, 0, 0, 0, 0],     # 4: Dummy End Node
        ],
        'weight_demands': [0, 500, 300, 400, 0],  
        'volume_demands': [0, 20, 15, 10, 0], # Giả sử đã nhân 10 để thành số nguyên
        'vehicle_max_weights': [1000, 600, 2000],  # 3 xe tải
        'vehicle_max_volumes': [50, 30, 100],      
        'num_vehicles': 3,
        'starts': [0, 0, 0],     # Cả 3 xe xuất phát từ Node 0
        'ends': [4, 4, 4],       # Cả 3 xe kết thúc ở Node 4 (Dummy End)
        'vehicle_ids': ["V_001", "V_002", "V_003"]
    }

    final_result = solve_delivery_plan(mock_data)
    
    # In kết quả (để test)
    import json
    print(json.dumps(final_result, indent=2, ensure_ascii=False))