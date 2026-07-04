package com.example.ttcrs.algorithm.models.input;

public class DistanceElement {
	private String srcCode;
	private String destCode;
	private boolean isDriverBalance; // chỉ ra xem khoảng cách này có liên quan đến việc cân bằng lái xe hay không, có thể được sử dụng để xác định xem khoảng cách này có ảnh hưởng đến việc phân bổ lái xe hay không.
	private int[] drivers;
	private double distance;
	private double travelTime;
	private double d;
	private double t;
	
	
	public DistanceElement(String srcCode, String destCode, 
			boolean isDriverBalance,
			int[] drivers,
			double distance,
			double travelTime,
			double d,
			double t) {
		super();
		this.srcCode = srcCode;
		this.destCode = destCode;
		this.isDriverBalance = isDriverBalance;
		this.drivers = drivers;
		this.distance = distance;
		this.travelTime = travelTime;
		this.d = d;
		this.t = t;
	}
	public double getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(double travelTime) {
		this.travelTime = travelTime;
	}
	public String getSrcCode() {
		return srcCode;
	}
	public void setSrcCode(String srcCode) {
		this.srcCode = srcCode;
	}
	public String getDestCode() {
		return destCode;
	}
	public void setDestCode(String destCode) {
		this.destCode = destCode;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public boolean getIsDriverBalance(){
		return isDriverBalance;
	}
	public void setDriverBalance(boolean isDriverBalance) {
		this.isDriverBalance = isDriverBalance;
	}
	public void setIsDriverBalance(boolean isDriverBalance){
		this.isDriverBalance = isDriverBalance;
	}
	public int[] getDrivers(){
		return this.drivers;
	}
	public void setDrivers(int[] drivers){
		this.drivers = drivers;
	}
	public double getD() {
		return d;
	}
	public void setD(double d) {
		this.d = d;
	}
	public double getT() {
		return t;
	}
	public void setT(double t) {
		this.t = t;
	}
	public DistanceElement(String srcCode, String destCode, double distance) {
		super();
		this.srcCode = srcCode;
		this.destCode = destCode;
		this.distance = distance;
	}
	public DistanceElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}

