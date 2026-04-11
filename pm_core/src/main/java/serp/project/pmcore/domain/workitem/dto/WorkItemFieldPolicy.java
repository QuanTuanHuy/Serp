/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.workitem.dto;

public record WorkItemFieldPolicy(
        String fieldRefType,
        String fieldRef,
        boolean required,
        boolean hidden,
        boolean onScreen
) {

    public boolean isClientWritable() {
        return !hidden && onScreen;
    }
}
