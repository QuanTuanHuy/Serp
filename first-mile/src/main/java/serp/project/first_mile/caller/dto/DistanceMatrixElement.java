/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.caller.dto;

public record DistanceMatrixElement(String status, Long durationSeconds, Long distanceMeters) {
    public boolean isOk() {
        return "OK".equalsIgnoreCase(status)
                && durationSeconds != null && durationSeconds >= 0
                && distanceMeters != null && distanceMeters >= 0;
    }
}
