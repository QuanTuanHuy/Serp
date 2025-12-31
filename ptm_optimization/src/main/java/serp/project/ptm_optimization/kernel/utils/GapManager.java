/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Gap Management Utility
 */

package serp.project.ptm_optimization.kernel.utils;

import lombok.extern.slf4j.Slf4j;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Window;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.Assignment;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility for calculating and managing time gaps between assignments.
 * Inspired by ptm_schedule's TimeSlotUtils.CalculateGaps.
 * 
 * This enables efficient gap-based scheduling where tasks are inserted
 * into available gaps rather than simply appending after a cursor.
 */
@Component
@Slf4j
public class GapManager {

    /**
     * Calculate available gaps given base windows and booked assignments.
     * 
     * Algorithm:
     * 1. Group assignments by date
     * 2. For each window:
     *    a. Get assignments on that date
     *    b. Sort by start time
     *    c. Calculate gaps between assignments
     *    d. Add remaining gap at end if exists
     * 
     * Example:
     *   Window: [9:00 - 17:00] (540 min)
     *   Assignments: [9:00-10:30], [11:00-12:00], [14:00-15:30]
     *   Gaps: [10:30-11:00], [12:00-14:00], [15:30-17:00]
     * 
     * @param baseWindows Available time windows (e.g., work hours)
     * @param bookedAssignments Already scheduled assignments
     * @return List of gap windows where new tasks can be inserted
     */
    public List<Window> calculateGaps(List<Window> baseWindows, List<Assignment> bookedAssignments) {
        if (baseWindows == null || baseWindows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Window> gaps = new ArrayList<>();

        Map<Long, List<Assignment>> assignmentsByDate = bookedAssignments.stream()
                .collect(Collectors.groupingBy(Assignment::getDateMs));

        for (Window baseWin : baseWindows) {
            Long dateMs = baseWin.getDateMs();
            int windowStart = baseWin.getStartMin() != null ? baseWin.getStartMin() : 0;
            int windowEnd = baseWin.getEndMin() != null ? baseWin.getEndMin() : 1440;

            List<Assignment> dailyAssignments = filterAssignmentsForWindow(
                    assignmentsByDate.getOrDefault(dateMs, Collections.emptyList()),
                    baseWin
            );

            dailyAssignments.sort(Comparator.comparingInt(Assignment::getStartMin));

            int cursor = windowStart;

            for (Assignment assignment : dailyAssignments) {
                int assignmentStart = Math.max(windowStart, assignment.getStartMin());
                int assignmentEnd = Math.min(windowEnd, assignment.getEndMin());

                // Gap before this assignment
                if (assignmentStart > cursor) {
                    gaps.add(Window.builder()
                            .dateMs(dateMs)
                            .startMin(cursor)
                            .endMin(assignmentStart)
                            .isDeepWork(baseWin.getIsDeepWork())
                            .build());
                }

                cursor = Math.max(cursor, assignmentEnd);
            }

            // Gap at the end of window
            if (cursor < windowEnd) {
                gaps.add(Window.builder()
                        .dateMs(dateMs)
                        .startMin(cursor)
                        .endMin(windowEnd)
                        .isDeepWork(baseWin.getIsDeepWork())
                        .build());
            }
        }

        log.debug("Calculated {} gaps from {} windows and {} assignments",
                gaps.size(), baseWindows.size(), bookedAssignments.size());

        return gaps;
    }

    /**
     * Filter assignments that overlap with a given window.
     */
    private List<Assignment> filterAssignmentsForWindow(List<Assignment> assignments, Window window) {
        int windowStart = window.getStartMin() != null ? window.getStartMin() : 0;
        int windowEnd = window.getEndMin() != null ? window.getEndMin() : 1440;
        Long windowDate = window.getDateMs();

        return assignments.stream()
                .filter(a -> Objects.equals(a.getDateMs(), windowDate))
                .filter(a -> a.getEndMin() > windowStart && a.getStartMin() < windowEnd)
                .collect(Collectors.toList());
    }

    /**
     * Find assignments that conflict (overlap) with a given time range.
     * 
     * Used in ripple effect to find victims to displace.
     * 
     * @param schedule Current schedule
     * @param dateMs Target date
     * @param startMin Target start time
     * @param endMin Target end time
     * @return List of conflicting assignments
     */
    public List<Assignment> findConflicts(
            List<Assignment> schedule,
            Long dateMs,
            int startMin,
            int endMin
    ) {
        return schedule.stream()
                .filter(a -> Objects.equals(a.getDateMs(), dateMs))
                .filter(a -> a.getEndMin() > startMin && a.getStartMin() < endMin)
                .collect(Collectors.toList());
    }

    /**
     * Check if two assignments overlap.
     */
    public boolean overlaps(Assignment a1, Assignment a2) {
        if (!Objects.equals(a1.getDateMs(), a2.getDateMs())) {
            return false;
        }

        return Math.max(a1.getStartMin(), a2.getStartMin()) <
               Math.min(a1.getEndMin(), a2.getEndMin());
    }

    /**
     * Check if adding a new assignment would cause any overlaps.
     */
    public boolean wouldOverlap(Assignment newAssignment, List<Assignment> existingSchedule) {
        return existingSchedule.stream()
                .anyMatch(existing -> overlaps(newAssignment, existing));
    }

    /**
     * Calculate fragmentation score for a schedule.
     * Lower is better (less fragmentation).
     * 
     * Fragmentation = number of small unusable gaps
     * 
     * @param windows Base windows
     * @param schedule Current schedule
     * @param minUsefulGap Minimum gap size to be useful (e.g., 15 min)
     * @return Fragmentation score (0-100)
     */
    public double calculateFragmentation(
            List<Window> windows,
            List<Assignment> schedule,
            int minUsefulGap
    ) {
        List<Window> gaps = calculateGaps(windows, schedule);

        long smallGaps = gaps.stream()
                .filter(gap -> (gap.getEndMin() - gap.getStartMin()) < minUsefulGap)
                .count();

        if (gaps.isEmpty()) {
            return 0.0;
        }

        return (double) smallGaps / gaps.size() * 100.0;
    }

    /**
     * Calculate total gap duration.
     */
    public int totalGapDuration(List<Window> gaps) {
        return gaps.stream()
                .mapToInt(gap -> gap.getEndMin() - gap.getStartMin())
                .sum();
    }

    /**
     * Get largest gap (most promising for scheduling).
     */
    public Optional<Window> getLargestGap(List<Window> gaps) {
        return gaps.stream()
                .max(Comparator.comparingInt(gap -> gap.getEndMin() - gap.getStartMin()));
    }

    /**
     * Merge overlapping or adjacent gaps.
     * Useful after removing assignments.
     */
    public List<Window> mergeGaps(List<Window> gaps) {
        if (gaps.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Window>> gapsByDate = gaps.stream()
                .collect(Collectors.groupingBy(Window::getDateMs));

        List<Window> merged = new ArrayList<>();

        for (var entry : gapsByDate.entrySet()) {
            Long dateMs = entry.getKey();
            List<Window> dateGaps = entry.getValue();

            dateGaps.sort(Comparator.comparingInt(Window::getStartMin));

            Window current = dateGaps.get(0);

            for (int i = 1; i < dateGaps.size(); i++) {
                Window next = dateGaps.get(i);

                // Adjacent or overlapping
                if (next.getStartMin() <= current.getEndMin()) {
                    current = Window.builder()
                            .dateMs(dateMs)
                            .startMin(current.getStartMin())
                            .endMin(Math.max(current.getEndMin(), next.getEndMin()))
                            .isDeepWork(current.getIsDeepWork())
                            .build();
                } else {
                    // Non-overlapping, add current and move to next
                    merged.add(current);
                    current = next;
                }
            }

            merged.add(current);
        }

        return merged;
    }
}
