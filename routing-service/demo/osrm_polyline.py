import requests

def get_route_polyline(start_coordinates, end_coordinates) -> str:
    """
    start_coordinates: Tọa độ điểm bắt đầu.
    end_coordinates: Tọa độ điểm kết thúc.
    """
    coord_string = ";".join([start_coordinates, end_coordinates])
    
    # Dùng endpoint /route thay vì /table
    # Thêm overview=full để lấy độ chi tiết cao nhất (bám sát đường nhựa)
    url = f"http://localhost:5000/route/v1/driving/{coord_string}?overview=full"
    
    response = requests.get(url)
    data = response.json()
    
    if data['code'] == 'Ok':
        # Đây chính là chuỗi encoded_polyline mà bạn cần ném về cho Java
        return data['routes'][0]['geometry'] 
    return None

if __name__ == "__main__":
    start = "106.660172,10.762622"  # Ví dụ: Tọa độ Sài Gòn
    end = "106.682172,10.762622"    # Ví dụ: Một điểm khác ở Sài Gòn
    polyline = get_route_polyline(start, end)
    print(f"Encoded Polyline: {polyline}")