package com.example.ttcrs.algorithm.solver;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.ttcrs.algorithm.models.equipments.Container;
import com.example.ttcrs.algorithm.models.equipments.Mooc;
import com.example.ttcrs.algorithm.models.equipments.Truck;
import com.example.ttcrs.algorithm.models.places.DepotContainer;
import com.example.ttcrs.algorithm.models.places.DepotMooc;
import com.example.ttcrs.algorithm.models.places.DepotTruck;
import com.example.ttcrs.algorithm.models.places.Port;
import com.example.ttcrs.algorithm.models.places.Warehouse;
import com.example.ttcrs.algorithm.models.requests.ExportEmptyRequests;
import com.example.ttcrs.algorithm.models.requests.ExportLadenRequests;
import com.example.ttcrs.algorithm.models.requests.ImportEmptyRequests;
import com.example.ttcrs.algorithm.models.requests.ImportLadenRequests;
import com.example.ttcrs.algorithm.vrp.entities.ArcWeightsManager;
import com.example.ttcrs.algorithm.vrp.entities.NodeWeightsManager;
import com.example.ttcrs.algorithm.vrp.entities.Point;
import com.example.ttcrs.algorithm.vrp.utils.DateTimeUtils;

public class TruckContainerInitializer {

	public void init(TruckContainerSolver solver) {
		loadRequestsAndCounts(solver);
		initCollections(solver);

		int id = 0;
		int groupId = 0;

		IdAndGroup cursor = new IdAndGroup(id, groupId);
		cursor = buildTruckPoints(solver, cursor);
		cursor = buildMoocPoints(solver, cursor);
		cursor = buildExportEmptyRequestPoints(solver, cursor);
		cursor = buildExportLadenRequestPoints(solver, cursor);
		cursor = buildImportEmptyRequestPoints(solver, cursor);
		cursor = buildImportLadenRequestPoints(solver, cursor);

		buildWeightManagersAndMaxTravelTime(solver);
	}

	private void loadRequestsAndCounts(TruckContainerSolver solver) {
		solver.exEmptyRequests = solver.input.getExEmptyRequests();
		solver.exLadenRequests = solver.input.getExLadenRequests();
		solver.imEmptyRequests = solver.input.getImEmptyRequests();
		solver.imLadenRequests = solver.input.getImLadenRequests();

		solver.nRequest = solver.exEmptyRequests.length + solver.exLadenRequests.length + solver.imEmptyRequests.length
				+ solver.imLadenRequests.length;
		solver.nVehicle = solver.input.getTrucks().length;
	}

	private void initCollections(TruckContainerSolver solver) {
		solver.points = new ArrayList<Point>();
		solver.earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		solver.serviceDuration = new HashMap<Point, Integer>();
		solver.lastestAllowedArrivalTime = new HashMap<Point, Integer>();

		solver.pickupPoints = new ArrayList<Point>();
		solver.deliveryPoints = new ArrayList<Point>();
		solver.rejectPickupPoints = new ArrayList<Point>();
		solver.rejectDeliveryPoints = new ArrayList<Point>();
		solver.startPoints = new ArrayList<Point>();
		solver.stopPoints = new ArrayList<Point>();
		solver.startMoocPoints = new ArrayList<Point>();
		solver.stopMoocPoints = new ArrayList<Point>();
		solver.point2Type = new HashMap<Point, String>();

		solver.pickup2Delivery = new HashMap<Point, Point>();
		solver.delivery2Pickup = new HashMap<Point, Point>();

		solver.start2stopMoocPoint = new HashMap<Point, Point>();
		solver.stop2startMoocPoint = new HashMap<Point, Point>();

		solver.startPoint2Truck = new HashMap<Point, Truck>();
		solver.startPoint2Mooc = new HashMap<Point, Mooc>();

		solver.point2Group = new HashMap<Point, Integer>();
		solver.group2marked = new HashMap<Integer, Integer>();

		solver.group2EE = new HashMap<Integer, ExportEmptyRequests>();
		solver.group2EL = new HashMap<Integer, ExportLadenRequests>();
		solver.group2IE = new HashMap<Integer, ImportEmptyRequests>();
		solver.group2IL = new HashMap<Integer, ImportLadenRequests>();

		solver.point2moocWeight = new HashMap<Point, Integer>();
		solver.point2containerWeight = new HashMap<Point, Integer>();

		solver.route2DeliveryMooc = new HashMap<Integer, Point>();
	}

