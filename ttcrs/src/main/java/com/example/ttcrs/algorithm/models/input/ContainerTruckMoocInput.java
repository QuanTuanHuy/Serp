package com.example.ttcrs.algorithm.models.input;

import com.example.ttcrs.algorithm.models.places.*;
import com.example.ttcrs.algorithm.models.requests.*;
import com.example.ttcrs.algorithm.models.equipments.*;;


public class ContainerTruckMoocInput {
	private ExportContainerTruckMoocRequest[] exRequests;
	private ImportContainerTruckMoocRequest[] imRequests;
	private WarehouseTransportRequest[] warehouseRequests;
	
	private ExportEmptyRequests[] exEmptyRequests;
	private ExportLadenRequests[] exLadenRequests;
	private ImportEmptyRequests[] imEmptyRequests;
	private ImportLadenRequests[] imLadenRequests;
	
	
	private ShipCompany[] companies;
	private DepotContainer[] depotContainers;
	private DepotMooc[] depotMoocs;
	private DepotTruck[] depotTrucks;
	private Warehouse[] warehouses;
	private Truck[] trucks;
	private Mooc[] moocs;
	private MoocGroup[] moocGroup;
	private Port[] ports;
	
	private Container[] containers;
	
	DistanceElement[] distance;
	DistanceElement[] travelTime;

	private ConfigParam params;
	

	
	

	public ContainerTruckMoocInput(
			ExportContainerTruckMoocRequest[] exRequests,
			ImportContainerTruckMoocRequest[] imRequests,
			WarehouseTransportRequest[] warehouseRequests,
			ExportEmptyRequests[] exEmptyRequests,
			ExportLadenRequests[] exLadenRequests,
			ImportEmptyRequests[] imEmptyRequests,
			ImportLadenRequests[] imLadenRequests, ShipCompany[] companies,
			DepotContainer[] depotContainers, DepotMooc[] depotMoocs,
			DepotTruck[] depotTrucks, Warehouse[] warehouses, Truck[] trucks,
			Mooc[] moocs, MoocGroup[] moocGroup, Port[] ports, Container[] containers,
			DistanceElement[] distance, DistanceElement[] travelTime,
			ConfigParam params) {
		super();
		this.exRequests = exRequests;
		this.imRequests = imRequests;
		this.warehouseRequests = warehouseRequests;
		this.exEmptyRequests = exEmptyRequests;
		this.exLadenRequests = exLadenRequests;
		this.imEmptyRequests = imEmptyRequests;
		this.imLadenRequests = imLadenRequests;
		this.companies = companies;
		this.depotContainers = depotContainers;
		this.depotMoocs = depotMoocs;
		this.depotTrucks = depotTrucks;
		this.warehouses = warehouses;
		this.trucks = trucks;
		this.moocs = moocs;
		this.moocGroup = moocGroup;
		this.ports = ports;
		this.containers = containers;
		this.distance = distance;
		this.travelTime = travelTime;
		this.params = params;
	}


	public ExportEmptyRequests[] getExEmptyRequests() {
		return exEmptyRequests;
	}


	public void setExEmptyRequests(ExportEmptyRequests[] exEmptyRequests) {
		this.exEmptyRequests = exEmptyRequests;
	}


	public ExportLadenRequests[] getExLadenRequests() {
		return exLadenRequests;
	}


	public void setExLadenRequests(ExportLadenRequests[] exLadenRequests) {
		this.exLadenRequests = exLadenRequests;
	}


	public ImportEmptyRequests[] getImEmptyRequests() {
		return imEmptyRequests;
	}


	public void setImEmptyRequests(ImportEmptyRequests[] imEmptyRequests) {
		this.imEmptyRequests = imEmptyRequests;
	}


	public ImportLadenRequests[] getImLadenRequests() {
		return imLadenRequests;
	}


	public void setImLadenRequests(ImportLadenRequests[] imLadenRequests) {
		this.imLadenRequests = imLadenRequests;
	}

	public Port[] getPorts() {
		return ports;
	}


	public void setPorts(Port[] ports) {
		this.ports = ports;
	}








