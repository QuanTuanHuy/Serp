/*
Author: QuanTuanHuy
Description: Part of Serp Project - Local Search Move Operations
*/

package serp.project.ptm_optimization.infrastructure.algorithm.localsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a move operation in local search.
 * A move modifies the current schedule to explore the neighborhood.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Move {
    
    private MoveType type;
    
    // For SWAP
    private Long taskId1;
    private Long taskId2;
    
    // For SHIFT
    private Long taskId;
    private Long targetDateMs;
    private Integer targetStartMin;
    
    // For REORDER (swap positions on same day)
    private Integer position1;
    private Integer position2;
    
    public static Move swap(Long taskId1, Long taskId2) {
        Move move = new Move();
        move.setType(MoveType.SWAP);
        move.setTaskId1(taskId1);
        move.setTaskId2(taskId2);
        return move;
    }
    
    public static Move shift(Long taskId, Long targetDateMs, Integer targetStartMin) {
        Move move = new Move();
        move.setType(MoveType.SHIFT);
        move.setTaskId(taskId);
        move.setTargetDateMs(targetDateMs);
        move.setTargetStartMin(targetStartMin);
        return move;
    }
    
    public static Move reorder(Long taskId1, Long taskId2, Integer position1, Integer position2) {
        Move move = new Move();
        move.setType(MoveType.REORDER);
        move.setTaskId1(taskId1);
        move.setTaskId2(taskId2);
        move.setPosition1(position1);
        move.setPosition2(position2);
        return move;
    }
    
    public enum MoveType {
        SWAP,    // Swap two tasks' time slots
        SHIFT,   // Move task to different gap
        REORDER  // Change order within same day
    }
}
