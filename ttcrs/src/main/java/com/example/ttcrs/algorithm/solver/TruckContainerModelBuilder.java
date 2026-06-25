package com.example.ttcrs.algorithm.solver;

import com.example.ttcrs.algorithm.constraints.ContainerCapacityConstraint;
import com.example.ttcrs.algorithm.constraints.ContainerCarriedByTrailerConstraint;
import com.example.ttcrs.algorithm.constraints.MoocCapacityConstraint;
import com.example.ttcrs.algorithm.vrp.AccumulatedWeightNodesVR;
import com.example.ttcrs.algorithm.vrp.ConstraintSystemVR;
import com.example.ttcrs.algorithm.vrp.VRManager;
import com.example.ttcrs.algorithm.vrp.VarRoutesVR;
import com.example.ttcrs.algorithm.vrp.entities.LexMultiValues;
import com.example.ttcrs.algorithm.vrp.entities.Point;
import com.example.ttcrs.algorithm.vrp.functions.TotalCostVR;
import com.example.ttcrs.algorithm.vrp.invariants.EarliestArrivalTimeVR;
import com.example.ttcrs.algorithm.vrp.constraints.CEarliestArrivalTimeVR;

public class TruckContainerModelBuilder {
	public void build(TruckContainerSolver solver) {
		createCoreVrpObjects(solver); // Tạo các đối tượng cốt lõi của VRP
		registerRoutes(solver); // Đăng ký các tuyến đường (start-stop) vào mô hình
		registerClientPoints(solver); // Đăng ký các điểm khách hàng vào mô hình
		buildTimeWindows(solver); // Xây dựng cửa sổ thời gian
		buildAccumulators(solver); // Xây dựng các bộ tích lũy
		buildConstraintsAndObjective(solver); // Xây dựng các ràng buộc và hàm mục tiêu
	}

	private void createCoreVrpObjects(TruckContainerSolver solver) {
		solver.mgr = new VRManager();
		solver.XR = new VarRoutesVR(solver.mgr);
		solver.S = new ConstraintSystemVR(solver.mgr);
	}

	private void registerRoutes(TruckContainerSolver solver) {
		for (int i = 0; i < solver.startPoints.size(); ++i)
			solver.XR.addRoute(solver.startPoints.get(i), solver.stopPoints.get(i));
	}

	private void registerClientPoints(TruckContainerSolver solver) {
		for (int i = 0; i < solver.pickupPoints.size(); ++i) {
			Point pickup = solver.pickupPoints.get(i);
			Point delivery = solver.deliveryPoints.get(i);
			solver.XR.addClientPoint(pickup);
			solver.XR.addClientPoint(delivery);
		}
		for (int i = 0; i < solver.startMoocPoints.size(); ++i) {
			solver.XR.addClientPoint(solver.startMoocPoints.get(i));
			solver.XR.addClientPoint(solver.stopMoocPoints.get(i));
		}
	}
	
	private void buildTimeWindows(TruckContainerSolver solver) {
		//tính toán/propagator (cập nhật giá trị) giá trị ETA khi route thay đổi. EAT tại nút i = max(earliestAllowed[i], EAT(prev) + travelTime(prev,i) + serviceDuration(prev)).
		solver.eat = new EarliestArrivalTimeVR(solver.XR, solver.awm, solver.earliestAllowedArrivalTime,
				solver.serviceDuration);
		//constraint để đảm bảo ETA của mỗi điểm không vượt quá thời gian cho phép (kiểm tra xem EAT <= latestAllowedArrivalTime hay không.)
		solver.cEarliest = new CEarliestArrivalTimeVR(solver.eat, solver.lastestAllowedArrivalTime);
	}

	private void buildAccumulators(TruckContainerSolver solver) {
		solver.accMoocInvr = new AccumulatedWeightNodesVR(solver.XR, solver.nwMooc);
		solver.accContainerInvr = new AccumulatedWeightNodesVR(solver.XR, solver.nwContainer);
	}

	private void buildConstraintsAndObjective(TruckContainerSolver solver) {
		solver.capContCtr = new ContainerCapacityConstraint(solver.XR, solver.accContainerInvr); //constraint để đảm bảo rằng trọng lượng tích lũy của container trên mỗi tuyến đường không vượt quá sức chứa tối đa của container.
		solver.capMoocCtr = new MoocCapacityConstraint(solver.XR, solver.accMoocInvr); //constraint để đảm bảo rằng trọng lượng tích lũy của mooc trên mỗi tuyến đường không vượt quá sức chứa tối đa của mooc.
		solver.contmoocCtr = new ContainerCarriedByTrailerConstraint(solver.XR, solver.accContainerInvr,
				solver.accMoocInvr); //constraint để đảm bảo rằng container chỉ có thể được vận chuyển nếu nó được chở bởi một mooc (trailer). Điều này có nghĩa là nếu một container đang được vận chuyển trên một tuyến đường, thì phải có một mooc tương ứng trên cùng tuyến đường đó để chở container đó.

		solver.S.post(solver.cEarliest); //đăng ký constraint CEarliestArrivalTimeVR vào hệ thống ràng buộc để đảm bảo rằng các ràng buộc về thời gian được áp dụng trong quá trình giải quyết bài toán.
		solver.S.post(solver.capContCtr); //tương tự
		solver.S.post(solver.capMoocCtr); //tương tự
		solver.S.post(solver.contmoocCtr); //tương tự
		solver.objective = new TotalCostVR(solver.XR, solver.awm); //hàm mục tiêu để tính tổng chi phí của các tuyến đường, dựa trên trọng số khoảng cách giữa các điểm.
		solver.valueSolution = new LexMultiValues();
		solver.valueSolution.add(solver.S.violations());
		solver.valueSolution.add(solver.objective.getValue());
		// Bảng giá trị LexMultiValues trước hết là violations rồi tới cost (tức là ưu tiên tối thiểu hóa violations trước, sau đó mới tối thiểu hóa cost trong số các giải pháp có cùng số violations).
		solver.mgr.close();
	}
}

