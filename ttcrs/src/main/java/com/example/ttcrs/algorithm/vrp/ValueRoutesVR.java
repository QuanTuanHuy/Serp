package com.example.ttcrs.algorithm.vrp;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.ttcrs.algorithm.vrp.entities.Point;
// Bản sao giá trị để lưu trạng thái hiện tại của các tuyến đường, được sử dụng để so sánh với trạng thái mới sau khi thực hiện các thao tác như di chuyển điểm từ tuyến đường này sang tuyến đường khác hoặc đảo ngược hướng của một đoạn tuyến đường.
public class ValueRoutesVR {
	private HashMap<Point, Point> next;
	private HashMap<Point, Point> prev;
	private HashMap<Point, Integer> route;
	private VarRoutesVR XR;
	private ArrayList<Point> allPoints;
	
	public ValueRoutesVR(VarRoutesVR XR){
		this.XR = XR;
		this.next = new HashMap<Point, Point>();
		this.prev = new HashMap<Point, Point>();
		this.route = new HashMap<Point, Integer>();
		
		allPoints = XR.getAllPoints();
		for(Point p : allPoints){
			next.put(p, XR.next(p));
			prev.put(p, XR.prev(p));
			route.put(p, XR.route(p));
		}
	}
	public Point next(Point p){
		return next.get(p);
	}
	public Point prev(Point p){
		return prev.get(p);
	}
	public int route(Point p){
		return route.get(p);
	}
	// Stores the current values of the routes
	public void store(){
		for(Point p : allPoints){
			next.put(p, XR.next(p));
			prev.put(p, XR.prev(p));
			route.put(p, XR.route(p));
		}
	}
	public String toString() {
		String s = "";
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			s += "route[" + k + "] = ";
			Point x = XR.getStartingPointOfRoute(k);
			while(x != XR.getTerminatingPointOfRoute(k)){
				s = s + x.getID() + " " + " -> ";
				x = next.get(x);
			}
			s = s + x.getID() + "\n";
		}
		return s;
	}
}


