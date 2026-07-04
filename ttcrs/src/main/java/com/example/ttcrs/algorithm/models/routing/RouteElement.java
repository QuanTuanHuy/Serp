package com.example.ttcrs.algorithm.models.routing;

//Route Element là một stop trên route. Nó chứa thông tin về mã địa điểm, hành động (lấy hàng, giao hàng), thời gian đến, thời gian rời đi, thời gian di chuyển và ID yêu cầu (nếu có).
public class RouteElement {
    private String locationCode;
	private String action;
	
	private String arrivalTime;
	private String departureTime;
	private int travelTime;
	private Long requestId;  // null for depot/mooc stops; DB entity ID for operational stops
	private String containerCode; // container code for PICKUP_CONTAINER stops (OE requests)

	public RouteElement(String locationCode, String action,
			String arrivalTime, String departureTime, int travelTime){
		super();
		this.locationCode = locationCode;
		this.action = action;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.travelTime = travelTime;
	}

	public RouteElement(String locationCode, String action,
			String arrivalTime, String departureTime, int travelTime, Long requestId){
		this(locationCode, action, arrivalTime, departureTime, travelTime);
		this.requestId = requestId;
	}

	public RouteElement(String locationCode, String action,
			String arrivalTime, String departureTime, int travelTime,
			Long requestId, String containerCode){
		this(locationCode, action, arrivalTime, departureTime, travelTime, requestId);
		this.containerCode = containerCode;
	}
	public RouteElement() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getLocationCode(){
		return this.locationCode;
	}
	public void setLocationCode(String locationCode){
		this.locationCode = locationCode;
	}
	public String getAction(){
		return this.action;
	}
	public void setAction(String action){
		this.action = action;
	}
	
	public String getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public String getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}
	public int getTravelTime() {
		return travelTime;
	}
	public void setTravelTime(int travelTime) {
		this.travelTime = travelTime;
	}
	public Long getRequestId() {
		return requestId;
	}
	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}
	public String getContainerCode() {
		return containerCode;
	}
	public void setContainerCode(String containerCode) {
		this.containerCode = containerCode;
	}
}

