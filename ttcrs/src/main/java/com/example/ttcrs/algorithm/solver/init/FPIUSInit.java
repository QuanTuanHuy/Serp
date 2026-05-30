package com.example.ttcrs.algorithm.solver.init;

import java.util.Stack;

import com.example.ttcrs.algorithm.solver.TruckContainerSolver;
import com.example.ttcrs.algorithm.vrp.Constants;
import com.example.ttcrs.algorithm.vrp.entities.Point;

/**
 * FPIUS (First Possible Insertion with Unscheduled Set) initialization strategy.
 * Implements the InitializationStrategy interface.
 */
public class FPIUSInit implements InitializationStrategy {
	
	@Override
	public void initialize(TruckContainerSolver solver) {
		firstPossibleInitFPIUS(solver);
	}
	
	public void firstPossibleInitFPIUS(TruckContainerSolver solver) {
		Stack<String> stack = new Stack<String>();
		for (int r = solver.XR.getNbRoutes(); r >= 1; r--) {
			String s = "" + r;
			stack.push(s);
		}

		for (int i = 0; i < solver.pickup2Delivery.size(); i++) {
			System.out.println("req " + i + "/" + solver.pickup2Delivery.size());
			Point pickup = solver.pickupPoints.get(i);
			int groupId = solver.point2Group.get(pickup);
			if (solver.XR.route(pickup) != Constants.NULL_POINT || solver.group2marked.get(groupId) == 1)
				continue;
			Point delivery = solver.deliveryPoints.get(i);
			// add the request to route
			boolean isAdded = false;
			for (int k = stack.size() - 1; k >= 0; k--) {
				if (isAdded)
					break;
				int r = Integer.parseInt(stack.get(k)); // Lấy route r từ stack, bắt đầu từ route cuối cùng (route có số lớn nhất) để thử chèn yêu cầu vào đó. Việc duyệt ngược từ cuối stack giúp ưu tiên chèn vào các route mới hơn, có thể có nhiều chỗ trống hơn để chứa yêu cầu mới.
				Point st = solver.XR.getStartingPointOfRoute(r);

				int groupTruck = solver.point2Group.get(st);
				// Nếu nhóm xe đã bị đánh dấu và route gần như rỗng hoặc quá ngắn, thì bỏ qua route này.
				if (solver.group2marked.get(groupTruck) == 1 && solver.XR.index(solver.XR.getTerminatingPointOfRoute(r)) <= 1)
					continue;
				// Duyệt mọi vị trí có thể đặt pickup sau điểm p.
				for (Point p = st; p != solver.XR.getTerminatingPointOfRoute(r); p = solver.XR.next(p)) {
					if (isAdded)
						break;
					// Thử chèn pickup sau điểm p và delivery sau pickup, sau đó kiểm tra xem ràng buộc có bị vi phạm không. Nếu không vi phạm, đánh dấu nhóm của xe và yêu cầu là đã được thêm vào, đồng thời cập nhật stack để phản ánh việc chèn thành công này. Nếu vi phạm, hoàn tác việc chèn và tiếp tục thử các vị trí khác.
					for (Point q = p; q != solver.XR.getTerminatingPointOfRoute(r); q = solver.XR.next(q)) {
						solver.mgr.performAddTwoPoints(pickup, p, delivery, q);
						insertMoocToRoutes(solver, r);
						if (solver.S.violations() == 0) {
							solver.group2marked.put(groupTruck, 1);
							solver.group2marked.put(groupId, 1);
							stack.remove(stack.get(k));
							String s = "" + r;
							stack.push(s);
							isAdded = true;
							removeMoocOnRoutes(solver, r);
							break;
						}
						solver.mgr.performRemoveTwoPoints(pickup, delivery);
						removeMoocOnRoutes(solver, r);
					}
				}
			}
		}
		// Sau khi cố gắng chèn tất cả các yêu cầu vào các route hiện có, kiểm tra lại tất cả các điểm pickup để đảm bảo rằng nếu có điểm pickup nào vẫn chưa được chèn vào route nào và không bị từ chối, thì sẽ bị từ chối cùng với điểm delivery tương ứng của nó. Điều này đảm bảo rằng tất cả các yêu cầu không thể được chèn vào bất kỳ route nào sẽ được xử lý một cách nhất quán bằng cách đánh dấu chúng là bị từ chối.
		for (int i = 0; i < solver.pickup2Delivery.size(); i++) {
			Point pickup = solver.pickupPoints.get(i);
			if (solver.XR.route(pickup) == Constants.NULL_POINT && !solver.rejectPickupPoints.contains(pickup)) {
				solver.rejectPickupPoints.add(pickup);
				solver.rejectDeliveryPoints.add(solver.pickup2Delivery.get(pickup));
			}
		}
		// Cuối cùng, sau khi tất cả các yêu cầu đã được xử lý, chèn mooc vào tất cả các route để đảm bảo rằng các ràng buộc về việc container phải được chở bởi một mooc được
		insertMoocForAllRoutes(solver);
	}
	// Chọn mooc tốt nhất để chèn vào trước điểm pickup trên route r, dựa trên tổng thời gian di chuyển từ điểm trước pickup đến điểm pickup thông qua mooc đó. Chỉ xem xét các mooc chưa được sử dụng (nhóm của mooc chưa bị đánh dấu và mooc đó không có route nào đang sử dụng). Nếu tìm thấy mooc phù hợp, trả về điểm bắt đầu của mooc đó; nếu không tìm thấy, trả về null.
	private Point getBestStartMoocForRequest(TruckContainerSolver solver, int r, Point p, Point pickup) {
		Point bestMooc = null;
		double min_d = Double.MAX_VALUE;
		for (int i = 0; i < solver.startMoocPoints.size(); i++) {
			Point stMooc = solver.startMoocPoints.get(i);
			int groupMooc = solver.point2Group.get(stMooc);
			if (solver.group2marked.get(groupMooc) == 1 || solver.XR.route(stMooc) != Constants.NULL_POINT)
				continue;
			double d = solver.getTravelTime(p.getLocationCode(), stMooc.getLocationCode())
					+ solver.getTravelTime(stMooc.getLocationCode(), pickup.getLocationCode());
			if (d < min_d) {
				min_d = d;
				bestMooc = stMooc;
			}
		}
		return bestMooc;
	}

