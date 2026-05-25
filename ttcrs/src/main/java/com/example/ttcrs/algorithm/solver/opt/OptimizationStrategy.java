package com.example.ttcrs.algorithm.solver.opt;

import com.example.ttcrs.algorithm.solver.TruckContainerSolver;

public interface OptimizationStrategy {
    void optimize(TruckContainerSolver solver, String outputFile);
}

