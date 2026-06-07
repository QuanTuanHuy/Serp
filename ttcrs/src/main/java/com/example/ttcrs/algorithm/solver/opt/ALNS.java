package com.example.ttcrs.algorithm.solver.opt;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.example.ttcrs.algorithm.models.output.TruckContainerSolution;
import com.example.ttcrs.algorithm.solver.TruckContainerSolver;
import com.example.ttcrs.algorithm.solver.init.FPIUSInit;
import com.example.ttcrs.algorithm.vrp.entities.Point;

/**
 * ALNS (Adaptive Large Neighborhood Search) optimization strategy.
 * Implements the OptimizationStrategy interface.
 */
public class ALNS implements OptimizationStrategy {
	private final TruckContainerSolver solver;
	private final FPIUSInit initialSolutionBuilder;

	public ALNS(TruckContainerSolver solver) {
		this.solver = solver;
		this.initialSolutionBuilder = new FPIUSInit();
	}
	
	@Override
	public void optimize(TruckContainerSolver solver, String outputFile) {
		// Use this.solver field which was set in constructor
		adaptiveSearchOperators(outputFile);
	}

	public void initParamsForALNS() {
		solver.nChosed = new HashMap<Point, Integer>();
		solver.removeAllowed = new HashMap<Point, Boolean>();
		for (int i = 0; i < solver.pickupPoints.size(); i++) {
			Point pi = solver.pickupPoints.get(i);
			solver.nChosed.put(pi, 0);
			solver.removeAllowed.put(pi, true);

			Point pj = solver.pickup2Delivery.get(pi);
			solver.nChosed.put(pj, 0);
			solver.removeAllowed.put(pj, true);
		}
	}

	public int getNbUsedTrucks() {
		int nb = 0;
		for (int r = 1; r <= solver.XR.getNbRoutes(); r++) {
			if (solver.XR.index(solver.XR.getTerminatingPointOfRoute(r)) > 3)
				nb++;
		}
		return nb;
	}

	public int getNbRejectedRequests() {
		Set<Integer> grs = new HashSet<Integer>();
		for (int i = 0; i < solver.rejectPickupPoints.size(); i++) {
			Point pickup = solver.rejectPickupPoints.get(i);
			int groupId = solver.point2Group.get(pickup);

			if (solver.group2marked.get(groupId) == 1)
				continue;
			grs.add(groupId);
		}
		return grs.size();
	}

