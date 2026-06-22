package com.example.ttcrs.algorithm.vrp;

import com.example.ttcrs.algorithm.vrp.entities.Point;
// Interface này định nghĩa một phương thức để lấy khoảng cách giữa hai điểm. Nó có thể được triển khai bằng cách sử dụng các thuật toán khác nhau (ví dụ: khoảng cách Euclidean, khoảng cách Manhattan, hoặc khoảng cách dựa trên bản đồ).
public interface IDistanceManager {
	public double getDistance(Point x, Point y);
}


