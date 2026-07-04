package com.example.ttcrs.algorithm.solver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.Gson;

import com.example.ttcrs.algorithm.models.equipments.*;
import com.example.ttcrs.algorithm.models.input.ContainerTruckMoocInput;
import com.example.ttcrs.algorithm.models.input.DistanceElement;
import com.example.ttcrs.algorithm.models.places.*;
import com.example.ttcrs.algorithm.models.requests.*;

public class DataMapper {
    
    private ContainerTruckMoocInput input;
    
    // Maps and data structures
    public String[] locationCodes;
    public HashMap<String, Integer> mLocationCode2Index;
    public double[][] distance;
    public double[][] travelTime;
    
    public HashMap<String, Truck> mCode2Truck;
    public HashMap<String, Mooc> mCode2Mooc;
    public HashMap<String, Container> mCode2Container;
    public HashMap<String, DepotContainer> mCode2DepotContainer;
    public HashMap<String, DepotTruck> mCode2DepotTruck;
    public HashMap<String, DepotMooc> mCode2DepotMooc;
    public HashMap<String, Warehouse> mCode2Warehouse;
    public HashMap<String, Port> mCode2Port;
    public ArrayList<Container> additionalContainers;
    
    public DataMapper() {
        // Constructor
    }
    
    /**
     * Reads and parses input data from JSON file and populates solver fields.
     * This method directly fills all solver properties, eliminating the need for loadData().
     * 
     * @param fileName Path to the input JSON file
     * @param solver The TruckContainerSolver to populate with data
     * @return The parsed ContainerTruckMoocInput object
     */
    public ContainerTruckMoocInput readData(String fileName, TruckContainerSolver solver) {
        try {
            Gson g = new Gson();
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            input = g.fromJson(in, ContainerTruckMoocInput.class);
            in.close();
            
            InputAnalyzer IA = new InputAnalyzer();
            IA.standardize(input);
            
            mapData(fileName);
            
            // Directly populate solver fields
            solver.input = input;
            solver.locationCodes = this.locationCodes;
            solver.mLocationCode2Index = this.mLocationCode2Index;
            solver.distance = this.distance;
            solver.travelTime = this.travelTime;
            solver.mCode2Truck = this.mCode2Truck;
            solver.mCode2Mooc = this.mCode2Mooc;
            solver.mCode2Container = this.mCode2Container;
            solver.mCode2DepotContainer = this.mCode2DepotContainer;
            solver.mCode2DepotTruck = this.mCode2DepotTruck;
            solver.mCode2DepotMooc = this.mCode2DepotMooc;
            solver.mCode2Warehouse = this.mCode2Warehouse;
            solver.mCode2Port = this.mCode2Port;
            solver.additionalContainers = this.additionalContainers;
            
            return input;
        } catch (Exception e) {
            System.out.println("Error reading data: " + e);
            return null;
        }
    }
    
    private void mapData(String dataFileName) {
        processContainers();
        processLocationCodes();
        processDistanceAndTravelTime();
        processDepotContainers();
        processDepotMoocs();
        processDepotTrucks();
        processWarehouses();
        processMoocs();
        processTrucks();
        processContainerMapping();
        processPorts();
    }
    // Làm 3 việc: 1: Tạo container từ IE request, 2: Lọc container trùng lặp trong input, 3: Cập nhật lại danh sách container
    private void processContainers() {
        additionalContainers = new ArrayList<Container>();
        int idxCode = -1;
        // Tạo thêm các container ảo cho các yêu cầu nhập rỗng nếu chúng chưa tồn tại trong danh sách container ban đầu
        if (input.getImEmptyRequests() != null) {
            for (int i = 0; i < input.getImEmptyRequests().length; i++) {
                ImportEmptyRequests R = input.getImEmptyRequests()[i];
                idxCode++;
                String code = "A-" + idxCode;
                String depotContainerCode = null;
                
                if (R.getDepotContainerCode() != null)
                    depotContainerCode = R.getDepotContainerCode();
                else {
                    // Gán depotContainerCode mặc định nếu không có thông tin trong yêu cầu
                    int idx = 0;
                    depotContainerCode = input.getDepotContainers()[idx].getCode();
                }
                
                String[] returnDepot = new String[1];
                returnDepot[0] = depotContainerCode;
                
                Container c = new Container(code, (int) R.getWeight(),
                        R.getContainerCategory(), depotContainerCode, returnDepot);
                additionalContainers.add(c);
                R.setContainerCode(code);
            }
        }
        
        ArrayList<String> containerCodes = new ArrayList<String>();
        Container[] temp = input.getContainers();
        ArrayList<Container> cL = new ArrayList<Container>();
        // Loại bỏ các container trùng lặp trong danh sách container ban đầu
        for (int i = 0; i < temp.length; i++) {
            if (!containerCodes.contains(temp[i].getCode())) {
                containerCodes.add(temp[i].getCode());
                cL.add(temp[i]);
            }
        }
        
        Container[] L = new Container[cL.size() + additionalContainers.size()];
        for (int i = 0; i < cL.size(); i++) {
            L[i] = cL.get(i);
            L[i].setImportedContainer(false);
        }
        for (int i = 0; i < additionalContainers.size(); i++) {
            L[i + cL.size()] = additionalContainers.get(i);
            L[i + cL.size()].setImportedContainer(true);
        }
        // Cập nhật lại danh sách container trong input sau khi đã loại bỏ trùng lặp và thêm các container ảo
        input.setContainers(L);
    }
    // Làm 2 việc: 1: Tạo list location code từ distance matrix, 2: Tạo mapping location code -> index
    private void processLocationCodes() {
        HashSet<String> s_locationCode = new HashSet<String>();
        for (int i = 0; i < input.getDistance().length; i++) {
            DistanceElement e = input.getDistance()[i];
            //HashSet đảm bảo rằng mỗi location code chỉ được thêm một lần duy nhất
            s_locationCode.add(e.getSrcCode());
            s_locationCode.add(e.getDestCode());
        }
        
        locationCodes = new String[s_locationCode.size()];
        mLocationCode2Index = new HashMap<String, Integer>();
        int idx = -1;
        for (String lc : s_locationCode) {
            idx++;
            locationCodes[idx] = lc;
            mLocationCode2Index.put(lc, idx); // Tạo mapping từ location code sang index để dễ dàng truy cập trong ma trận khoảng cách và thời gian di chuyển
        }
    }
    
