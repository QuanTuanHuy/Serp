from services.osrm_service import get_distance_matrix, get_route_polyline
from services.ortools_service import solve_delivery_plan

def process_routing_request(message: dict) -> dict:
    try:
        # 1. Trích xuất dữ liệu & Tọa độ
        depot = message['depot']
        slips = message['slips']
        vehicles = message['vehicles']
        
        coordinates = [f"{depot['lng']},{depot['lat']}"]
        index_to_node_id = {0: depot['depot_id']}
        
        weight_demands = [0]
        volume_demands = [0]
        
        for i, slip in enumerate(slips):
            index = i + 1
            coordinates.append(f"{slip['lng']},{slip['lat']}")
            index_to_node_id[index] = slip['slip_id']
            weight_demands.append(int(slip['weight']))
            volume_demands.append(int(slip['volume']))

        # 2. Gọi OSRM lấy ma trận
        distance_matrix = get_distance_matrix(coordinates)
        
        # 3. Thêm Dummy End Node cho OR-Tools
        num_nodes = len(distance_matrix)
        for row in distance_matrix: row.append(0)
        distance_matrix.append([0] * (num_nodes + 1))
        weight_demands.append(0)
        volume_demands.append(0)

        # 4. Trích xuất Vehicles
        vehicle_ids = [v['vehicle_id'] for v in vehicles]
        vehicle_max_weights = [int(v['max_weight']) for v in vehicles]
        vehicle_max_volumes = [int(v['max_volume']) for v in vehicles]
        num_vehicles = len(vehicles)

        # 5. Giải VRP bằng OR-Tools
        ortools_data = {
            'distance_matrix': distance_matrix,
            'weight_demands': weight_demands,
            'volume_demands': volume_demands,
            'vehicle_max_weights': vehicle_max_weights,
            'vehicle_max_volumes': vehicle_max_volumes,
            'num_vehicles': num_vehicles,
            'starts': [0] * num_vehicles,
            'ends': [num_nodes] * num_vehicles,
            'vehicle_ids': vehicle_ids
        }
        
        solution = solve_delivery_plan(ortools_data)
        if not solution:
            return {"plan_id": message['plan_id'], "status": "FAILED", "reason": "No solution found"}

        # 6. Format Output & Lấy Polyline cho từng chặng
        final_routes = []
        for route in solution['routes']:
            stops = []
            previous_node_index = 0 # Luôn bắt đầu từ Depot
            
            for seq, node_index in enumerate(route['stops'], start=1):
                # Lấy tọa độ điểm trước và điểm hiện tại
                start_coord = coordinates[previous_node_index]
                end_coord = coordinates[node_index]
                
                # Gọi OSRM lấy Polyline
                polyline = get_route_polyline(start_coord, end_coord)
                
                stops.append({
                    "slip_id": index_to_node_id[node_index],
                    "sequence": seq,
                    "encoded_polyline": polyline
                })
                previous_node_index = node_index # Cập nhật điểm trước cho chặng sau
                
            final_routes.append({
                "vehicle_id": route['vehicle_id'],
                "route_distance": route['distance'] / 1000.0, # Chuyển mét sang km nếu cần
                "total_weight": route['total_weight'],
                "total_volume": route['total_volume'],
                "stops": stops
            })

        # Xử lý các node bị rớt
        dropped_ids = [index_to_node_id[idx] for idx in solution['dropped_slip_ids']]

        # 7. Trả về format chuẩn
        return {
            "plan_id": message['plan_id'],
            "status": "COMPLETED",
            "total_plan_distance": solution['total_distance'] / 1000.0,
            "routes": final_routes,
            "unused_vehicle_ids": solution['unused_vehicle_ids'],
            "dropped_slip_ids": dropped_ids
        }

    except Exception as e:
        print(f"Error processing plan {message.get('plan_id')}: {e}")
        return {"plan_id": message.get('plan_id', 'UNKNOWN'), "status": "FAILED", "reason": str(e)}