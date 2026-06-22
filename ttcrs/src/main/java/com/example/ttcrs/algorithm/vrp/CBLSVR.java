package com.example.ttcrs.algorithm.vrp;

import com.example.ttcrs.algorithm.vrp.entities.Point;
// Lớp này chứa các hằng số và phương thức tiện ích được sử dụng trong thuật toán CBLSVR (Constraint-Based Local Search for Vehicle Routing). Các hằng số bao gồm MAX_INT, EPSILON, và NULL_POINT, được sử dụng để đại diện cho giá trị lớn nhất của một số nguyên, một giá trị rất nhỏ để so sánh các số thực, và một điểm null để đại diện cho việc không có điểm nào. Phương thức equal được sử dụng để so sánh hai số thực với độ chính xác nhất định, tránh các vấn đề liên quan đến độ chính xác của số thực trong Java.
public class CBLSVR {
	public static final int MAX_INT = 2147483647;
	public static final double EPSILON = 0.0000000001;
	public static final Point NULL_POINT = new Point(-1);
	public static boolean equal(double a, double b){
		return Math.abs(a-b) < EPSILON;
	}
}


