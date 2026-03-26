/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.dto.workitem.create;

public record FieldPolicy(String fieldRefType,
                          String fieldRef,
                          boolean required,
                          boolean hidden,
                          boolean onCreateScreen) {

    public boolean isClientWritableOnCreate() {
        return !hidden && onCreateScreen;
    }
}
