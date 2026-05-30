package com.example.ttcrs.algorithm.vrp.entities;

import java.util.ArrayList;

import com.example.ttcrs.algorithm.vrp.CBLSVR;
// Lớp này đại diện cho một tập hợp các giá trị đa mục tiêu
// (multi-objective values) được sử dụng trong các thuật toán 
// tối ưu hóa đa mục tiêu. Nó chứa một ArrayList để lưu trữ các
// giá trị, và cung cấp các phương thức để thực hiện các phép 
// toán như cộng hai tập giá trị, so sánh hai tập giá trị theo 
// thứ tự từ điển (lexicographical order), và kiểm tra sự bằng 
// nhau giữa hai tập giá trị. 
// Các phương thức lt, leq, và eq được sử dụng để so sánh các 
// tập giá trị, trong đó lt kiểm tra xem một tập giá trị có 
// nhỏ hơn tập giá trị khác hay không, leq kiểm tra xem một tập 
// giá trị có nhỏ hơn hoặc bằng tập giá trị khác hay không, và 
// eq kiểm tra xem hai tập giá trị có bằng nhau hay không. 
public class LexMultiValues {
	private ArrayList<Double> values;
	public LexMultiValues(){
		values = new ArrayList<Double>();
	}
	public LexMultiValues(LexMultiValues V){
		values = new ArrayList<Double>();
		for(int i = 0; i < V.size(); i++)
			values.add(V.get(i));
	}
	public LexMultiValues(ArrayList<Double> values){
		this.values = values;
	}
	public LexMultiValues(double v){
		values = new ArrayList<Double>();
		values.add(v);
	}
	public LexMultiValues(double v1, double v2){
		values = new ArrayList<Double>();
		values.add(v1);
		values.add(v2);
	}
	public void fill(int sz, double v){
		values.clear();
		for(int i = 0; i < sz; i++)
			values.add(v);
	}
	public int size(){
		return values.size();
	}
	public void clear(){
		values.clear();
	}
	public void add(double v){
		values.add(v);
	}
	public double get(int i){
		return values.get(i);
	}
	public LexMultiValues plus(LexMultiValues mv){
		ArrayList<Double> A = new ArrayList<Double>();
		for(int i = 0; i < size(); i++)
			A.add(get(i) + mv.get(i));
		return new LexMultiValues(A);
	}
	// So sánh 2 LexMultiValues theo thứ tự từ điển (lexicographical order). Phương thức này sẽ trả về true nếu đối tượng hiện tại nhỏ hơn đối tượng V, và false nếu ngược lại. So sánh được thực hiện bằng cách duyệt qua từng giá trị trong hai đối tượng, so sánh chúng với nhau. Nếu có một cặp giá trị nào đó không bằng nhau, phương thức sẽ trả về kết quả của phép so sánh giữa hai giá trị đó. Nếu tất cả các cặp giá trị đều bằng nhau, phương thức sẽ trả về false, vì trong trường hợp này hai đối tượng được coi là bằng nhau chứ không phải là một cái nhỏ hơn cái kia.
	public boolean lt(LexMultiValues V){
		for(int i = 0; i < values.size(); i++){
			double x = values.get(i);
			double y = V.get(i);
			if (!CBLSVR.equal(x, y)) {
				return x < y; 
			}
		}
		return false;
	}
	public boolean lt(double v){
		for(int i = 0; i < values.size(); i++){
			double x = values.get(i);
			if (!CBLSVR.equal(x, v)) {
				return x < v; 
			}
		}
		return false;
	}
	
	public boolean leq(LexMultiValues V){
		for(int i = 0; i < values.size(); i++){
			double x = values.get(i);
			double y = V.get(i);
			if (!CBLSVR.equal(x, y)) {
				return x < y; 
			}
		}
		return true;
	}
	
	public boolean eq(LexMultiValues V){
		for(int i = 0; i < values.size(); i++){
			double x = values.get(i);
			double y = V.get(i);
			if (!CBLSVR.equal(x, y)) {
				return false; 
			}
		}
		return true;
	}
	public void set(LexMultiValues v){
		values.clear();
		for(int i = 0; i < v.size(); i++){
			values.add(v.get(i));
		}
	}
	public String toString(){
		String s = "";
		for(int i = 0; i < values.size(); i++)
			s = s + values.get(i) + ", ";
		return s;
	}
}