	private IdAndGroup buildTruckPoints(TruckContainerSolver solver, IdAndGroup cursor) {
		int id = cursor.id;
		int groupId = cursor.groupId;

		for (int i = 0; i < solver.nVehicle; i++) {
			Truck truck = solver.input.getTrucks()[i];
			groupId++;
			solver.group2marked.put(groupId, 0);
			for (int j = 0; j < truck.getReturnDepotCodes().length; j++) {
				id++;
				Point sp = new Point(id, truck.getDepotTruckLocationCode());

				solver.points.add(sp);
				solver.startPoints.add(sp);
				solver.point2Type.put(sp, TruckContainerSolver.START_TRUCK);
				solver.startPoint2Truck.put(sp, truck);

				solver.point2Group.put(sp, groupId);

				solver.earliestAllowedArrivalTime.put(sp,
						(int) (DateTimeUtils.dateTime2Int(truck.getStartWorkingTime())));
				solver.serviceDuration.put(sp, 0);
				solver.lastestAllowedArrivalTime.put(sp, solver.INF_TIME);

				id++;
				DepotTruck depotTruck = solver.mCode2DepotTruck.get(truck.getReturnDepotCodes()[j]);
				Point tp = new Point(id, depotTruck.getLocationCode());
				solver.points.add(tp);
				solver.stopPoints.add(tp);
				solver.point2Type.put(tp, TruckContainerSolver.END_TRUCK);

				solver.point2Group.put(tp, groupId);

				solver.earliestAllowedArrivalTime.put(tp,
						(int) (DateTimeUtils.dateTime2Int(solver.input.getTrucks()[i].getStartWorkingTime())));
				solver.serviceDuration.put(tp, 0);
				solver.lastestAllowedArrivalTime.put(tp, solver.INF_TIME);

				solver.point2moocWeight.put(sp, 0);
				solver.point2moocWeight.put(tp, 0);

				solver.point2containerWeight.put(sp, 0);
				solver.point2containerWeight.put(tp, 0);
			}
		}

		return new IdAndGroup(id, groupId);
	}

	private IdAndGroup buildMoocPoints(TruckContainerSolver solver, IdAndGroup cursor) {
		int id = cursor.id;
		int groupId = cursor.groupId;

		for (int i = 0; i < solver.input.getMoocs().length; i++) {
			Mooc mooc = solver.input.getMoocs()[i];
			groupId++;
			solver.group2marked.put(groupId, 0);
			for (int j = 0; j < mooc.getReturnDepotCodes().length; j++) {
				id++;
				Point sp = new Point(id, mooc.getDepotMoocLocationCode());
				solver.points.add(sp);
				solver.startMoocPoints.add(sp);
				solver.point2Type.put(sp, TruckContainerSolver.START_MOOC);
				solver.startPoint2Mooc.put(sp, mooc);

				solver.point2Group.put(sp, groupId);

				solver.earliestAllowedArrivalTime.put(sp, 0);
				solver.serviceDuration.put(sp, solver.input.getParams().getLinkMoocDuration());
				solver.lastestAllowedArrivalTime.put(sp, solver.INF_TIME);

				id++;
				String moocCode = mooc.getReturnDepotCodes()[j];
				DepotMooc depotMooc = solver.mCode2DepotMooc.get(moocCode);
				Point tp = new Point(id, depotMooc.getLocationCode());
				solver.points.add(tp);
				solver.stopMoocPoints.add(tp);
				solver.point2Type.put(tp, TruckContainerSolver.END_MOOC);
				solver.point2Group.put(tp, groupId);

				solver.earliestAllowedArrivalTime.put(tp, 0);
				solver.serviceDuration.put(tp, 0);
				solver.lastestAllowedArrivalTime.put(tp, solver.INF_TIME);

				solver.start2stopMoocPoint.put(sp, tp);
				solver.stop2startMoocPoint.put(tp, sp);

				solver.point2moocWeight.put(sp, 2);
				solver.point2moocWeight.put(tp, -2);

				solver.point2containerWeight.put(sp, 0);
				solver.point2containerWeight.put(tp, 0);
			}
		}

		return new IdAndGroup(id, groupId);
	}

