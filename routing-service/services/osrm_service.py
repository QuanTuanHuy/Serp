import requests
import logging
from config import OSRM_BASE_URL

logger = logging.getLogger(__name__)

def get_distance_matrix(coordinates: list[str]) -> list[list[int]]:
    """Gọi OSRM lấy ma trận khoảng cách giữa các tọa độ (lng,lat)"""
    coord_string = ";".join(coordinates)
    url = f"{OSRM_BASE_URL}/table/v1/driving/{coord_string}?annotations=distance"
    
    response = requests.get(url, timeout=10)
    response.raise_for_status()
    data = response.json()
    
    if data.get('code') != 'Ok':
        raise Exception(f"OSRM Error: {data.get('message')}")
        
    # Chuyển đổi float thành int cho OR-Tools
    matrix = data['distances']
    return [[int(val) for val in row] for row in matrix]

def get_route_polyline(start_coord: str, end_coord: str) -> str:
    """Gọi OSRM lấy chuỗi polyline chi tiết giữa 2 điểm"""
    coord_string = f"{start_coord};{end_coord}"
    url = f"{OSRM_BASE_URL}/route/v1/driving/{coord_string}?overview=full"
    
    response = requests.get(url, timeout=5)
    if response.status_code == 200:
        data = response.json()
        if data.get('code') == 'Ok':
            return data['routes'][0]['geometry']
    logger.error("Failed to get route polyline from OSRM")
    return ""

def get_full_route_polyline(coordinates: list[str]) -> str:
    """
    Nhận vào mảng các chuỗi tọa độ ['lng1,lat1', 'lng2,lat2', ...]
    Gọi OSRM 1 lần duy nhất để lấy tổng hợp Polyline cho cả lộ trình.
    """
    coord_string = ";".join(coordinates)
    url = f"{OSRM_BASE_URL}/route/v1/driving/{coord_string}?overview=full"
    
    try:
        response = requests.get(url, timeout=10) # Tăng timeout lên 10s vì request dài
        if response.status_code == 200:
            data = response.json()
            if data.get('code') == 'Ok':
                return data['routes'][0]['geometry']
    except Exception as e:
        logger.error(f"Lỗi khi lấy full polyline từ OSRM: {e}")
    return ""