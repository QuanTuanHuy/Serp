package com.example.ttcrs.algorithm.vrp.entities;

import java.util.*;
// Lớp này quản lý trọng số của các node (điểm) trong bài toán VRP. Nó sử dụng một ArrayList để lưu trữ các điểm, một mảng để lưu trữ trọng số tương ứng với mỗi điểm, và một HashMap để ánh xạ mỗi điểm đến chỉ số của nó trong mảng trọng số. Khi thêm một điểm mới, nếu mảng trọng số đã đầy, nó sẽ tự động mở rộng kích thước của mảng. Các phương thức getWeight và setWeight cho phép truy cập và cập nhật trọng số của từng điểm.
// Trọng số của node là một giá trị đại diện cho độ ưu tiên hoặc chi phí liên quan đến việc ghé thăm node đó trong quá trình giải bài toán VRP. Trọng số này có thể được sử dụng trong các thuật toán tối ưu hóa để quyết định thứ tự ghé thăm các node, nhằm giảm tổng chi phí hoặc thời gian di chuyển của các tuyến đường.
// Mục đích của việc sử dụng HashMap là để nhanh chóng tìm kiếm chỉ số của một điểm trong mảng trọng số dựa trên đối tượng Point. Điều này giúp tăng hiệu quả khi truy cập hoặc cập nhật trọng số của các điểm, đặc biệt khi số lượng điểm lớn. HashMap cung cấp thời gian truy cập trung bình O(1), trong khi nếu không sử dụng HashMap, việc tìm kiếm chỉ số của một điểm trong mảng có thể mất thời gian O(n) trong trường hợp xấu nhất.
public class NodeWeightsManager {
	protected ArrayList<Point> points;
	protected double[] weights;
	protected HashMap<Point, Integer> map;
	public NodeWeightsManager(ArrayList<Point> points){
		this.points = points;
		map = new HashMap<Point, Integer>();
		for(int i = 0; i < points.size(); i++)
			map.put(points.get(i), i);
		//weights = new double[points.size()];
		// Khởi tạo mảng trọng số với kích thước tối thiểu là 100 để tránh phải mở rộng mảng quá thường xuyên khi số lượng điểm nhỏ.
		weights = new double[points.size() < 100 ? 100 : points.size()];
	}
	// Phương thức này được sử dụng để mở rộng kích thước của mảng trọng số khi số lượng điểm vượt quá kích thước hiện tại của mảng. Nó tạo một mảng mới có kích thước gấp đôi mảng hiện tại, sao chép các giá trị từ mảng cũ sang mảng mới, và sau đó thay thế mảng cũ bằng mảng mới. Điều này giúp đảm bảo rằng mảng trọng số luôn đủ lớn để chứa trọng số cho tất cả các điểm mà không cần phải mở rộng quá thường xuyên, điều này có thể ảnh hưởng đến hiệu suất của chương trình.
	private void scaleUp(){
		double[] t_w = new double[2*weights.length];
		System.arraycopy(weights, 0, t_w, 0, weights.length);
		weights = t_w;
	}
	public void addPoint(Point p){
		if(weights.length == points.size()) scaleUp();
		points.add(p);
		map.put(p, points.size()-1);
	}

	public double getWeight(Point p){
		return weights[map.get(p)];
	}
	public void setWeight(Point p, double w){
		weights[map.get(p)] = w;
	}
	public ArrayList<Point> getPoints(){
		return this.points;
	}
	
	public String name(){
		return "NodeWeightManager";
	}
	public void print(){
		for(int i = 0; i < points.size(); i++){
			System.out.println(name() + "::NodeWeightManager::print, point " + points.get(i).ID);
		}
	}
}