	public ContainerTruckMoocInput(
			ExportContainerTruckMoocRequest[] exRequests,
			ImportContainerTruckMoocRequest[] imRequests,
			WarehouseTransportRequest[] warehouseRequests,
			ShipCompany[] companies, DepotContainer[] depotContainers,
			DepotMooc[] depotMoocs, DepotTruck[] depotTrucks,
			Warehouse[] warehouses, Truck[] trucks, Mooc[] moocs, MoocGroup[] moocGroup,
			Port[] ports, Container[] containers, DistanceElement[] distance,
			DistanceElement[] travelTime, ConfigParam params) {
		super();
		this.exRequests = exRequests;
		this.imRequests = imRequests;
		this.warehouseRequests = warehouseRequests;
		this.companies = companies;
		this.depotContainers = depotContainers;
		this.depotMoocs = depotMoocs;
		this.depotTrucks = depotTrucks;
		this.warehouses = warehouses;
		this.trucks = trucks;
		this.moocs = moocs;
		this.moocGroup = moocGroup;
		this.ports = ports;
		this.containers = containers;
		this.distance = distance;
		this.travelTime = travelTime;
		this.params = params;
	}


	public Warehouse[] getWarehouses() {
		return warehouses;
	}


	public void setWarehouses(Warehouse[] warehouses) {
		this.warehouses = warehouses;
	}


	public ExportContainerTruckMoocRequest[] getExRequests() {
		return exRequests;
	}


	public void setExRequests(ExportContainerTruckMoocRequest[] exRequests) {
		this.exRequests = exRequests;
	}


	public ImportContainerTruckMoocRequest[] getImRequests() {
		return imRequests;
	}


	public void setImRequests(ImportContainerTruckMoocRequest[] imRequests) {
		this.imRequests = imRequests;
	}


	public WarehouseTransportRequest[] getWarehouseRequests() {
		return warehouseRequests;
	}


	public void setWarehouseRequests(WarehouseTransportRequest[] warehouseRequests) {
		this.warehouseRequests = warehouseRequests;
	}


	public ShipCompany[] getCompanies() {
		return companies;
	}


	public void setCompanies(ShipCompany[] companies) {
		this.companies = companies;
	}


	public DepotContainer[] getDepotContainers() {
		return depotContainers;
	}


	public void setDepotContainers(DepotContainer[] depotContainers) {
		this.depotContainers = depotContainers;
	}


	public DepotMooc[] getDepotMoocs() {
		return depotMoocs;
	}


	public void setDepotMoocs(DepotMooc[] depotMoocs) {
		this.depotMoocs = depotMoocs;
	}


	public DepotTruck[] getDepotTrucks() {
		return depotTrucks;
	}


	public void setDepotTrucks(DepotTruck[] depotTrucks) {
		this.depotTrucks = depotTrucks;
	}


	public Truck[] getTrucks() {
		return trucks;
	}


	public void setTrucks(Truck[] trucks) {
		this.trucks = trucks;
	}


	public Mooc[] getMoocs() {
		return moocs;
	}


	public void setMoocs(Mooc[] moocs) {
		this.moocs = moocs;
	}

	public MoocGroup[] getMoocGroup(){
		return this.moocGroup;
	}
	
	public void setMoocGroup(MoocGroup[] moocGroup){
		this.moocGroup = moocGroup;
	}

	public Container[] getContainers() {
		return containers;
	}


	public void setContainers(Container[] containers) {
		this.containers = containers;
	}


	public DistanceElement[] getDistance() {
		return distance;
	}


	public void setDistance(DistanceElement[] distance) {
		this.distance = distance;
	}


	public DistanceElement[] getTravelTime() {
		return travelTime;
	}


	public void setTravelTime(DistanceElement[] travelTime) {
		this.travelTime = travelTime;
	}


	public ConfigParam getParams() {
		return params;
	}


	public void setParams(ConfigParam params) {
		this.params = params;
	}


	public ContainerTruckMoocInput() {
		super();
		// TODO Auto-generated constructor stub
	}
    
}