	private IdAndGroup buildExportEmptyRequestPoints(TruckContainerSolver solver, IdAndGroup cursor) {
		int id = cursor.id;
		int groupId = cursor.groupId;

		for (int i = 0; i < solver.exEmptyRequests.length; i++) {
			groupId++;
			solver.group2marked.put(groupId, 0);
			solver.group2EE.put(groupId, solver.exEmptyRequests[i]);
			for (int j = 0; j < solver.input.getContainers().length; j++) {
				Container c = solver.input.getContainers()[j];
				if (c.isImportedContainer())
					continue;

				// kept for parity with original implementation
				DepotContainer depotCont = solver.mCode2DepotContainer.get(c.getDepotContainerCode());
				id++;
				Point pickup = new Point(id, c.getDepotContainerCode());
				id++;
				Warehouse wh = solver.mCode2Warehouse.get(solver.exEmptyRequests[i].getWareHouseCode());
				Point delivery = new Point(id, wh.getLocationCode());

				solver.points.add(pickup);
				solver.points.add(delivery);

				solver.pickupPoints.add(pickup);
				solver.deliveryPoints.add(delivery);

				solver.pickup2Delivery.put(pickup, delivery);
				solver.delivery2Pickup.put(delivery, pickup);

				solver.point2moocWeight.put(pickup, 0);
				if (solver.exEmptyRequests[i].getIsBreakRomooc())
					solver.point2moocWeight.put(delivery, -2);
				else
					solver.point2moocWeight.put(delivery, 0);

				solver.point2containerWeight.put(pickup, 1);
				solver.point2containerWeight.put(delivery, -1);
				if (solver.exEmptyRequests[i].getContainerType() != null
						&& solver.exEmptyRequests[i].getContainerType().equals("40")) {
					solver.point2containerWeight.put(pickup, 2);
					solver.point2containerWeight.put(delivery, -2);
				}

				solver.point2Type.put(pickup, TruckContainerSolver.START_CONT);
				solver.point2Type.put(delivery, TruckContainerSolver.WH_DELIVERY_EMPTYCONT);

				solver.point2Group.put(pickup, groupId);
				solver.point2Group.put(delivery, groupId);

				int early = 0;
				int latest = solver.INF_TIME;
				if (solver.exEmptyRequests[i].getEarlyDateTimePickupAtDepot() != null)
					early = (int) (DateTimeUtils.dateTime2Int(solver.exEmptyRequests[i].getEarlyDateTimePickupAtDepot()));
				if (solver.exEmptyRequests[i].getLateDateTimePickupAtDepot() != null)
					latest = (int) (DateTimeUtils.dateTime2Int(solver.exEmptyRequests[i].getLateDateTimePickupAtDepot()));
				solver.earliestAllowedArrivalTime.put(pickup, early);
				solver.serviceDuration.put(pickup, solver.input.getParams().getLinkEmptyContainerDuration());
				solver.lastestAllowedArrivalTime.put(pickup, latest);

				early = 0;
				latest = solver.INF_TIME;
				if (solver.exEmptyRequests[i].getEarlyDateTimeLoadAtWarehouse() != null)
					early = (int) (DateTimeUtils.dateTime2Int(solver.exEmptyRequests[i].getEarlyDateTimeLoadAtWarehouse()));
				if (solver.exEmptyRequests[i].getLateDateTimeLoadAtWarehouse() != null)
					latest = (int) (DateTimeUtils.dateTime2Int(solver.exEmptyRequests[i].getLateDateTimeLoadAtWarehouse()));
				solver.earliestAllowedArrivalTime.put(delivery, early);
				solver.serviceDuration.put(delivery, (int) (solver.input.getParams().getUnlinkEmptyContainerDuration()));
				solver.lastestAllowedArrivalTime.put(delivery, latest);
			}
		}

		return new IdAndGroup(id, groupId);
	}