	public void insertMoocToRoutes(TruckContainerSolver solver, int r) {
		Point st = solver.XR.getStartingPointOfRoute(r);
		Point stMooc = null;
		Point enMooc = null;
		for (Point p = solver.XR.next(st); p != solver.XR.getTerminatingPointOfRoute(r); p = solver.XR.next(p)) {
			// Nếu tại điểm p trên route r, tổng trọng lượng mooc tích lũy trước đó (tức là tại điểm trước p) bằng 0 hoặc âm, điều đó có nghĩa là không có mooc nào đang được sử dụng để chở hàng tại thời điểm đó. Trong trường hợp này, cần phải chèn một mooc mới vào trước điểm p để đảm bảo rằng các container có thể được vận chuyển nếu cần thiết. Việc chèn mooc này sẽ giúp đảm bảo rằng các ràng buộc về việc container phải được chở bởi một mooc.
			if (solver.accMoocInvr.getSumWeights(solver.XR.prev(p)) <= 0) {
				stMooc = getBestStartMoocForRequest(solver, r, solver.XR.prev(p), p);
				if (stMooc == null)
					continue;
				// Chèn điểm bắt đầu mooc vào trước điểm p trên route r, đồng thời đánh dấu nhóm của mooc đó là đã được sử dụng. Sau đó, lấy điểm kết thúc tương ứng của mooc đó để chuẩn bị cho việc chèn sau này nếu cần thiết.
				solver.mgr.performAddOnePoint(stMooc, solver.XR.prev(p));
				int groupMooc = solver.point2Group.get(stMooc);
				solver.group2marked.put(groupMooc, 1);
				enMooc = solver.start2stopMoocPoint.get(stMooc);
			}
		}
		if (solver.accMoocInvr.getSumWeights(solver.XR.getTerminatingPointOfRoute(r)) > 0 && enMooc != null) {
			solver.mgr.performAddOnePoint(enMooc, solver.XR.prev(solver.XR.getTerminatingPointOfRoute(r)));
		}
	}

	public void removeMoocOnRoutes(TruckContainerSolver solver, int r) {
		Point x = solver.XR.getStartingPointOfRoute(r);
		Point next_x = solver.XR.next(x);
		while (next_x != solver.XR.getTerminatingPointOfRoute(r)) {
			x = next_x;
			next_x = solver.XR.next(x);
			if (solver.startMoocPoints.contains(x) || solver.stopMoocPoints.contains(x)) {
				solver.mgr.performRemoveOnePoint(x);
				int groupMooc = solver.point2Group.get(x);
				solver.group2marked.put(groupMooc, 0);
			}
		}
	}

	private Point getBestMoocForRequest(TruckContainerSolver solver, Point curStMooc, Point p, Point np, Point q,
			Point nq) {
		Point bestMooc = curStMooc;
		double min_d = Double.MAX_VALUE;
		for (int i = 0; i < solver.startMoocPoints.size(); i++) {
			Point stMooc = solver.startMoocPoints.get(i);
			Point enMooc = solver.start2stopMoocPoint.get(stMooc);
			int groupMooc = solver.point2Group.get(stMooc);
			if ((solver.group2marked.get(groupMooc) == 1 || solver.XR.route(stMooc) != Constants.NULL_POINT)
					&& stMooc != curStMooc)
				continue;
			double d = solver.getTravelTime(p.getLocationCode(), stMooc.getLocationCode())
					+ solver.getTravelTime(stMooc.getLocationCode(), np.getLocationCode())
					+ solver.getTravelTime(q.getLocationCode(), enMooc.getLocationCode())
					+ solver.getTravelTime(enMooc.getLocationCode(), nq.getLocationCode());
			if (d < min_d) {
				min_d = d;
				bestMooc = stMooc;
			}
		}
		return bestMooc;
	}

