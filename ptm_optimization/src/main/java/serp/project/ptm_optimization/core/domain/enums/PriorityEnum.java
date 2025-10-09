/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.core.domain.enums;

import java.util.List;

import lombok.Getter;

@Getter
public enum PriorityEnum {
    LOW("LOW", 1),
    MEDIUM("MEDIUM", 2),
    HIGH("HIGH", 3),
    STAR("STAR", 5);

    private final String name;
    private final Integer weight;

    PriorityEnum(String name, Integer weight) {
        this.name = name;
        this.weight = weight;
    }

    public static PriorityEnum fromName(String name) {
        for (PriorityEnum priority : PriorityEnum.values()) {
            if (priority.getName().equalsIgnoreCase(name)) {
                return priority;
            }
        }
        return null;
    }

    public static Integer calculateWeightOfPriorities(List<PriorityEnum> priorities) {
        if (priorities == null || priorities.isEmpty()) {
            return 0;
        }
        return priorities.stream()
                .mapToInt(PriorityEnum::getWeight)
                .sum();
    }
}