	private IdAndGroup buildExportLadenRequestPoints(TruckContainerSolver solver, IdAndGroup cursor) {
		int id = cursor.id;
		int groupId = cursor.groupId;

		for (int i = 0; i < solver.exLadenRequests.length; i++) {
			groupId++;
			solver.group2marked.put(groupId, 0);
			solver.group2EL.put(groupId, solver.exLadenRequests[i]);
			id++;
			Warehouse wh = solver.mCode2Warehouse.get(solver.exLadenRequests[i].getWareHouseCode());
			Point pickup = new Point(id, wh.getLocationCode());
			id++;
			Port port = solver.mCode2Port.get(solver.exLadenRequests[i].getPortCode());
			Point delivery = new Point(id, port.getLocationCode());

			solver.points.add(pickup);
			solver.points.add(delivery);

			solver.pickupPoints.add(pickup);
			solver.deliveryPoints.add(delivery);

			solver.pickup2Delivery.put(pickup, delivery);
			solver.delivery2Pickup.put(delivery, pickup);

			solver.point2Type.put(pickup, TruckContainerSolver.WH_PICKUP_FULLCONT);
			solver.point2Type.put(delivery, TruckContainerSolver.PORT_DELIVERY_FULLCONT);

			solver.point2Group.put(pickup, groupId);
			solver.point2Group.put(delivery, groupId);

			solver.point2moocWeight.put(pickup, 0);
			if (solver.exLadenRequests[i].getIsBreakRomooc())
				solver.point2moocWeight.put(delivery, -2);
			else
				solver.point2moocWeight.put(delivery, 0);

			solver.point2containerWeight.put(pickup, 1);
			solver.point2containerWeight.put(delivery, -1);
			if (solver.exLadenRequests[i].getContainerType() != null
					&& solver.exLadenRequests[i].getContainerType().equals("40")) {
				solver.point2containerWeight.put(pickup, 2);
				solver.point2containerWeight.put(delivery, -2);
			}

			int early = 0;
			int latest = solver.INF_TIME;
			if (solver.exLadenRequests[i].getEarlyDateTimeAttachAtWarehouse() != null)
				early = (int) (DateTimeUtils.dateTime2Int(solver.exLadenRequests[i].getEarlyDateTimeAttachAtWarehouse()));

			solver.earliestAllowedArrivalTime.put(pickup, early);
			solver.serviceDuration.put(pickup, solver.input.getParams().getLinkLoadedContainerDuration());
			solver.lastestAllowedArrivalTime.put(pickup, latest);

			early = 0;
			latest = solver.INF_TIME;
			if (solver.exLadenRequests[i].getLateDateTimeUnloadAtPort() != null)
				latest = (int) (DateTimeUtils.dateTime2Int(solver.exLadenRequests[i].getLateDateTimeUnloadAtPort()));
			solver.earliestAllowedArrivalTime.put(delivery, early);
			solver.serviceDuration.put(delivery, (int) (solver.input.getParams().getUnlinkLoadedContainerDuration()));
			solver.lastestAllowedArrivalTime.put(delivery, latest);
		}

		return new IdAndGroup(id, groupId);
	}