    private void processDistanceAndTravelTime() {
        int size = locationCodes.length;
        distance = new double[size][size];
        travelTime = new double[size][size];
        
        for (int i = 0; i < input.getDistance().length; i++) {
            DistanceElement e = input.getDistance()[i];
            int is = mLocationCode2Index.get(e.getSrcCode()); 
            int id = mLocationCode2Index.get(e.getDestCode());
            distance[is][id] = e.getDistance();
            travelTime[is][id] = e.getTravelTime();
        }
    }
    // Làm 2 việc: 1: Loại bỏ các depot container trùng lặp trong danh sách depot container ban đầu, 2: Tạo mapping code -> depot container
    private void processDepotContainers() {
        ArrayList<DepotContainer> dcL = new ArrayList<DepotContainer>();
        ArrayList<String> codes = new ArrayList<String>();
        // Loại bỏ các depot container trùng lặp trong danh sách depot container ban đầu
        for (int i = 0; i < input.getDepotContainers().length; i++) {
            if (!codes.contains(input.getDepotContainers()[i].getCode())) {
                codes.add(input.getDepotContainers()[i].getCode());
                dcL.add(input.getDepotContainers()[i]);
            }
        }
        // Chuyển từ ArrayList sang mảng DepotContainer
        DepotContainer[] dpc = new DepotContainer[dcL.size()];
        for (int i = 0; i < dcL.size(); i++)
            dpc[i] = dcL.get(i);
        input.setDepotContainers(dpc);
        
        mCode2DepotContainer = new HashMap<String, DepotContainer>();
        for (int i = 0; i < input.getDepotContainers().length; i++) {
            mCode2DepotContainer.put(input.getDepotContainers()[i].getCode(),
                    input.getDepotContainers()[i]);
        }
    }
    // Làm 2 việc: 1: Loại bỏ các depot mooc trùng lặp trong danh sách depot mooc ban đầu, 2: Tạo mapping code -> depot mooc
    private void processDepotMoocs() {
        ArrayList<DepotMooc> depotMoocList = new ArrayList<DepotMooc>();
        ArrayList<String> codes = new ArrayList<String>();
        
        for (int i = 0; i < input.getDepotMoocs().length; i++) {
            if (!codes.contains(input.getDepotMoocs()[i].getCode())) {
                codes.add(input.getDepotMoocs()[i].getCode());
                depotMoocList.add(input.getDepotMoocs()[i]);
            }
        }
        
        DepotMooc[] dpm = new DepotMooc[depotMoocList.size()];
        for (int i = 0; i < depotMoocList.size(); i++)
            dpm[i] = depotMoocList.get(i);
        input.setDepotMoocs(dpm);
        
        mCode2DepotMooc = new HashMap<String, DepotMooc>();
        for (int i = 0; i < input.getDepotMoocs().length; i++) {
            mCode2DepotMooc.put(input.getDepotMoocs()[i].getCode(),
                    input.getDepotMoocs()[i]);
        }
    }
    // Làm 2 việc: 1: Loại bỏ các depot truck trùng lặp trong danh sách depot truck ban đầu, 2: Tạo mapping code -> depot truck
    private void processDepotTrucks() {
        ArrayList<DepotTruck> depotTruckList = new ArrayList<DepotTruck>();
        ArrayList<String> codes = new ArrayList<String>();
        
        for (int i = 0; i < input.getDepotTrucks().length; i++) {
            if (!codes.contains(input.getDepotTrucks()[i].getCode())) {
                codes.add(input.getDepotTrucks()[i].getCode());
                depotTruckList.add(input.getDepotTrucks()[i]);
            }
        }
        
        DepotTruck[] dpt = new DepotTruck[depotTruckList.size()];
        for (int i = 0; i < depotTruckList.size(); i++)
            dpt[i] = depotTruckList.get(i);
        input.setDepotTrucks(dpt);
        
        mCode2DepotTruck = new HashMap<String, DepotTruck>();
        for (int i = 0; i < input.getDepotTrucks().length; i++) {
            mCode2DepotTruck.put(input.getDepotTrucks()[i].getCode(),
                    input.getDepotTrucks()[i]);
        }
    }
    // Làm 2 việc: 1: Loại bỏ các warehouse trùng lặp trong danh sách warehouse ban đầu, 2: Tạo mapping code -> warehouse
    private void processWarehouses() {
        ArrayList<Warehouse> whList = new ArrayList<Warehouse>();
        ArrayList<String> codes = new ArrayList<String>();
        
        for (int i = 0; i < input.getWarehouses().length; i++) {
            if (!codes.contains(input.getWarehouses()[i].getCode())) {
                codes.add(input.getWarehouses()[i].getCode());
                whList.add(input.getWarehouses()[i]);
            }
        }
        
        Warehouse[] whs = new Warehouse[whList.size()];
        for (int i = 0; i < whList.size(); i++)
            whs[i] = whList.get(i);
        input.setWarehouses(whs);
        
        mCode2Warehouse = new HashMap<String, Warehouse>();
        for (int i = 0; i < input.getWarehouses().length; i++) {
            mCode2Warehouse.put(input.getWarehouses()[i].getCode(),
                    input.getWarehouses()[i]);
        }
    }
    // Làm 2 việc: 1: Loại bỏ các mooc trùng lặp trong danh sách mooc ban đầu, 2: Tạo mapping code -> mooc
    private void processMoocs() {
        ArrayList<Mooc> moocList = new ArrayList<Mooc>();
        ArrayList<String> codes = new ArrayList<String>();
        
        for (int i = 0; i < input.getMoocs().length; i++) {
            if (!codes.contains(input.getMoocs()[i].getCode())) {
                codes.add(input.getMoocs()[i].getCode());
                moocList.add(input.getMoocs()[i]);
            }
        }
        
        Mooc[] ms = new Mooc[moocList.size()];
        for (int i = 0; i < moocList.size(); i++)
            ms[i] = moocList.get(i);
        input.setMoocs(ms);
        
        mCode2Mooc = new HashMap<String, Mooc>();
        for (int i = 0; i < input.getMoocs().length; i++) {
            Mooc mooc = input.getMoocs()[i];
            mCode2Mooc.put(mooc.getCode(), mooc);
        }
    }
    // Làm 2 việc: 1: Loại bỏ các truck trùng lặp trong danh sách truck ban đầu, 2: Tạo mapping code -> truck
    private void processTrucks() {
        ArrayList<Truck> truckList = new ArrayList<Truck>();
        ArrayList<String> codes = new ArrayList<String>();
        
        for (int i = 0; i < input.getTrucks().length; i++) {
            if (!codes.contains(input.getTrucks()[i].getCode())) {
                codes.add(input.getTrucks()[i].getCode());
                truckList.add(input.getTrucks()[i]);
            }
        }
        
        Truck[] ts = new Truck[truckList.size()];
        for (int i = 0; i < truckList.size(); i++)
            ts[i] = truckList.get(i);
        input.setTrucks(ts);
        
        mCode2Truck = new HashMap<String, Truck>();
        for (int i = 0; i < input.getTrucks().length; i++) {
            Truck truck = input.getTrucks()[i];
            mCode2Truck.put(truck.getCode(), truck);
        }
    }
    // Tạo mapping code -> container để dễ dàng truy cập thông tin container theo mã container trong quá trình xây dựng mô hình và giải quyết bài toán
    private void processContainerMapping() {
        mCode2Container = new HashMap<String, Container>();
        for (int i = 0; i < input.getContainers().length; i++) {
            Container c = input.getContainers()[i];
            mCode2Container.put(c.getCode(), c);
        }
    }
    // Làm 2 việc: 1: Loại bỏ các port trùng lặp trong danh sách port ban đầu, 2: Tạo mapping code -> port
    private void processPorts() {
        ArrayList<Port> portList = new ArrayList<Port>();
        ArrayList<String> codes = new ArrayList<String>();
        
        for (int i = 0; i < input.getPorts().length; i++) {
            if (!codes.contains(input.getPorts()[i].getCode())) {
                codes.add(input.getPorts()[i].getCode());
                portList.add(input.getPorts()[i]);
            }
        }
        
        Port[] ps = new Port[portList.size()];
        for (int i = 0; i < portList.size(); i++)
            ps[i] = portList.get(i);
        input.setPorts(ps);
        
        mCode2Port = new HashMap<String, Port>();
        for (int i = 0; i < input.getPorts().length; i++) {
            mCode2Port.put(input.getPorts()[i].getCode(), input.getPorts()[i]);
        }
    }
    
    // Getters for all processed data
    public ContainerTruckMoocInput getInput() {
        return input;
    }
}