	public void removeAllMoocFromRoutes(TruckContainerSolver solver) {
		for (int i = 0; i < solver.startMoocPoints.size(); i++) {
			Point st = solver.startMoocPoints.get(i);
			Point tp = solver.start2stopMoocPoint.get(st);
			if (solver.XR.route(st) != Constants.NULL_POINT) {
				solver.mgr.performRemoveOnePoint(st);
				int groupMooc = solver.point2Group.get(st);
				solver.group2marked.put(groupMooc, 0);
			}
			if (solver.XR.route(tp) != Constants.NULL_POINT) {
				solver.mgr.performRemoveOnePoint(tp);
				int groupMooc = solver.point2Group.get(tp);
				solver.group2marked.put(groupMooc, 0);
			}
		}
	}

	public void insertMoocForAllRoutes(TruckContainerSolver solver) {
		//
		removeAllMoocFromRoutes(solver);
		for (int r = 1; r <= solver.XR.getNbRoutes(); r++) {
			Point st = solver.XR.getStartingPointOfRoute(r);
			Point stMooc = null;
			Point preP = null;
			Point nextP = null;
			Point enMooc = null;
			for (Point p = solver.XR.next(st); p != solver.XR.getTerminatingPointOfRoute(r); p = solver.XR.next(p)) {
				if (solver.accMoocInvr.getSumWeights(solver.XR.prev(p)) <= 0) {
					stMooc = getBestStartMoocForRequest(solver, r, solver.XR.prev(p), p);
					if (stMooc == null)
						continue;
					preP = solver.XR.prev(p);
					nextP = p;
					solver.mgr.performAddOnePoint(stMooc, solver.XR.prev(p));
					int groupMooc = solver.point2Group.get(stMooc);
					solver.group2marked.put(groupMooc, 1);
				}
			}
			if (solver.accMoocInvr.getSumWeights(solver.XR.getTerminatingPointOfRoute(r)) > 0) {
				Point enPoint = solver.XR.prev(solver.XR.getTerminatingPointOfRoute(r));
				Point newStMooc = getBestMoocForRequest(solver, stMooc, preP, nextP, enPoint,
						solver.XR.getTerminatingPointOfRoute(r));
				if (newStMooc != stMooc) {
					solver.mgr.performRemoveOnePoint(stMooc);
					int groupMooc = solver.point2Group.get(stMooc);
					solver.group2marked.put(groupMooc, 0);
					solver.mgr.performAddOnePoint(newStMooc, preP);
					groupMooc = solver.point2Group.get(newStMooc);
					solver.group2marked.put(groupMooc, 1);
				}
				enMooc = solver.start2stopMoocPoint.get(newStMooc);
				solver.mgr.performAddOnePoint(enMooc, enPoint);
			}
		}
	}
}

// lời giải được khởi tạo bằng cách duyệt lần lượt các yêu cầu 
// pickup→delivery và cố gắng chèn từng yêu cầu vào một tuyến hiện 
// có theo chiến lược “chèn chỗ hợp lệ đầu tiên”: trước tiên tạo một 
// ngăn xếp các tuyến (ưu tiên tuyến ở đỉnh ngăn xếp), sau đó với mỗi 
// yêu cầu chưa được gán và nhóm chưa bị đánh dấu, lặp qua từng tuyến 
// trong ngăn xếp (bỏ qua những tuyến của nhóm đã marked mà tuyến đó 
// gần như rỗng) và duyệt hai mức vị trí chèn p (vị trí đặt pickup 
// sau p) và q (vị trí đặt delivery sau q). Ở mỗi cặp (p,q) chương 
// trình tạm thời gọi performAddTwoPoints để thêm pickup và delivery, 
// gọi insertMoocToRoutes để chèn tạm các điểm mooc cần thiết, rồi 
// kiểm tra ràng buộc bằng solver.S.violations(). Nếu không có vi 
// phạm, nó đánh dấu nhóm xe và nhóm yêu cầu là đã dùng, cập nhật 
// ngăn xếp để tăng ưu tiên cho tuyến vừa dùng, giữ thao tác chèn và 
// gỡ các mooc tạm (mooc được tối ưu lại sau cùng); nếu có vi phạm 
// thì hoàn tác chèn và gỡ mooc rồi thử vị trí khác. Sau khi cố chèn 
// mọi yêu cầu, những pickup còn lại được đưa vào danh sách bị từ 
// chối, và cuối cùng insertMoocForAllRoutes được gọi để loại bỏ mooc 
// tạm và bố trí lại các điểm mooc tối ưu trên tất cả các tuyến.
