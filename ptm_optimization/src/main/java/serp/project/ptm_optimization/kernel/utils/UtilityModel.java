package serp.project.ptm_optimization.kernel.utils;

/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.TaskInput;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.input.Weights;
import serp.project.ptm_optimization.infrastructure.algorithm.dto.output.UtilityBreakdown;

@Component
@Slf4j
public class UtilityModel {
    private static final double DEADLINE_MINUTES_SCALE = 60.0;

    private static final double FATIGUE_MINUTES_SCALE = 120.0;

    public Double latenessMin(Long deadlineMs, Long dateMs, Integer endMin) {
        if (deadlineMs == null) {
            return 0.0;
        }
        long endAbs = dateMs + endMin * 60_000L;
        if (endAbs <= deadlineMs) {
            return 0.0;
        }
        return (endAbs - deadlineMs) / 60_000.0;
    }

    public Double fatiguePenalty(Integer continuousMin) {
        if (continuousMin == null || continuousMin <= 0)
            return 0.0;
        double x = continuousMin / FATIGUE_MINUTES_SCALE;
        return Math.sqrt(x);
    }

    public int categoryKey(TaskInput t) {
        double enjoy = t.getEnjoyability() == null ? 0.0 : t.getEnjoyability();
        int dur = t.getDurationMin() == null ? 0 : t.getDurationMin();
        int durBucket = dur / 30;
        int key = durBucket;
        if (enjoy > 0)
            key += 100;
        else if (enjoy < 0)
            key -= 100;
        return key;
    }

    public Pair<Double, UtilityBreakdown> scorePlacement(
            TaskInput task,
            Long dateMs,
            Integer startMin,
            Integer endMin,
            Integer prevCat,
            Weights weights,
            Integer contSoFar) {
        double wPriority = weights.getWPriority() == null ? 0.0 : weights.getWPriority();
        double wDeadline = weights.getWDeadline() == null ? 0.0 : weights.getWDeadline();
        double wSwitch = weights.getWSwitch() == null ? 0.0 : weights.getWSwitch();
        double wFatigue = weights.getWFatigue() == null ? 0.0 : weights.getWFatigue();
        double wEnjoy = weights.getWEnjoy() == null ? 0.0 : weights.getWEnjoy();

        double priorityScore = clamp(task.getPriorityScore(), 0.0, 1.0);
        double enjoy = clamp(task.getEnjoyability(), -1.0, 1.0);

        // Normalize lateness to hours to reduce cross-weight sensitivity
        double lateMin = latenessMin(task.getDeadlineMs(), dateMs, endMin);
        double lateNorm = lateMin / DEADLINE_MINUTES_SCALE;
        double pri = wPriority * priorityScore;
        double dead = -wDeadline * lateNorm;

        Double sw = 0.0;
        int cat = categoryKey(task);
        if (prevCat != null && prevCat.intValue() != cat) {
            sw = -wSwitch;
        }

        int duration = (endMin == null || startMin == null) ? 0 : (endMin - startMin);
        int cont = (contSoFar == null ? 0 : contSoFar) + duration;
        double fat = -wFatigue * fatiguePenalty(cont);
        double enj = wEnjoy * enjoy;

        double util = pri + dead + sw + fat + enj;
        UtilityBreakdown breakdown = UtilityBreakdown.builder()
                .priority(pri)
                .deadline(dead)
                .uSwitch(sw)
                .fatigue(fat)
                .enjoy(enj)
                .build();
        return Pair.of(util, breakdown);
    }

    private static double clamp(Double v, double lo, double hi) {
        if (v == null)
            return lo;
        return Math.max(lo, Math.min(hi, v));
    }
}
