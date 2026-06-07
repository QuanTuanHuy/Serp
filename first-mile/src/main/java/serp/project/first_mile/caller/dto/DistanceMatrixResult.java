package serp.project.first_mile.caller.dto;

import serp.project.first_mile.caller.DistanceMatrixCaller;

import java.util.List;

public record DistanceMatrixResult(List<List<DistanceMatrixElement>> rows) {
}
