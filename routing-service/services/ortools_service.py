from ortools.constraint_solver import routing_enums_pb2
from ortools.constraint_solver import pywrapcp
from config import ORTOOLS_TIME_LIMIT_SEC

def solve_delivery_plan(data: dict) -> dict:
    manager = pywrapcp.RoutingIndexManager(
        len(data['distance_matrix']),
        data['num_vehicles'],
        data['starts'],
        data['ends']
    )
    routing = pywrapcp.RoutingModel(manager)

    # 1. Khoảng cách
    def distance_callback(from_index, to_index):
        return data['distance_matrix'][manager.IndexToNode(from_index)][manager.IndexToNode(to_index)]
    transit_callback_index = routing.RegisterTransitCallback(distance_callback)
    routing.SetArcCostEvaluatorOfAllVehicles(transit_callback_index)

    # 2. Khối lượng
    def weight_callback(from_index):
        return data['weight_demands'][manager.IndexToNode(from_index)]
    weight_callback_index = routing.RegisterUnaryTransitCallback(weight_callback)
    routing.AddDimensionWithVehicleCapacity(
        weight_callback_index, 0, data['vehicle_max_weights'], True, 'Weight'
    )

    # 3. Thể tích
    def volume_callback(from_index):
        return data['volume_demands'][manager.IndexToNode(from_index)]
    volume_callback_index = routing.RegisterUnaryTransitCallback(volume_callback)
    routing.AddDimensionWithVehicleCapacity(
        volume_callback_index, 0, data['vehicle_max_volumes'], True, 'Volume'
    )

    # 4. Phạt rớt đơn
    penalty = 1000000 
    for node in range(1, len(data['distance_matrix']) - 1): 
        routing.AddDisjunction([manager.NodeToIndex(node)], penalty)

    # 5. Cấu hình chạy
    search_parameters = pywrapcp.DefaultRoutingSearchParameters()
    search_parameters.first_solution_strategy = routing_enums_pb2.FirstSolutionStrategy.PATH_CHEAPEST_ARC
    search_parameters.local_search_metaheuristic = routing_enums_pb2.LocalSearchMetaheuristic.GUIDED_LOCAL_SEARCH
    search_parameters.time_limit.seconds = ORTOOLS_TIME_LIMIT_SEC

    solution = routing.SolveWithParameters(search_parameters)
    if not solution:
        return None

    # 6. Parse kết quả
    result = {"total_distance": 0, "routes": [], "unused_vehicle_ids": [], "dropped_slip_ids": []}

    for node in range(1, len(data['distance_matrix']) - 1):
        if solution.Value(routing.NextVar(manager.NodeToIndex(node))) == manager.NodeToIndex(node):
            result["dropped_slip_ids"].append(node)

    for vehicle_id in range(data['num_vehicles']):
        index = routing.Start(vehicle_id)
        next_index = solution.Value(routing.NextVar(index))
        
        if routing.IsEnd(next_index):
            result["unused_vehicle_ids"].append(data['vehicle_ids'][vehicle_id])
            continue

        route_nodes = []
        route_distance = 0
        route_load_weight = 0
        route_load_volume = 0

        while not routing.IsEnd(index):
            node_index = manager.IndexToNode(index)
            if node_index != 0: 
                route_nodes.append(node_index)
            
            route_load_weight += data['weight_demands'][node_index]
            route_load_volume += data['volume_demands'][node_index]
            
            previous_index = index
            index = solution.Value(routing.NextVar(index))
            route_distance += routing.GetArcCostForVehicle(previous_index, index, vehicle_id)

        result["routes"].append({
            "vehicle_id": data['vehicle_ids'][vehicle_id],
            "distance": route_distance,
            "total_weight": route_load_weight,
            "total_volume": route_load_volume,
            "stops": route_nodes 
        })
        result["total_distance"] += route_distance

    return result