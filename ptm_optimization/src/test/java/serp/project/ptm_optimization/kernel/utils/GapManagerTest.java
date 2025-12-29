/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - GapManager Unit Tests
 */

package serp.project.ptm_optimization.kernel.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.input.Window;
import serp.project.ptm_optimization.kernel.algorithm.heuristic.dto.output.Assignment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GapManagerTest {

    private GapManager gapManager;

    @BeforeEach
    void setUp() {
        gapManager = new GapManager();
    }

    @Test
    void testCalculateGaps_emptySchedule() {
        // Given: One window, no assignments
        Window window = Window.builder()
                .dateMs(1735516800000L) // 2024-12-30
                .startMin(540)          // 9:00 AM
                .endMin(1020)           // 5:00 PM (480 min)
                .build();

        List<Window> windows = Collections.singletonList(window);
        List<Assignment> assignments = Collections.emptyList();

        // When
        List<Window> gaps = gapManager.calculateGaps(windows, assignments);

        // Then: Entire window is a gap
        assertEquals(1, gaps.size());
        assertEquals(540, gaps.get(0).getStartMin());
        assertEquals(1020, gaps.get(0).getEndMin());
        assertEquals(480, gaps.get(0).getEndMin() - gaps.get(0).getStartMin());
    }

    @Test
    void testCalculateGaps_multipleTasksInWindow() {
        // Given: Window 9am-5pm with 3 tasks
        Window window = Window.builder()
                .dateMs(1735516800000L)
                .startMin(540)  // 9:00
                .endMin(1020)   // 17:00
                .build();

        List<Assignment> assignments = Arrays.asList(
                // Task A: 9:00-10:30 (90 min)
                createAssignment(1L, 1735516800000L, 540, 630),
                // Task B: 11:00-12:00 (60 min)
                createAssignment(2L, 1735516800000L, 660, 720),
                // Task C: 14:00-15:30 (90 min)
                createAssignment(3L, 1735516800000L, 840, 930)
        );

        // When
        List<Window> gaps = gapManager.calculateGaps(
                Collections.singletonList(window),
                assignments
        );

        // Then: 3 gaps
        assertEquals(3, gaps.size());

        // Gap 1: 10:30-11:00 (30 min)
        assertEquals(630, gaps.get(0).getStartMin());
        assertEquals(660, gaps.get(0).getEndMin());

        // Gap 2: 12:00-14:00 (120 min)
        assertEquals(720, gaps.get(1).getStartMin());
        assertEquals(840, gaps.get(1).getEndMin());

        // Gap 3: 15:30-17:00 (90 min)
        assertEquals(930, gaps.get(2).getStartMin());
        assertEquals(1020, gaps.get(2).getEndMin());

        // Total gap duration: 30 + 120 + 90 = 240 min
        int totalGapDuration = gapManager.totalGapDuration(gaps);
        assertEquals(240, totalGapDuration);
    }

    @Test
    void testFindConflicts() {
        // Given: Schedule with 2 assignments
        List<Assignment> schedule = Arrays.asList(
                createAssignment(1L, 1735516800000L, 540, 630),   // 9:00-10:30
                createAssignment(2L, 1735516800000L, 720, 840)    // 12:00-14:00
        );

        // When: Check conflicts for 10:00-11:00
        List<Assignment> conflicts = gapManager.findConflicts(
                schedule,
                1735516800000L,
                600,  // 10:00
                660   // 11:00
        );

        // Then: Conflicts with Task 1
        assertEquals(1, conflicts.size());
        assertEquals(1L, conflicts.get(0).getTaskId());
    }

    @Test
    void testOverlaps() {
        // Given
        Assignment a1 = createAssignment(1L, 1735516800000L, 540, 630);  // 9:00-10:30
        Assignment a2 = createAssignment(2L, 1735516800000L, 600, 720);  // 10:00-12:00 (overlaps with a1)
        Assignment a3 = createAssignment(3L, 1735516800000L, 630, 900);  // 10:30-15:00 (adjacent/touching a1)
        Assignment a4 = createAssignment(4L, 1735516800000L, 750, 900);  // 12:30-15:00 (no overlap with a1)

        // When/Then
        assertTrue(gapManager.overlaps(a1, a2), "a1 and a2 should overlap");   
        assertFalse(gapManager.overlaps(a1, a4), "a1 and a4 should not overlap");  
        // Adjacent times (630-630) are not considered overlapping
        assertFalse(gapManager.overlaps(a1, a3), "Adjacent times should not overlap");
    }

    @Test
    void testCalculateFragmentation() {
        // Given: Window with small gaps
        Window window = Window.builder()
                .dateMs(1735516800000L)
                .startMin(540)
                .endMin(1020)
                .build();

        List<Assignment> assignments = Arrays.asList(
                createAssignment(1L, 1735516800000L, 540, 630),   // 9:00-10:30
                createAssignment(2L, 1735516800000L, 640, 720),   // 10:40-12:00 (10 min gap)
                createAssignment(3L, 1735516800000L, 725, 840)    // 12:05-14:00 (5 min gap)
        );

        // When: Calculate fragmentation (min useful gap = 15 min)
        double fragmentation = gapManager.calculateFragmentation(
                Collections.singletonList(window),
                assignments,
                15
        );

        // Then: 2 small gaps out of 3 total = 66.7% fragmentation
        assertTrue(fragmentation > 60.0 && fragmentation < 70.0);
    }

    @Test
    void testMergeGaps() {
        // Given: Adjacent gaps
        List<Window> gaps = Arrays.asList(
                Window.builder().dateMs(1735516800000L).startMin(540).endMin(600).build(),   // 9:00-10:00
                Window.builder().dateMs(1735516800000L).startMin(600).endMin(720).build(),   // 10:00-12:00 (adjacent)
                Window.builder().dateMs(1735516800000L).startMin(840).endMin(900).build()    // 14:00-15:00 (separate)
        );

        // When
        List<Window> merged = gapManager.mergeGaps(gaps);

        // Then: 2 gaps (first two merged)
        assertEquals(2, merged.size());
        assertEquals(540, merged.get(0).getStartMin());
        assertEquals(720, merged.get(0).getEndMin());  // Merged 9:00-12:00
        assertEquals(840, merged.get(1).getStartMin());
        assertEquals(900, merged.get(1).getEndMin());
    }

    @Test
    void testGetLargestGap() {
        // Given
        List<Window> gaps = Arrays.asList(
                Window.builder().dateMs(1735516800000L).startMin(540).endMin(570).build(),   // 30 min
                Window.builder().dateMs(1735516800000L).startMin(600).endMin(720).build(),   // 120 min (largest)
                Window.builder().dateMs(1735516800000L).startMin(840).endMin(900).build()    // 60 min
        );

        // When
        Window largest = gapManager.getLargestGap(gaps).orElse(null);

        // Then
        assertNotNull(largest);
        assertEquals(600, largest.getStartMin());
        assertEquals(720, largest.getEndMin());
        assertEquals(120, largest.getEndMin() - largest.getStartMin());
    }

    // Helper methods

    private Assignment createAssignment(Long taskId, Long dateMs, int startMin, int endMin) {
        return Assignment.builder()
                .taskId(taskId)
                .dateMs(dateMs)
                .startMin(startMin)
                .endMin(endMin)
                .build();
    }
}