	private IdAndGroup buildImportEmptyRequestPoints(TruckContainerSolver solver, IdAndGroup cursor) {
		int id = cursor.id;
		int groupId = cursor.groupId;

		for (int i = 0; i < solver.imEmptyRequests.length; i++) {
			groupId++;
			solver.group2marked.put(groupId, 0);
			solver.group2IE.put(groupId, solver.imEmptyRequests[i]);
			for (int j = 0; j < solver.input.getDepotContainers().length; j++) {
				DepotContainer depotCont = solver.input.getDepotContainers()[j];
				id++;
				Warehouse wh = solver.mCode2Warehouse.get(solver.imEmptyRequests[i].getWareHouseCode());
				Point pickup = new Point(id, wh.getLocationCode());
				id++;

				Point delivery = new Point(id, depotCont.getLocationCode());

				solver.points.add(pickup);
				solver.points.add(delivery);

				solver.pickupPoints.add(pickup);
				solver.deliveryPoints.add(delivery);

				solver.pickup2Delivery.put(pickup, delivery);
				solver.delivery2Pickup.put(delivery, pickup);

				solver.point2moocWeight.put(pickup, 0);
				solver.point2moocWeight.put(delivery, 0);

				solver.point2containerWeight.put(pickup, 1);
				solver.point2containerWeight.put(delivery, -1);
				if (solver.imEmptyRequests[i].getContainerType() != null
						&& solver.imEmptyRequests[i].getContainerType().equals("40")) {
					solver.point2containerWeight.put(pickup, 2);
					solver.point2containerWeight.put(delivery, -2);
				}

				solver.point2Type.put(pickup, TruckContainerSolver.WH_PICKUP_EMPTYCONT);
				solver.point2Type.put(delivery, TruckContainerSolver.END_CONT);

				solver.point2Group.put(pickup, groupId);
				solver.point2Group.put(delivery, groupId);

				int early = 0;
				int latest = solver.INF_TIME;
				if (solver.imEmptyRequests[i].getEarlyDateTimeAttachAtWarehouse() != null)
					early = (int) (DateTimeUtils.dateTime2Int(solver.imEmptyRequests[i].getEarlyDateTimeAttachAtWarehouse()));
				solver.earliestAllowedArrivalTime.put(pickup, early);
				solver.serviceDuration.put(pickup, solver.input.getParams().getLinkEmptyContainerDuration());
				solver.lastestAllowedArrivalTime.put(pickup, latest);

				early = 0;
				latest = solver.INF_TIME;

				if (solver.imEmptyRequests[i].getLateDateTimeReturnEmptyAtDepot() != null)
					latest = (int) (DateTimeUtils.dateTime2Int(solver.imEmptyRequests[i].getLateDateTimeReturnEmptyAtDepot()));
				solver.earliestAllowedArrivalTime.put(delivery, early);
				solver.serviceDuration.put(delivery, (int) (solver.input.getParams().getUnlinkEmptyContainerDuration()));
				solver.lastestAllowedArrivalTime.put(delivery, latest);
			}
		}

		return new IdAndGroup(id, groupId);
	}