	public void adaptiveSearchOperators(String outputfile) {
		int it = 0;
		int iS = 0;

		// Ensure ALNS bookkeeping maps are ready
		initParamsForALNS();

		// insertion operators selection probabilities
		double[] pti = new double[solver.nInsertionOperators];
		// removal operators selection probabilities
		double[] ptd = new double[solver.nRemovalOperators];

		// wi - number of times used during last iteration
		int[] wi = new int[solver.nInsertionOperators];
		int[] wd = new int[solver.nRemovalOperators];

		// pi_i - score of operator
		int[] si = new int[solver.nInsertionOperators];
		int[] sd = new int[solver.nRemovalOperators];

		// init probabilites
		for (int i = 0; i < solver.nInsertionOperators; i++) {
			pti[i] = 1.0 / solver.nInsertionOperators;
			wi[i] = 1;
			si[i] = 0;
		}
		for (int i = 0; i < solver.nRemovalOperators; i++) {
			ptd[i] = 1.0 / solver.nRemovalOperators;
			wd[i] = 1;
			sd[i] = 0;
		}

		SearchOptimumSolution opt = new SearchOptimumSolution(solver);

		double best_cost = solver.objective.getValue();

		TruckContainerSolution best_solution = new TruckContainerSolution(solver.XR, solver.rejectPickupPoints,
				solver.rejectDeliveryPoints, best_cost, getNbUsedTrucks(), getNbRejectedRequests(),
				solver.point2Group, solver.group2marked);

		double start_search_time = System.currentTimeMillis();
		try {
			FileOutputStream write = new FileOutputStream(outputfile, true);
			PrintWriter fo = new PrintWriter(write);
			fo.println("time limit = " + solver.timeLimit + ", nbIters = " + solver.nIter + ", maxStable = " + solver.maxStable);
			fo.println("#Request = " + TruckContainerSolver.nRequest);
			fo.println("iter=====insertion=====removal=====time=====cost=====nbReject=====nbTrucks");
			fo.println("0 -1 -1 " + " " + System.currentTimeMillis() / 1000 + " " + best_cost + " "
					+ getNbRejectedRequests() + " " + getNbUsedTrucks());
			fo.close();
		} catch (Exception e) {
			System.out.println(e);
		}

		while ((System.currentTimeMillis() - start_search_time) < solver.timeLimit && it++ < solver.nIter) {
			System.out.println("nb of iterator: " + it);
			double current_cost = solver.objective.getValue();
			int current_nbTrucks = getNbUsedTrucks();
			TruckContainerSolution current_solution = new TruckContainerSolution(solver.XR, solver.rejectPickupPoints,
					solver.rejectDeliveryPoints, current_cost, current_nbTrucks, solver.getNbRejectedRequests(),
					solver.point2Group, solver.group2marked);

			// Was solver.removeAllMoocFromRoutes(); but it moved to builder
			initialSolutionBuilder.removeAllMoocFromRoutes(solver);

			int i_selected_removal = -1;
			if (iS >= solver.maxStable) {
				opt.allRemoval();
				iS = 0;
			} else {
				i_selected_removal = get_operator(ptd);
				wd[i_selected_removal]++;
				switch (i_selected_removal) {
					case 0:
						opt.routeRemoval();
						break;
					case 1:
						opt.randomRequestRemoval();
						break;
					case 2:
						opt.shaw_removal();
						break;
					case 3:
						opt.worst_removal();
						break;
					case 4:
						opt.forbidden_removal(0);
						break;
					case 5:
						opt.forbidden_removal(1);
						break;
					case 6:
						opt.forbidden_removal(2);
						break;
					case 7:
						opt.forbidden_removal(3);
						break;
				}
			}

			int i_selected_insertion = get_operator(pti);
			wi[i_selected_insertion]++;
			switch (i_selected_insertion) {
				case 0:
					opt.greedyInsertion();
					break;
				case 1:
					opt.greedyInsertionWithNoise();
					break;
				case 2:
					opt.regret_n_insertion(2);
					break;
				case 3:
					opt.first_possible_insertion();
					break;
				case 4:
					opt.sort_before_insertion(0);
					break;
				case 5:
					opt.sort_before_insertion(1);
					break;
				case 6:
					opt.sort_before_insertion(2);
					break;
				case 7:
					opt.sort_before_insertion(3);
					break;
			}

			int new_nb_reject_points = solver.rejectPickupPoints.size();
			double new_cost = solver.objective.getValue();
			int new_nbTrucks = getNbUsedTrucks();
			int current_nb_reject_points = current_solution.get_rejectPickupPoints().size();

			if (new_nb_reject_points < current_nb_reject_points
					|| (new_nb_reject_points == current_nb_reject_points && new_cost < current_cost)) {
				int best_nb_reject_points = best_solution.get_rejectPickupPoints().size();

				if (new_nb_reject_points < best_nb_reject_points
						|| (new_nb_reject_points == best_nb_reject_points && new_cost < best_cost)) {

					best_cost = new_cost;
					best_solution = new TruckContainerSolution(solver.XR, solver.rejectPickupPoints, solver.rejectDeliveryPoints,
							new_cost, new_nbTrucks, new_nb_reject_points, solver.point2Group, solver.group2marked);
					try {
						FileOutputStream write = new FileOutputStream(outputfile, true);
						PrintWriter fo = new PrintWriter(write);
						fo.println(it + " " + i_selected_insertion + " " + i_selected_removal + " "
								+ System.currentTimeMillis() / 1000 + " " + best_cost + " " + getNbRejectedRequests()
								+ " " + getNbUsedTrucks());
						fo.close();
					} catch (Exception e) {
						System.out.println(e);
					}
					si[i_selected_insertion] += solver.sigma1;
					if (i_selected_removal >= 0)
						sd[i_selected_removal] += solver.sigma1;
				} else {
					si[i_selected_insertion] += solver.sigma2;
					if (i_selected_removal >= 0)
						sd[i_selected_removal] += solver.sigma2;
				}
			}
			/*
			 * if new solution has cost worst than current solution
			 * because XR is new solution
			 * copy current solution back if reject
			 */
			else {
				si[i_selected_insertion] += solver.sigma3;
				if (i_selected_removal >= 0)
					sd[i_selected_removal] += solver.sigma3;
				double v = Math.exp(-(new_cost - current_cost) / solver.temperature);
				double e = Math.random();
				if (e >= v) {
					current_solution.copy2XR(solver.XR);
					solver.group2marked = current_solution.get_group2marked();
					solver.rejectPickupPoints = current_solution.get_rejectPickupPoints();
					solver.rejectDeliveryPoints = current_solution.get_rejectDeliveryPoints();
				}
				iS++;
			}

			solver.temperature = solver.cooling_rate * solver.temperature;

			// update probabilities
			if (it % solver.nw == 0) {
				for (int i = 0; i < solver.nInsertionOperators; i++) {
					pti[i] = Math.max(0.0001, pti[i] * (1 - solver.rp) + solver.rp * si[i] / wi[i]);
				}

				for (int i = 0; i < solver.nRemovalOperators; i++) {
					ptd[i] = Math.max(0.0001, ptd[i] * (1 - solver.rp) + solver.rp * sd[i] / wd[i]);
				}
			}
		}

		best_solution.copy2XR(solver.XR);
		solver.group2marked = best_solution.get_group2marked();

		solver.rejectPickupPoints = best_solution.get_rejectPickupPoints();
		solver.rejectDeliveryPoints = best_solution.get_rejectDeliveryPoints();
		try {
			FileOutputStream write = new FileOutputStream(outputfile, true);
			PrintWriter fo = new PrintWriter(write);
			fo.println(it + " -1 -1 " + System.currentTimeMillis() / 1000 + " " + best_cost + " "
					+ getNbRejectedRequests() + " " + getNbUsedTrucks());
			fo.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private int get_operator(double[] p) {
		int n = p.length;
		double[] s = new double[n];
		s[0] = 0 + p[0];

		for (int i = 1; i < n; i++)
			s[i] = s[i - 1] + p[i];

		double r = s[n - 1] * Math.random();

		if (r >= 0 && r <= s[0])
			return 0;

		for (int i = 1; i < n; i++) {
			if (r > s[i - 1] && r <= s[i])
				return i;
		}
		return -1;
	}
}

