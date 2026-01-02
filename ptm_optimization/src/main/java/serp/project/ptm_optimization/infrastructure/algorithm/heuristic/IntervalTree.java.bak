/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.ptm_optimization.infrastructure.algorithm.heuristic;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import lombok.Data;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.Assignment;

/**
 * Interval Tree for efficient overlap detection in O(log n + k) time
 * where k is the number of overlapping intervals found.
 */
public class IntervalTree {

    @Data
    private static class Interval {
        private final int start;
        private final int end;
        private final Assignment assignment;

        public Interval(int start, int end, Assignment assignment) {
            this.start = start;
            this.end = end;
            this.assignment = assignment;
        }

        public boolean overlaps(int otherStart, int otherEnd) {
            return !(end <= otherStart || otherEnd <= start);
        }
    }

    private final TreeMap<Long, List<Interval>> intervalsByDate;

    public IntervalTree() {
        this.intervalsByDate = new TreeMap<>();
    }

    public void add(Assignment a) {
        if (a.getDateMs() == null || a.getStartMin() == null || a.getEndMin() == null) {
            return;
        }

        Long date = a.getDateMs();
        int start = a.getStartMin();
        int end = a.getEndMin();

        intervalsByDate.computeIfAbsent(date, k -> new ArrayList<>())
                .add(new Interval(start, end, a));
    }

    public void build(List<Assignment> assignments) {
        for (Assignment a : assignments) {
            add(a);
        }

        for (List<Interval> intervals : intervalsByDate.values()) {
            intervals.sort((i1, i2) -> Integer.compare(i1.start, i2.start));
        }
    }

    public boolean hasOverlap(Long dateMs, int start, int end) {
        List<Interval> intervals = intervalsByDate.get(dateMs);
        if (intervals == null || intervals.isEmpty()) {
            return false;
        }

        int left = 0;
        int right = intervals.size();

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (intervals.get(mid).start < end) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        for (int i = 0; i < left; i++) {
            Interval interval = intervals.get(i);
            if (interval.overlaps(start, end)) {
                return true;
            }
        }

        return false;
    }

    public List<Assignment> findOverlapping(Long dateMs, int start, int end) {
        List<Assignment> result = new ArrayList<>();
        List<Interval> intervals = intervalsByDate.get(dateMs);

        if (intervals == null || intervals.isEmpty()) {
            return result;
        }

        int left = 0;
        int right = intervals.size();

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (intervals.get(mid).start < end) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }

        for (int i = 0; i < left; i++) {
            Interval interval = intervals.get(i);
            if (interval.overlaps(start, end)) {
                result.add(interval.assignment);
            }
        }

        return result;
    }

    public static boolean hasAnyOverlap(List<Assignment> assignments) {
        TreeMap<Long, List<Assignment>> byDate = new TreeMap<>();
        for (Assignment a : assignments) {
            if (a.getDateMs() == null)
                continue;
            byDate.computeIfAbsent(a.getDateMs(), k -> new ArrayList<>()).add(a);
        }

        for (List<Assignment> dayAssignments : byDate.values()) {
            dayAssignments.sort((a1, a2) -> {
                int s1 = a1.getStartMin() != null ? a1.getStartMin() : 0;
                int s2 = a2.getStartMin() != null ? a2.getStartMin() : 0;
                return Integer.compare(s1, s2);
            });

            for (int i = 1; i < dayAssignments.size(); i++) {
                Assignment prev = dayAssignments.get(i - 1);
                Assignment curr = dayAssignments.get(i);

                int prevEnd = prev.getEndMin() != null ? prev.getEndMin() : 0;
                int currStart = curr.getStartMin() != null ? curr.getStartMin() : 0;

                if (prevEnd > currStart) {
                    return true;
                }
            }
        }

        return false;
    }

    public void clear() {
        intervalsByDate.clear();
    }

    public int size(Long dateMs) {
        List<Interval> intervals = intervalsByDate.get(dateMs);
        return intervals != null ? intervals.size() : 0;
    }

    public int totalSize() {
        return intervalsByDate.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}