	private IdAndGroup buildImportLadenRequestPoints(TruckContainerSolver solver, IdAndGroup cursor) {
		int id = cursor.id;
		int groupId = cursor.groupId;

		for (int i = 0; i < solver.imLadenRequests.length; i++) {
			groupId++;
			solver.group2marked.put(groupId, 0);
			solver.group2IL.put(groupId, solver.imLadenRequests[i]);
			id++;
			Port port = solver.mCode2Port.get(solver.imLadenRequests[i].getPortCode());
			Point pickup = new Point(id, port.getLocationCode());

			id++;
			Warehouse wh = solver.mCode2Warehouse.get(solver.imLadenRequests[i].getWareHouseCode());
			Point delivery = new Point(id, wh.getLocationCode());

			solver.points.add(pickup);
			solver.points.add(delivery);

			solver.pickupPoints.add(pickup);
			solver.deliveryPoints.add(delivery);

			solver.pickup2Delivery.put(pickup, delivery);
			solver.delivery2Pickup.put(delivery, pickup);

			solver.point2moocWeight.put(pickup, 0);
			if (solver.imLadenRequests[i].getIsBreakRomooc())
				solver.point2moocWeight.put(delivery, -2);
			else
				solver.point2moocWeight.put(delivery, 0);

			solver.point2containerWeight.put(pickup, 1);
			solver.point2containerWeight.put(delivery, -1);
			if (solver.imLadenRequests[i].getContainerType() != null
					&& solver.imLadenRequests[i].getContainerType().equals("40")) {
				solver.point2containerWeight.put(pickup, 2);
				solver.point2containerWeight.put(delivery, -2);
			}

			solver.point2Type.put(pickup, TruckContainerSolver.PORT_PICKUP_FULLCONT);
			solver.point2Type.put(delivery, TruckContainerSolver.WH_DELIVERY_FULLCONT);

			solver.point2Group.put(pickup, groupId);
			solver.point2Group.put(delivery, groupId);

			int early = 0;
			int latest = solver.INF_TIME;
			if (solver.imLadenRequests[i].getEarlyDateTimePickupAtPort() != null)
				early = (int) (DateTimeUtils.dateTime2Int(solver.imLadenRequests[i].getEarlyDateTimePickupAtPort()));
			if (solver.imLadenRequests[i].getLateDateTimePickupAtPort() != null)
				latest = (int) (DateTimeUtils.dateTime2Int(solver.imLadenRequests[i].getLateDateTimePickupAtPort()));
			solver.earliestAllowedArrivalTime.put(pickup, early);
			solver.serviceDuration.put(pickup, solver.input.getParams().getLinkLoadedContainerDuration());
			solver.lastestAllowedArrivalTime.put(pickup, latest);

			early = 0;
			latest = solver.INF_TIME;
			if (solver.imLadenRequests[i].getEarlyDateTimeUnloadAtWarehouse() != null)
				early = (int) (DateTimeUtils.dateTime2Int(solver.imLadenRequests[i].getEarlyDateTimeUnloadAtWarehouse()));
			if (solver.imLadenRequests[i].getLateDateTimeUnloadAtWarehouse() != null)
				latest = (int) (DateTimeUtils.dateTime2Int(solver.imLadenRequests[i].getLateDateTimeUnloadAtWarehouse()));

			solver.earliestAllowedArrivalTime.put(delivery, early);
			solver.serviceDuration.put(delivery, (int) (solver.input.getParams().getUnlinkLoadedContainerDuration()));
			solver.lastestAllowedArrivalTime.put(delivery, latest);
		}

		return new IdAndGroup(id, groupId);
	}

	private void buildWeightManagersAndMaxTravelTime(TruckContainerSolver solver) {
		solver.nwMooc = new NodeWeightsManager(solver.points);
		solver.nwContainer = new NodeWeightsManager(solver.points);
		solver.awm = new ArcWeightsManager(solver.points);
		double max_time = Double.MIN_VALUE;
		for (int i = 0; i < solver.points.size(); i++) {
			for (int j = 0; j < solver.points.size(); j++) {
				double tmp_cost = solver.getTravelTime(solver.points.get(i).getLocationCode(),
						solver.points.get(j).getLocationCode());
				solver.awm.setWeight(solver.points.get(i), solver.points.get(j), tmp_cost);
				max_time = tmp_cost > max_time ? tmp_cost : max_time;
			}
			solver.nwMooc.setWeight(solver.points.get(i), solver.point2moocWeight.get(solver.points.get(i)));
			solver.nwContainer.setWeight(solver.points.get(i), solver.point2containerWeight.get(solver.points.get(i)));
		}
		TruckContainerSolver.MAX_TRAVELTIME = max_time;
	}

	private static final class IdAndGroup {
		final int id;
		final int groupId;

		IdAndGroup(int id, int groupId) {
			this.id = id;
			this.groupId = groupId;
		}
	}
}

